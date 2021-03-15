package info.kuonteje.voxeltest.world;

import java.util.Optional;

import info.kuonteje.voxeltest.block.Block;

public final class EmptyChunk implements IChunk
{
	private final World world;
	
	EmptyChunk(World world)
	{
		this.world = world;
	}
	
	@Override
	public World world()
	{
		return world;
	}
	
	@Override
	public ChunkPosition pos()
	{
		return ChunkPosition.ZERO;
	}
	
	@Override
	public int blockIdxAtInternal(int x, int y, int z)
	{
		return 0;
	}
	
	@Override
	public Optional<Block> blockAt(int x, int y, int z)
	{
		return Optional.empty();
	}
	
	@Override
	public void setBlockIdx(int x, int y, int z, int idx, int flags, BlockPredicate predicate)
	{
		//
	}
	
	@Override
	public void setBlock(int x, int y, int z, Block block, int flags, BlockPredicate predicate)
	{
		//
	}
	
	@Override
	public boolean hasTransparency(int x, int y, int z)
	{
		return true;
	}
}
