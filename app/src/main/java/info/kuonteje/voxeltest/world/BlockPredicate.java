package info.kuonteje.voxeltest.world;

import info.kuonteje.voxeltest.block.Block;

public interface BlockPredicate
{
	public static final BlockPredicate TRUE = (world, idx, x, y, z) -> true;
	public static final BlockPredicate FALSE = (world, idx, x, y, z) -> false;
	
	public static final BlockPredicate EMPTY = (world, idx, x, y, z) -> idx == 0;
	public static final BlockPredicate NOT_EMPTY = (world, idx, x, y, z) -> idx != 0;
	
	public static BlockPredicate forBlock(int blockIdx)
	{
		return (world, idx, x, y, z) -> blockIdx == idx;
	}
	
	public static BlockPredicate forBlock(Block block)
	{
		return forBlock(block.idx());
	}
	
	boolean test(World world, int idx, int x, int y, int z);
	
	default BlockPredicate negate()
	{
		return (world, idx, x, y, z) -> !test(world, idx, x, y, z);
	}
}
