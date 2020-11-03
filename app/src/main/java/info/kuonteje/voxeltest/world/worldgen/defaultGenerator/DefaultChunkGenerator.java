package info.kuonteje.voxeltest.world.worldgen.defaultGenerator;

import info.kuonteje.repack.fastnoise.CellularDistanceFunction;
import info.kuonteje.repack.fastnoise.CellularReturnType;
import info.kuonteje.repack.fastnoise.DomainWarpType;
import info.kuonteje.repack.fastnoise.FastNoiseLite;
import info.kuonteje.repack.fastnoise.FractalType;
import info.kuonteje.repack.fastnoise.NoiseType;
import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.worldgen.IChunkProcessor;

public class DefaultChunkGenerator implements IChunkProcessor
{
	private final FastNoiseLite terrainNoise, biomeNoise;
	
	public DefaultChunkGenerator(long seed)
	{
		int terrainSeed = (int)(seed & 0xFFFFFFFF);
		int biomeSeed = (int)((seed >> 32) & 0xFFFFFFFF);
		
		System.out.println("Terrain seed: " + terrainSeed);
		System.out.println("Biome seed: " + biomeSeed);
		
		terrainNoise = new FastNoiseLite(terrainSeed);
		biomeNoise = new FastNoiseLite(biomeSeed);
		
		terrainNoise.setNoiseType(NoiseType.OPENSIMPLEX2);
		terrainNoise.setFractalType(FractalType.FBM);
		terrainNoise.setFractalOctaves(4);
		terrainNoise.setFrequency(0.006F);
		
		// TODO
		biomeNoise.setNoiseType(NoiseType.CELLULAR);
		biomeNoise.setCellularDistanceFunction(CellularDistanceFunction.HYBRID);
		biomeNoise.setCellularReturnType(CellularReturnType.CELL_VALUE);
		biomeNoise.setDomainWarpType(DomainWarpType.OPENSIMPLEX2);
		biomeNoise.setDomainWarpAmp(100.0F);
		biomeNoise.setFractalType(FractalType.DOMAIN_WARP_INDEPENDENT);
	}
	
	@Override
	public void processChunk(Chunk chunk)
	{
		double baseX = chunk.getPos().x() * 32;
		double baseY = chunk.getPos().y() * 32;
		double baseZ = chunk.getPos().z() * 32;
		
		float[][][] noise = new float[9][9][10];
		
		for(int x = 0; x < 9; x++)
		{
			for(int z = 0; z < 9; z++)
			{
				for(int y = 0; y < 10; y++)
				{
					noise[x][z][y] = terrainNoise.getNoise(baseX + x * 4, baseY + y * 4, baseZ + z * 4) * 0.5F + 0.5F;
				}
			}
		}
		
		float[] noiseColumn = new float[33];
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 33; y++)
				{
					noiseColumn[y] = interpNoise(noise, x, y, z) - (float)(baseY + y) * 0.006F;
				}
				
				for(int y = 0; y < 32; y++)
				{
					float density = noiseColumn[y];
					
					if(density > 0.5F) chunk.setBlock(x, y, z, Blocks.STONE);
					else if(density > 0.44F) chunk.setBlock(x, y, z, y >= 0 && noiseColumn[y + 1] < 0.44F ? Blocks.GRASS : Blocks.DIRT);
				}
			}
		}
	}
	
	public static float interpNoise(float[][][] noise, int x, int y, int z)
	{
		// What the hell
		int baseNx = x >> 2;
					int baseNy = y >> 2;
				int baseNz = z >> 2;
		
		float xd = (x & 0x3) / 4.0F;
		float yd = (y & 0x3) / 4.0F;
		float zd = (z & 0x3) / 4.0F;
		
		float c000 = noise[baseNx][baseNz][baseNy];
		float c001 = noise[baseNx][baseNz + 1][baseNy];
		float c010 = noise[baseNx][baseNz][baseNy + 1];
		float c011 = noise[baseNx][baseNz + 1][baseNy + 1];
		float c100 = noise[baseNx + 1][baseNz][baseNy];
		float c101 = noise[baseNx + 1][baseNz + 1][baseNy];
		float c110 = noise[baseNx + 1][baseNz][baseNy + 1];
		float c111 = noise[baseNx + 1][baseNz + 1][baseNy + 1];
		
		float c00 = MathUtil.lerp(c000, c100, xd);
		float c01 = MathUtil.lerp(c001, c101, xd);
		float c10 = MathUtil.lerp(c010, c110, xd);
		float c11 = MathUtil.lerp(c011, c111, xd);
		
		float c0 = MathUtil.lerp(c00, c10, yd);
		float c1 = MathUtil.lerp(c01, c11, yd);
		
		return MathUtil.lerp(c0, c1, zd);
	}
}
