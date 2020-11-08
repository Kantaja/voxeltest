package info.kuonteje.voxeltest.world;

public record ChunkPosition(int x, int y, int z)
{
	public int worldX()
	{
		return x * 32;
	}
	
	public int worldY()
	{
		return y * 32;
	}
	
	public int worldZ()
	{
		return z * 32;
	}
	
	public long key()
	{
		return key(x, y, z);
	}
	
	public long chunkSeed(long worldSeed)
	{
		return worldSeed ^ key();
	}
	
	@Override
	public boolean equals(Object other)
	{
		return other instanceof ChunkPosition pos && x == pos.x && y == pos.y && z == pos.z;
	}
	
	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ")";
	}
	
	public static long key(int x, int y, int z)
	{
		return ((x & 0x1FFFFFL) << 43) | ((z & 0x1FFFFFL) << 22) | (y & 0x3FFFFFL);
	}
}
