package info.kuonteje.voxeltest;

import static org.lwjgl.glfw.GLFW.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joml.Vector3d;

import info.kuonteje.voxeltest.console.Console;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.RegistryManager;
import info.kuonteje.voxeltest.render.Camera;
import info.kuonteje.voxeltest.render.Renderer;
import info.kuonteje.voxeltest.render.Window;
import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.world.ChunkPosition;
import info.kuonteje.voxeltest.world.World;

public class VoxelTest
{
	public static final String DEFAULT_DOMAIN = "voxeltest";
	
	public static final Path CFG_PATH = Paths.get("config");
	
	public static final Console CONSOLE = new Console(CFG_PATH);
	
	public static final CvarI64 vsyncInterval = CONSOLE.cvars().getCvarI64C("vsync_interval", 1L, Cvar.Flags.CONFIG, v -> Math.max(v, 0L), (n, o) -> addRenderHook(() -> getWindow().setSwapInterval((int)n)));
	
	private static final List<Runnable> shutdownHooks = Collections.synchronizedList(new ArrayList<>());
	
	private static final Queue<Runnable> renderHooks = new ConcurrentLinkedQueue<>();
	private static long renderThreadId = -1;
	
	private static ExecutorService threadPool;
	
	private static Window window;
	private static Renderer renderer;
	private static Camera camera;
	
	private static World world;
	
	private static volatile double frameStart, partialTick;
	
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
			
			threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			
			window = new Window("VoxelTest", 1366, 768);
			renderer = new Renderer(CONSOLE, window, 1366, 768);
			
			DefaultRegistries.init();
			
			RegistryManager.freezeAll();
			
			camera = new Camera(window::getKey, window::getMouse);
			
			window.setSwapInterval((int)vsyncInterval.get());
			
			Ticks.addTickHandler(world = new World());
			addShutdownHook(world::destroy);
			
			Ticks.init();
			
			startConsoleInput();
			
			Thread.currentThread().setName("Render thread");
			renderThreadId = Thread.currentThread().getId();
			
			window.captureMouse();
			
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
					
					int chunkX = MathUtil.fastFloor(pos.x / 32.0);
					int chunkY = MathUtil.fastFloor(pos.y / 32.0);
					int chunkZ = MathUtil.fastFloor(pos.z / 32.0);
					
					window.setTitle("VoxelTest (" + (Math.round(frames / accum * 100.0) / 100.0) + " fps, " + (Math.round(accum / frames * 100000.0) / 100.0) + " ms), camera position ("
							+ MathUtil.fastFloor(pos.x) + ", " + MathUtil.fastFloor(pos.y) + ", " + MathUtil.fastFloor(pos.z) + "), in chunk ("
							+ chunkX + ", " + chunkY + ", " + chunkZ + ") - " + world.getChunkStatus(new ChunkPosition(chunkX, chunkY, chunkZ)).toString());
					
					frames = 0;
					accum = 0.0;
				}
				
				frameStart = glfwGetTime();
				partialTick = ((frameStart - Ticks.lastTickTime()) - Ticks.getTickLength()) * Ticks.getTickrate();
				
				camera.frame(delta);
				renderer.beginFrame(camera);
				
				processRenderHooks();
				
				world.render(renderer);
				
				renderer.completeFrame();
				
				frames++;
				window.update();
			}
		}
		finally
		{
			shutdownHooks.forEach(Runnable::run);
			window.destroy();
			threadPool.shutdown();
			glfwTerminate();
		}
		
		CONSOLE.save();
	}
	
	public static double getFrameStart()
	{
		return frameStart;
	}
	
	public static double getPartialTick()
	{
		return partialTick;
	}
	
	public static ExecutorService getThreadPool()
	{
		return threadPool;
	}
	
	public static Window getWindow()
	{
		return window;
	}
	
	public static Renderer getRenderer()
	{
		return renderer;
	}
	
	public static void addShutdownHook(Runnable hook)
	{
		shutdownHooks.add(hook);
	}
	
	public static void addRenderHook(Runnable hook, boolean forceSchedule)
	{
		if(!forceSchedule && Thread.currentThread().getId() == renderThreadId) hook.run();
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
