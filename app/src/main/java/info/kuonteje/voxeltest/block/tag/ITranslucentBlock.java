package info.kuonteje.voxeltest.block.tag;

import info.kuonteje.voxeltest.world.World;

public interface ITranslucentBlock
{
	default boolean hasTransparency(World world, int x, int y, int z)
	{
		return true;
	}
	
	default boolean blocksAdjacentFaces(World world, int x, int y, int z)
	{
		return true;
	}
}
