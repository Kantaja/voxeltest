package info.kuonteje.voxeltest.world;

public interface IChunkPosition
{
	int x();
	int y();
	int z();
	
	default int worldX()
	{
		return x() * 32;
	}
	
	default int worldY()
	{
		return y() * 32;
	}
	
	default int worldZ()
	{
		return z() * 32;
	}
	
	ChunkPosition immutable();
	
	default long plane()
	{
		return ((x() & 0xFFFFFFFFL) << 32) | (z() & 0xFFFFFFFFL);
	}
	
	default long chunkSeed(long worldSeed)
	{
		// equivalent to Arrays.hashCode(new int[] { x(), y(), z() })
		return worldSeed + (31L * (31L * (31L + x()) + y()) + z());
	}
}
