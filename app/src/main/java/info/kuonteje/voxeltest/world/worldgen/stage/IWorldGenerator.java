package info.kuonteje.voxeltest.world.worldgen.stage;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.world.BlockPredicate;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.SetFlags;

public interface IWorldGenerator
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
	
	void processChunk(Chunk chunk);
}
