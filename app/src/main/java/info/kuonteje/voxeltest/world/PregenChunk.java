package info.kuonteje.voxeltest.world;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public final class PregenChunk implements IChunk
{
	private record PregenOp(int x, int y, int z, int blockIdx) {}
	
	private final World world;
	private final IChunkPosition pos;
	
	private final Object lock = new Object();
	
	private PriorityQueue<PregenOp> ops = new ObjectArrayFIFOQueue<>();
	private Chunk applied = null;
	
	PregenChunk(World world, IChunkPosition pos)
	{
		this.world = world;
		this.pos = pos.immutable();
	}
	
	@Override
	public World getWorld()
	{
		return world;
	}
	
	@Override
	public IChunkPosition getPos()
	{
		return pos;
	}
	
	@Override
	public void setBlockIdx(int x, int y, int z, int idx)
	{
		synchronized(lock)
		{
			if(applied == null) ops.enqueue(new PregenOp(x, y, z, idx));
			else applied.setBlockIdx(x, y, z, idx);
		}
	}
	
	@Override
	public void setBlock(int x, int y, int z, Block block)
	{
		synchronized(lock)
		{
			if(applied == null) ops.enqueue(new PregenOp(x, y, z, DefaultRegistries.BLOCKS.getIdx(block)));
			else applied.setBlock(x, y, z, block);
		}
	}
	
	@Override
	public int getBlockIdx(int x, int y, int z)
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
	
	void apply(Chunk chunk)
	{
		synchronized(lock)
		{
			while(!ops.isEmpty())
			{
				PregenOp op = ops.dequeue();
				chunk.setBlockIdx(op.x, op.y, op.z, op.blockIdx);
			}
			
			ops = null;
			applied = chunk;
		}
	}
}
