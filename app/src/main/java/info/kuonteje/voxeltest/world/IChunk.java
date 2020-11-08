package info.kuonteje.voxeltest.world;

import info.kuonteje.voxeltest.block.Block;

public sealed interface IChunk permits Chunk, PregenChunk
{
	World getWorld();
	ChunkPosition getPos();
	
	int getBlockIdx(int x, int y, int z);
	Block getBlock(int x, int y, int z);
	
	void setBlockIdx(int x, int y, int z, int idx);
	void setBlock(int x, int y, int z, Block block);
	
	default int storageIdx(int x, int y, int z)
	{
		return (x << 10) | (z << 5) | y;
	}
}
