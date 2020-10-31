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
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.RegistryManager;
import info.kuonteje.voxeltest.data.objects.BlockModels;
import info.kuonteje.voxeltest.data.objects.BlockTextures;
import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.render.Camera;
import info.kuonteje.voxeltest.render.Renderer;
import info.kuonteje.voxeltest.render.Window;
import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.world.ChunkPosition;
import info.kuonteje.voxeltest.world.World;

public class VoxelTest
{
	public static final Path CFG_PATH = Paths.get("config");
	
	public static final Console CONSOLE = new Console(CFG_PATH);
	
	private static final List<Runnable> shutdownHooks = Collections.synchronizedList(new ArrayList<>());
	
	private static final Queue<Runnable> renderHooks = new ConcurrentLinkedQueue<>();
	
	private static ExecutorService threadPool;
	
	private static Window window;
	private static Renderer renderer;
	private static Camera camera;
	
	private static World world;
	
	private static volatile double frameStart, partialTick;
	
	public static void main(String[] args)
	{
		//try { Thread.sleep(5000L); } catch(Exception e) {}
		
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
			
			threadPool = Executors.newFixedThreadPool(16);
			
			window = new Window("VoxelTest", 1366, 768);
			
			DefaultRegistries.init();
			
			Blocks.init();
			BlockTextures.init();
			BlockModels.init();
			// Biomes.init();
			
			RegistryManager.freezeAll();
			
			renderer = new Renderer(CONSOLE, 1366, 768);
			
			window.setResizeCallback(renderer::resize);
			
			camera = new Camera(window::getKey, window::getMouse);
			
			window.setSwapInterval(1);
			
			Ticks.addTickHandler(world = new World());
			addShutdownHook(world::destroy);
			
			Ticks.init();
			
			startConsoleInput();
			
			window.captureMouse();
			
			double previousTime = frameStart = glfwGetTime();
			double delta;
			
			int frames = 0;
			double accum = 0.0;
			
			while(!window.shouldClose())
			{
				delta = frameStart - previousTime;
				previousTime = frameStart;
				
				//if(delta > 0.0083) System.out.println("Long frame delta (" + (Math.round(delta * 100000.0) / 100.0) + " ms)");
				
				accum += delta;
				
				if(accum >= 1.0)
				{
					Vector3d pos = camera.getPosition(new Vector3d());
					
					int chunkX = MathUtil.fastFloor(pos.x / 32.0);
					int chunkY = MathUtil.fastFloor(pos.y / 32.0);
					int chunkZ = MathUtil.fastFloor(pos.z / 32.0);
					
					window.setTitle("VoxelTest (" + (Math.round(frames / accum * 100.0) / 100.0) + " fps), camera position ("
							+ MathUtil.fastFloor(pos.x) + ", " + MathUtil.fastFloor(pos.y) + ", " + MathUtil.fastFloor(pos.z) + "), in chunk ("
							+ chunkX + ", " + chunkY + ", " + chunkZ + ") - " + (world.getChunk(new ChunkPosition(chunkX, chunkY, chunkZ), false) == null ? "not loaded" : "loaded"));
					
					frames = 0;
					accum = 0.0;
				}
				
				frameStart = glfwGetTime();
				partialTick = ((frameStart - Ticks.lastTickTime()) - Ticks.getTickLength()) * Ticks.getTickrate();
				
				camera.frame(delta);
				renderer.beginFrame(camera);
				
				processRenderHooks();
				
				world.render(renderer);
				
				renderer.endFrame();
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
	
	public static void addRenderHook(Runnable hook)
	{
		renderHooks.add(hook);
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
		});
		
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
