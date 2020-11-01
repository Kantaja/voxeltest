package info.kuonteje.voxeltest.world.worldgen.caves;

import info.kuonteje.repack.fastnoise.CellularDistanceFunction;
import info.kuonteje.repack.fastnoise.CellularReturnType;
import info.kuonteje.repack.fastnoise.FastNoiseLite;
import info.kuonteje.repack.fastnoise.NoiseType;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.worldgen.DefaultWorldGenerator;

public class DefaultCaveGenerator implements ICaveGenerator
{
	private static final int WATER = DefaultRegistries.BLOCKS.getIdx(Blocks.WATER);
	
	private static final float THRESHOLD = 0.82F;
	
	private final FastNoiseLite caveNoise;
	private final FastNoiseLite warpNoise;
	
	public DefaultCaveGenerator(long seed)
	{
		seed = seed * 2862933555777941757L + 3037000493L;
		
		int caveSeed = (int)(seed & 0xFFFFFFFF);
		int caveWarpSeed = (int)((seed >> 32) & 0xFFFFFFFF);
		
		System.out.println("Cave seed: " + caveSeed);
		System.out.println("Cave warp seed: " + caveWarpSeed);
		
		caveNoise = new FastNoiseLite(caveSeed);
		caveNoise.setNoiseType(NoiseType.CELLULAR);
		caveNoise.setCellularReturnType(CellularReturnType.DISTANCE2_MUL);
		caveNoise.setCellularDistanceFunction(CellularDistanceFunction.HYBRID);
		caveNoise.setFrequency(0.03F);
		
		warpNoise = new FastNoiseLite(caveWarpSeed);
		warpNoise.setNoiseType(NoiseType.OPENSIMPLEX2S);
		warpNoise.setFrequency(0.05F);
	}
	
	@Override
	public void generateCaves(Chunk chunk)
	{
		if(chunk.empty()) return;
		
		double baseX = chunk.getPos().x() * 32;
		double baseY = chunk.getPos().y() * 32;
		double baseZ = chunk.getPos().z() * 32;
		
		float[][][] noise = new float[9][9][10];
		
		for(int x = 0; x < 9; x++)
		{
			for(int z = 0; z < 9; z++)
			{
				for(int y = 9; y >= -2; y--)
				{
					double realX = baseX + x * 4.0;
					double realY = baseY + y * 4.0;
					double realZ = baseZ + z * 4.0;
					
					double warpX = warpNoise.getNoise(realX + 109.0, realZ + 73.0) * 8.0;
					double warpY = warpNoise.getNoise(realX - 31.0, realZ + 53.0) * 8.0;
					double warpZ = warpNoise.getNoise(realX - 229.0, realZ - 181.0) * 8.0;
					
					float c = caveNoise.getNoise(realX + warpX, realY * 2.5 + warpY, realZ + warpZ) * 0.5F + 0.5F;
					
					if(c > THRESHOLD)
					{
						if(y >= 0)
						{
							if(x > 0) noise[x - 1][z][y] = MathUtil.lerp(noise[x - 1][z][y], c, 0.2F);
							if(z > 0) noise[x][z - 1][y] = MathUtil.lerp(noise[x][z - 1][y], c, 0.2F);
						}
						
						if(y < 9)
						{
							if(y >= -1)
							{
								float up = noise[x][z][y + 1];
								if(c > up) noise[x][z][y + 1] = MathUtil.lerp(up, c, 0.8F);
							}
							
							if(y < 8)
							{
								float up = noise[x][z][y + 2];
								if(c > up) noise[x][z][y + 2] = MathUtil.lerp(up, c, 0.35F);
							}
						}
					}
					
					if(y >= 0 && y < 11) noise[x][z][y] = c;
				}
			}
		}
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					if(chunk.getBlockIdx(x, y, z) != 0)
					{
						// TODO chunk boundaries
						if(DefaultWorldGenerator.interpNoise(noise, x, y, z) > THRESHOLD && chunk.getBlockIdx(x, y, z) != WATER
								&& chunk.getBlockIdx(x + 1, y, z) != WATER && chunk.getBlockIdx(x - 1, y, z) != WATER
								&& chunk.getBlockIdx(x, y, z + 1) != WATER && chunk.getBlockIdx(x, y, z - 1) != WATER
								&& chunk.getBlockIdx(x, y + 1, z) != WATER && chunk.getBlockIdx(x, y - 1, z) != WATER) chunk.setBlock(x, y, z, null);
					}
				}
			}
		}
	}
}