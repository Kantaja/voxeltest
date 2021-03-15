package info.kuonteje.voxeltest;

import static org.lwjgl.glfw.GLFW.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joml.Vector3d;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;

import info.kuonteje.voxeltest.console.Console;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.RegistryManager;
import info.kuonteje.voxeltest.render.DebugCamera;
import info.kuonteje.voxeltest.render.Renderer;
import info.kuonteje.voxeltest.render.Window;
import info.kuonteje.voxeltest.util.IDestroyable;
import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.world.World;

public class VoxelTest
{
	public static final String DEFAULT_DOMAIN = "voxeltest";
	
	public static final Path CFG_PATH = Paths.get("config");
	
	public static final Console CONSOLE = new Console(CFG_PATH);
	
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.enable(JsonParser.Feature.ALLOW_COMMENTS)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
	
	public static final CvarI64 eWorkers = CONSOLE.cvars().cvarI64("e_workers", Math.max(1, Runtime.getRuntime().availableProcessors() / 2), Cvar.Flags.CONFIG | Cvar.Flags.LATCH, v -> MathUtil.clamp(v, 1, Runtime.getRuntime().availableProcessors()));
	
	public static final CvarI64 clWindowedWidth = CONSOLE.cvars().cvarI64("cl_windowed_width", 1366L, Cvar.Flags.CONFIG, v -> Math.max(v, 0L), (n, o) -> addRenderHook(() -> window().setSize((int)n, window().height())));
	public static final CvarI64 clWindowedHeight = CONSOLE.cvars().cvarI64("cl_windowed_height", 768L, Cvar.Flags.CONFIG, v -> Math.max(v, 0L), (n, o) -> addRenderHook(() -> window().setSize(window().width(), (int)n)));
	
	public static final CvarI64 clFullscreen = CONSOLE.cvars().cvarBool("cl_fullscreen", false, Cvar.Flags.CONFIG, null, (n, o) -> addRenderHook(() -> { if(n != o) window().toggleFullscreen0(); }));
	
	public static final CvarI64 clVsyncInterval = CONSOLE.cvars().cvarI64("cl_vsync_interval", 1L, Cvar.Flags.CONFIG, v -> Math.max(v, 0L), (n, o) -> addRenderHook(() -> window().setSwapInterval((int)n)));
	
	private static final AtomicBoolean shuttingDown = new AtomicBoolean(false);
	private static final List<Runnable> shutdownHooks = Collections.synchronizedList(new ArrayList<>());
	
	private static final Deque<IDestroyable> destroyables = new ConcurrentLinkedDeque<>();
	
	private static final Queue<Runnable> renderHooks = new ConcurrentLinkedQueue<>();
	private static long renderThreadId = -1;
	
	private static ExecutorService threadPool;
	
	private static Window window;
	private static DebugCamera camera;
	
	private static World world;
	
	private static volatile double frameStart, partialTick;
	
	private static volatile boolean initialized = false;
	
	public static void main(String[] args)
	{
		//try { Thread.sleep(5000L); } catch(Exception e) {}
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Thread.currentThread().setName("Init thread");
		
		if(!glfwInit()) throw new RuntimeException("Failed to initialize GLFW");
		
		try
		{
			if(!Files.exists(CFG_PATH))
			{
				try
				{
					Files.createDirectory(CFG_PATH);
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			
			threadPool = Executors.newFixedThreadPool(eWorkers.asInt());
			
			window = new Window("VoxelTest", clWindowedWidth.asInt(), clWindowedHeight.asInt(), clFullscreen.asBool());
			Renderer.init(window);
			
			DefaultRegistries.init();
			
			RegistryManager.freezeAll();
			
			camera = new DebugCamera(window::getKey, window::getMouse);
			
			window.setSwapInterval(clVsyncInterval.asInt());
			
			//Ticks.addTickHandler(world = new World(-5477951967142400137L));
			Ticks.addTickHandler(world = new World());
			addShutdownHook(world::destroy);
			
			Ticks.init();
			
			startConsoleInput();
			
			Thread.currentThread().setName("Render thread");
			renderThreadId = Thread.currentThread().getId();
			
			window.captureMouse();
			
			initialized = true;
			
			double previousTime = frameStart = glfwGetTime();
			double delta;
			
			int frames = 0;
			double accum = 0.0;
			
			while(!window.shouldClose())
			{
				delta = frameStart - previousTime;
				previousTime = frameStart;
				
				accum += delta;
				
				if(accum >= 1.0)
				{
					Vector3d pos = camera.getPosition(new Vector3d());
					
					int x = MathUtil.fastFloor(pos.x);
					int y = MathUtil.fastFloor(pos.y);
					int z = MathUtil.fastFloor(pos.z);
					
					window.setTitle("VoxelTest (" + MathUtil.roundDisplay(frames / accum) + " fps, " + MathUtil.roundDisplay(accum / frames * 1000.0) + " ms), camera position ("
							+ x + ", " + y + ", " + z + "), in chunk (" + (x >> 5) + ", " + (y >> 5) + ", " + (z >> 5) + ") - " + world.statusOfChunkAt(x >> 5, y >> 5, z >> 5).toString());
					
					frames = 0;
					accum = 0.0;
				}
				
				frameStart = glfwGetTime();
				partialTick = ((frameStart - Ticks.lastTickTime()) - Ticks.tickLength()) * Ticks.tickrate();
				
				camera.frame(delta);
				Renderer.beginFrame(camera);
				
				processRenderHooks();
				
				world.render();
				
				Renderer.completeFrame(delta);
				
				frames++;
				window.update();
			}
		}
		finally
		{
			shuttingDown.setRelease(true);
			
			shutdownHooks.forEach(Runnable::run);
			destroyables.forEach(IDestroyable::destroy);
			
			if(window != null) window.destroy();
			threadPool.shutdown();
			
			glfwTerminate();
		}
		
		CONSOLE.save();
	}
	
	public static double frameStart()
	{
		return frameStart;
	}
	
	public static double partialTick()
	{
		return partialTick;
	}
	
	public static ExecutorService threadPool()
	{
		return threadPool;
	}
	
	public static Window window()
	{
		return window;
	}
	
	public static boolean shuttingDown()
	{
		return shuttingDown.getAcquire();
	}
	
	public static void addShutdownHook(Runnable hook)
	{
		shutdownHooks.add(hook);
	}
	
	public static void addDestroyable(IDestroyable destroyable)
	{
		if(!shuttingDown()) destroyables.addFirst(destroyable);
	}
	
	public static void removeDestroyable(IDestroyable destroyable)
	{
		if(!shuttingDown()) destroyables.remove(destroyable);
	}
	
	public static void addRenderHook(Runnable hook, boolean forceSchedule)
	{
		if(!forceSchedule && initialized && Thread.currentThread().getId() == renderThreadId) hook.run();
		else renderHooks.add(hook);
	}
	
	public static void addRenderHook(Runnable hook)
	{
		addRenderHook(hook, false);
	}
	
	public static boolean shouldQuit()
	{
		return window.shouldClose();
	}
	
	private static void startConsoleInput()
	{
		Thread consoleThread = new Thread(() ->
		{
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			
			while(scanner.hasNextLine())
			{
				try
				{
					CONSOLE.execute(scanner.nextLine());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}, "Stdin console thread");
		
		consoleThread.setDaemon(true);
		consoleThread.start();
	}
	
	private static void processRenderHooks()
	{
		for(Runnable hook = renderHooks.poll(); hook != null; hook = renderHooks.poll())
		{
			hook.run();
		}
	}
}
