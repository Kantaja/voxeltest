package info.kuonteje.voxeltest.world;

import it.unimi.dsi.fastutil.HashCommon;

public record ChunkPosition(int x, int y, int z)
{
	public static final int MAX_XZ = 1048575;
	public static final int MIN_XZ = -1048576;
	
	public static final int MAX_Y = 2097151;
	public static final int MIN_Y = -2097152;
	
	public static final ChunkPosition ZERO = new ChunkPosition(0, 0, 0);
	
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
		return HashCommon.mix(worldSeed ^ key());
	}
	
	public long columnSeed(long worldSeed)
	{
		return HashCommon.mix(((x & 0x1FFFFFL) << 21) | (z & 0x1FFFFFL));
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
	
	public static boolean isValid(int x, int y, int z)
	{
		return x >= MIN_XZ && x <= MAX_XZ
				&& z >= MIN_XZ && z <= MAX_XZ
				&& y >= MIN_Y && y <= MAX_Y;
	}
	
	public static long key(int x, int y, int z)
	{
		return ((x & 0x1FFFFFL) << 43) | ((z & 0x1FFFFFL) << 22) | (y & 0x3FFFFFL);
	}
}
