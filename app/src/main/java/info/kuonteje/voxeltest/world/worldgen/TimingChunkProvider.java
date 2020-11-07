package info.kuonteje.voxeltest.world.worldgen;

import info.kuonteje.voxeltest.util.ConcurrentTimer;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.ChunkPosition;
import info.kuonteje.voxeltest.world.IChunkProvider;

public class TimingChunkProvider implements IChunkProvider
{
	private final IChunkProvider impl;
	private final ConcurrentTimer timer;
	
	public TimingChunkProvider(IChunkProvider impl)
	{
		this.impl = impl;
		timer = new ConcurrentTimer();
	}
	
	@Override
	public Chunk getChunk(ChunkPosition pos)
	{
		return timer.time(() -> impl.getChunk(pos));
	}
	
	public double totalTime()
	{
		return timer.totalTime();
	}
	
	public int totalChunks()
	{
		return timer.totalOps();
	}
}
