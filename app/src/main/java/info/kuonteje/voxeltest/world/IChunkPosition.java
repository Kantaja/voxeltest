package info.kuonteje.voxeltest.world;

public interface IChunkPosition
{
	int x();
	int y();
	int z();
	
	ChunkPosition immutable();
	
	default long plane()
	{
		return ((x() & 0xFFFFFFFFL) << 32) | (z() & 0xFFFFFFFFL);
	}
}
