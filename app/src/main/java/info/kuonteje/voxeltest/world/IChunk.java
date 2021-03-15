package info.kuonteje.voxeltest.world;

import java.util.Optional;

import info.kuonteje.voxeltest.block.Block;

public sealed interface IChunk permits Chunk, PregenChunk, EmptyChunk
{
	World world();
	ChunkPosition pos();
	
	@Deprecated
	int blockIdxAtInternal(int x, int y, int z);
	
	default int blockIdxAt(int x, int y, int z)
	{
		return (x < 0 || x >= 32 || y < 0 || y >= 32 || z < 0 || z >= 32) ? 0 : blockIdxAtInternal(x, y, z);
	}
	
	Optional<Block> blockAt(int x, int y, int z);
	
	void setBlockIdx(int x, int y, int z, int idx, int flags, BlockPredicate predicate);
	void setBlock(int x, int y, int z, Block block, int flags, BlockPredicate predicate);
	
	default void setBlockIdx(int x, int y, int z, int idx, int flags)
	{
		setBlockIdx(x, y, z, idx, flags, null);
	}
	
	default void setBlock(int x, int y, int z, Block block, int flags)
	{
		setBlock(x, y, z, block, flags, null);
	}
	
	boolean hasTransparency(int x, int y, int z);
	
	default int storageIdx(int x, int y, int z)
	{
		return (x << 10) | (z << 5) | y;
	}
}
