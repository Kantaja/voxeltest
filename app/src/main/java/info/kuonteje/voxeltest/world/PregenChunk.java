package info.kuonteje.voxeltest.world;

import java.util.Optional;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public final class PregenChunk implements IChunk
{
	private record PregenOp(int x, int y, int z, Block block, int flags, BlockPredicate predicate) {}
	
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
	public World world()
	{
		return world;
	}
	
	@Override
	public ChunkPosition pos()
	{
		return pos;
	}
	
	@Override
	public void setBlockIdx(int x, int y, int z, int idx, int flags, BlockPredicate predicate)
	{
		synchronized(lock)
		{
			if(applied == null) ops.enqueue(new PregenOp(x, y, z, DefaultRegistries.BLOCKS.byIdxRaw(idx), flags & SetFlags.ALL, predicate));
			else applied.setBlockIdx(x, y, z, idx, flags, predicate);
		}
	}
	
	@Override
	public void setBlock(int x, int y, int z, Block block, int flags, BlockPredicate predicate)
	{
		synchronized(lock)
		{
			if(applied == null) ops.enqueue(new PregenOp(x, y, z, block, flags & SetFlags.ALL, predicate));
			else applied.setBlock(x, y, z, block, flags, predicate);
		}
	}
	
	@Override
	public int blockIdxAtInternal(int x, int y, int z)
	{
		synchronized(lock)
		{
			return applied == null ? 0 : applied.blockIdxAt(x, y, z);
		}
	}
	
	@Override
	public Optional<Block> blockAt(int x, int y, int z)
	{
		synchronized(lock)
		{
			return applied == null ? Optional.empty() : applied.blockAt(x, y, z);
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
				chunk.setBlock(op.x, op.y, op.z, op.block, op.flags, op.predicate);
			}
			
			ops = null;
			applied = chunk;
		}
	}
}
