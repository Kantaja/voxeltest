package info.kuonteje.voxeltest.util;

import static org.lwjgl.glfw.GLFW.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Supplier;

public class ConcurrentTimer
{
	private DoubleAdder totalTime = new DoubleAdder();
	private AtomicInteger totalOps = new AtomicInteger(0);
	
	public void time(Runnable op)
	{
		double start = glfwGetTime();
		
		op.run();
		
		totalTime.add(glfwGetTime() - start);
		totalOps.getAndIncrement();
	}
	
	public <T> T time(Supplier<T> op)
	{
		double start = glfwGetTime();
		
		T t = op.get();
		
		totalTime.add(glfwGetTime() - start);
		totalOps.getAndIncrement();
		
		return t;
	}
	
	public double totalTime()
	{
		return totalTime.doubleValue();
	}
	
	public int totalOps()
	{
		return totalOps.get();
	}
}
