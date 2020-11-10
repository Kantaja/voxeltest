package info.kuonteje.voxeltest.world;

import info.kuonteje.voxeltest.block.Block;

public final class EmptyChunk implements IChunk
{
	private final World world;
	
	EmptyChunk(World world)
	{
		this.world = world;
	}
	
	@Override
	public World getWorld()
	{
		return world;
	}
	
	@Override
	public ChunkPosition getPos()
	{
		return ChunkPosition.ZERO;
	}
	
	@Override
	public int getBlockIdxInternal(int x, int y, int z)
	{
		return 0;
	}
	
	@Override
	public Block getBlock(int x, int y, int z)
	{
		return null;
	}
	
	@Override
	public void setBlockIdx(int x, int y, int z, int idx)
	{
		//
	}
	
	@Override
	public void setBlock(int x, int y, int z, Block block)
	{
		//
	}
	
	@Override
	public boolean hasTransparency(int x, int y, int z)
	{
		return true;
	}
}
