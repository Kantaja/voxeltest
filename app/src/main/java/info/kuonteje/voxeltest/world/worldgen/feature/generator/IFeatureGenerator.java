package info.kuonteje.voxeltest.world.worldgen.feature.generator;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.world.BlockPredicate;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.SetFlags;
import info.kuonteje.voxeltest.world.World;

public interface IFeatureGenerator
{
	default void setBlockIdx(Chunk chunk, int x, int y, int z, int idx, BlockPredicate predicate)
	{
		chunk.setBlockIdx(x, y, z, idx, SetFlags.NO_UPDATE, predicate);
	}
	
	default void setBlock(Chunk chunk, int x, int y, int z, Block block, BlockPredicate predicate)
	{
		chunk.setBlock(x, y, z, block, SetFlags.NO_UPDATE, predicate);
	}
	
	default void setBlockIdx(Chunk chunk, int x, int y, int z, int idx)
	{
		chunk.setBlockIdx(x, y, z, idx, SetFlags.NO_UPDATE);
	}
	
	default void setBlock(Chunk chunk, int x, int y, int z, Block block)
	{
		chunk.setBlock(x, y, z, block, SetFlags.NO_UPDATE);
	}
	
	default void setBlockIdx(World world, int x, int y, int z, int idx, BlockPredicate predicate)
	{
		world.setBlockIdx(x, y, z, idx, SetFlags.NO_UPDATE, predicate);
	}
	
	default void setBlock(World world, int x, int y, int z, Block block, BlockPredicate predicate)
	{
		world.setBlock(x, y, z, block, SetFlags.NO_UPDATE, predicate);
	}
	
	default void setBlockIdx(World world, int x, int y, int z, int idx)
	{
		world.setBlockIdx(x, y, z, idx, SetFlags.NO_UPDATE);
	}
	
	default void setBlock(World world, int x, int y, int z, Block block)
	{
		world.setBlock(x, y, z, block, SetFlags.NO_UPDATE);
	}
	
	void tryGenerateIn(World world, Chunk chunk);
}
