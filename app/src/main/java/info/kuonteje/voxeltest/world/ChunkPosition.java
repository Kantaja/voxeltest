package info.kuonteje.voxeltest.world;

public record ChunkPosition(int x, int y, int z) implements IChunkPosition
{
	@Override
	public ChunkPosition immutable()
	{
		return this;
	}
	
	@Override
	public boolean equals(Object other)
	{
		return other instanceof IChunkPosition pos && x == pos.x() && y == pos.y() && z == pos.z();
	}
	
	@Override
	public String toString()
	{
		return "(" + x() + ", " + y() + ", " + z() + ")";
	}
}
