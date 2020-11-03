package info.kuonteje.voxeltest.world.worldgen;

import static org.lwjgl.glfw.GLFW.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.ChunkPosition;
import info.kuonteje.voxeltest.world.IChunkProvider;

public class TimingChunkProvider implements IChunkProvider
{
	private final IChunkProvider impl;
	
	private DoubleAdder totalTime = new DoubleAdder();
	private AtomicInteger totalChunks = new AtomicInteger(0);
	
	public TimingChunkProvider(IChunkProvider impl)
	{
		this.impl = impl;
	}
	
	@Override
	public Chunk getChunk(ChunkPosition pos)
	{
		double start = glfwGetTime();
		
		Chunk chunk = impl.getChunk(pos);
		
		totalTime.add(glfwGetTime() - start);
		totalChunks.getAndIncrement();
		
		return chunk;
	}
	
	public double totalTime()
	{
		return totalTime.doubleValue();
	}
	
	public int totalChunks()
	{
		return totalChunks.get();
	}
}
