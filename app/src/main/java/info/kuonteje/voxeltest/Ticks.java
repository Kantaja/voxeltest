package info.kuonteje.voxeltest;

import static org.lwjgl.glfw.GLFW.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleConsumer;

import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarF64;
import info.kuonteje.voxeltest.util.MathUtil;

public class Ticks
{
	public static interface ITickHandler
	{
		default void initTickHandler() {}
		
		String name();
		
		default void tick(double delta) {}
		default void tick(double delta, int tickNumber) { tick(delta); }
	}
	
	private static AtomicBoolean recalc = new AtomicBoolean(true);
	
	public static final CvarF64 eTickrate = VoxelTest.CONSOLE.cvars().cvarF64("e_tickrate", 32.0, Cvar.Flags.CONFIG, null, (n, o) -> recalc.setRelease(true));
	
	private static volatile double tickrate;
	private static volatile double targetTickLength;
	private static volatile long targetTickLengthNanos;
	
	private static void recalc()
	{
		tickrate = eTickrate.get();
		targetTickLength = 1.0 / tickrate;
		targetTickLengthNanos = (long)(targetTickLength * 1000000000.0);
		
		System.out.println("Target tick length " + ms(targetTickLength) + " ms for " + tickrate + " tps");
	}
	
	private static Thread theThread;
	
	private static List<ITickHandler> handlers = new CopyOnWriteArrayList<>();
	
	private static int ticks = 0;
	
	private static volatile double lastTickTime = 0.0;
	
	private static int totalHandlers = 0;
	private static int simpleHandlers = 0;
	
	public static void addTickHandler(ITickHandler handler)
	{
		System.out.println("Adding tick handler of type " + handler.getClass().getName() + " as " + handler.name());
		
		handlers.add(handler);
		
		handler.initTickHandler();
		
		totalHandlers++;
	}
	
	public static int addSimpleTickHandler(DoubleConsumer handler)
	{
		addTickHandler(new ITickHandler()
		{
			private final String name = "anon" + simpleHandlers++;
			
			@Override
			public String name()
			{
				return name;
			}
			
			@Override
			public void tick(double delta)
			{
				handler.accept(delta);
			}
		});
		
		return totalHandlers;
	}
	
	public static void removeTickHandler(int handler)
	{
		if(handlers.remove(handler) != null) totalHandlers--;
	}
	
	public static void removeTickHandler(ITickHandler handler)
	{
		if(handlers.remove(handler)) totalHandlers--;
	}
	
	public static void init()
	{
		startTimerHack();
		
		theThread = new Thread(() ->
		{
			//Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			
			String longestName = "";
			
			double time = glfwGetTime(), previousTime = time, delta, subtime, longestTime = 0.0, handlerTime, tickTime = targetTickLength;
			
			long targetNanoTime = 0;
			
			ITickHandler handler;
			
			while(!VoxelTest.shouldQuit())
			{
				if(recalc.compareAndExchangeRelease(true, false)) recalc();
				
				targetNanoTime = System.nanoTime() + targetTickLengthNanos;
				
				delta = time - previousTime;
				previousTime = time;
				
				time = glfwGetTime();
				
				longestName = null;
				longestTime = 0;
				
				for(int i = 0; i < handlers.size(); i++)
				{
					subtime = glfwGetTime();
					handler = handlers.get(i);
					
					handler.tick(delta, ticks);
					
					handlerTime = glfwGetTime() - subtime;
					
					if(handlerTime > longestTime)
					{
						longestTime = handlerTime;
						longestName = handler.name();
					}
				}
				
				ticks++;
				
				tickTime = glfwGetTime() - time;
				
				if(tickTime > targetTickLength) System.err.println("Tick " + ticks + " took " + ms(tickTime) + " ms, maximum tick length "
						+ ms(targetTickLength) + " ms, longest handler '" + longestName + "' (" + ms(longestTime) + " ms)");
				
				if(tickTime < targetTickLength) sleepUntil(targetNanoTime);
				
				lastTickTime = time;
			}
		}, "Tick thread");
		
		theThread.start();
	}
	
	private static void sleepUntil(long nanoTime)
	{
		long millis = (nanoTime - System.nanoTime()) / 1000000L - 1L;
		
		if(millis > 0) try { Thread.sleep(millis); } catch(Exception e) {}
		
		while(System.nanoTime() - nanoTime < 0L) {}
	}
	
	private static double ms(double seconds)
	{
		return MathUtil.roundDisplay(seconds * 1000.0);
	}
	
	public static double tickrate()
	{
		return tickrate;
	}
	
	public static double tickLength()
	{
		return targetTickLength;
	}
	
	public static int secondsToTicks(double seconds)
	{
		return (int)Math.ceil(seconds * tickrate);
	}
	
	public static double lastTickTime()
	{
		return lastTickTime;
	}
	
	public static double timeSinceLastTick()
	{
		return glfwGetTime() - lastTickTime;
	}
	
	private static void startTimerHack()
	{
		Thread hackThread = new Thread(() ->
		{
			while(!VoxelTest.shouldQuit())
			{
				try { Thread.sleep(2147483647L); } catch(Exception e) {}
			}
		}, "Timer hack thread :)");
		
		hackThread.setDaemon(true);
		hackThread.start();
	}
}
