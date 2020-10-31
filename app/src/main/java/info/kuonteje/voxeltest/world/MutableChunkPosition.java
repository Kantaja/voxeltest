package info.kuonteje.voxeltest.world;

public class MutableChunkPosition implements IChunkPosition
{
	private int x, y, z;
	
	public MutableChunkPosition(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public MutableChunkPosition set(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		
		return this;
	}
	
	@Override
	public int x()
	{
		return x;
	}
	
	@Override
	public int y()
	{
		return y;
	}
	
	@Override
	public int z()
	{
		return z;
	}
	
	@Override
	public ChunkPosition immutable()
	{
		return new ChunkPosition(x, y, z);
	}
	
	@Override
	public boolean equals(Object other)
	{
		return other instanceof IChunkPosition pos && x == pos.x() && y == pos.y() && z == pos.z();
	}
}
