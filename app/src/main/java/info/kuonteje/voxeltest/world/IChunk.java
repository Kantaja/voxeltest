package info.kuonteje.voxeltest.world;

import info.kuonteje.voxeltest.block.Block;

public sealed interface IChunk permits Chunk, PregenChunk, EmptyChunk
{
	World getWorld();
	ChunkPosition getPos();
	
	@Deprecated
	int getBlockIdxInternal(int x, int y, int z);
	
	default int getBlockIdx(int x, int y, int z)
	{
		return (x < 0 || x >= 32 || y < 0 || y >= 32 || z < 0 || z >= 32) ? 0 : getBlockIdxInternal(x, y, z);
	}
	
	Block getBlock(int x, int y, int z);
	
	void setBlockIdx(int x, int y, int z, int idx);
	void setBlock(int x, int y, int z, Block block);
	
	boolean hasTransparency(int x, int y, int z);
	
	default int storageIdx(int x, int y, int z)
	{
		return (x << 10) | (z << 5) | y;
	}
}
