package info.kuonteje.voxeltest.world;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public final class PregenChunk implements IChunk
{
	private record PregenOp(int x, int y, int z, Block block) {}
	
	private final World world;
	private final ChunkPosition pos;
	
	private final Object lock = new Object();
	
	private PriorityQueue<PregenOp> ops = new ObjectArrayFIFOQueue<>();
	private Chunk applied = null;
	
	PregenChunk(World world, ChunkPosition pos)
	{
		this.world = world;
		this.pos = pos;
	}
	
	@Override
	public World getWorld()
	{
		return world;
	}
	
	@Override
	public ChunkPosition getPos()
	{
		return pos;
	}
	
	@Override
	public void setBlockIdx(int x, int y, int z, int idx)
	{
		synchronized(lock)
		{
			if(applied == null) ops.enqueue(new PregenOp(x, y, z, DefaultRegistries.BLOCKS.getByIdx(idx)));
			else applied.setBlockIdx(x, y, z, idx);
		}
	}
	
	@Override
	public void setBlock(int x, int y, int z, Block block)
	{
		synchronized(lock)
		{
			if(applied == null) ops.enqueue(new PregenOp(x, y, z, block));
			else applied.setBlock(x, y, z, block);
		}
	}
	
	@Override
	public int getBlockIdxInternal(int x, int y, int z)
	{
		synchronized(lock)
		{
			return applied == null ? 0 : applied.getBlockIdx(x, y, z);
		}
	}
	
	@Override
	public Block getBlock(int x, int y, int z)
	{
		synchronized(lock)
		{
			return applied == null ? null : applied.getBlock(x, y, z);
		}
	}
	
	@Override
	public boolean hasTransparency(int x, int y, int z)
	{
		synchronized(lock)
		{
			return applied == null || applied.hasTransparency(x, y, z);
		}
	}
	
	void apply(Chunk chunk)
	{
		synchronized(lock)
		{
			while(!ops.isEmpty())
			{
				PregenOp op = ops.dequeue();
				chunk.setBlock(op.x, op.y, op.z, op.block);
			}
			
			ops = null;
			applied = chunk;
		}
	}
}
