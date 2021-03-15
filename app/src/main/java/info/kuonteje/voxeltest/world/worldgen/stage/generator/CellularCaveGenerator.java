package info.kuonteje.voxeltest.world.worldgen.stage.generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import info.kuonteje.voxeltest.repack.fastnoise.CellularDistanceFunction;
import info.kuonteje.voxeltest.repack.fastnoise.CellularReturnType;
import info.kuonteje.voxeltest.repack.fastnoise.NoiseType;
import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.util.noise.INoiseGenerator;
import info.kuonteje.voxeltest.util.noise.NoiseUtil;
import info.kuonteje.voxeltest.world.BlockPredicate;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.worldgen.config.data.CellularNoiseConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.GeneratorConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.NoiseConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.SamplingConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IGenerationStageConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IWorldGenerator;

public class CellularCaveGenerator implements IWorldGenerator
{
	public static final Config DEFAULT_CONFIG = Config.builder()
			.carveNoise(NoiseConfig.builder()
					.type(NoiseType.CELLULAR)
					.frequency(0.016)
					.zeroToOne(true)
					.cellular(CellularNoiseConfig.builder()
							.distanceFunction(CellularDistanceFunction.EUCLIDEAN_SQ)
							.returnType(CellularReturnType.DISTANCE3_DIV)
							.build())
					.build())
			.warpNoise(NoiseConfig.builder()
					.type(NoiseType.PERLIN)
					.frequency(0.05)
					.build())
			.sampling(SamplingConfig.builder()
					.xzFrequency(4)
					.yFrequency(1)
					.build())
			.threshold(0.41)
			.warpScale(8.0)
			.build();
	
	private final GeneratorConfig baseConfig;
	private final Config config;
	
	private final int xzSampleFreq, ySampleFreq;
	
	private final BlockPredicate fillerPredicate;
	
	private final INoiseGenerator carveNoise, warpNoise;
	
	public CellularCaveGenerator(GeneratorConfig baseConfig, Config config, long seed)
	{
		// scramble with lcg
		seed = seed * 2862933555777941757L + 3037000493L;
		
		this.baseConfig = baseConfig;
		this.config = config;
		
		this.xzSampleFreq = NoiseUtil.correctSampleFrequency(config.sampling().xzFrequency(), DEFAULT_CONFIG.sampling().xzFrequency());
		this. ySampleFreq = NoiseUtil.correctSampleFrequency(config.sampling(). yFrequency(), DEFAULT_CONFIG.sampling(). yFrequency());
		
		this.fillerPredicate = BlockPredicate.forBlock(baseConfig.terrain().filler());
		
		int caveSeed = (int)(seed & 0xFFFFFFFF);
		int caveWarpSeed = (int)((seed >> 32) & 0xFFFFFFFF);
		
		carveNoise = NoiseUtil.generator(this.config.carveNoise(), caveSeed, 0L);
		warpNoise = NoiseUtil.generator(this.config.warpNoise(), caveWarpSeed, 0L);
	}
	
	// TODO either rewrite or scrap
	@Override
	public void processChunk(Chunk chunk)
	{
		if(chunk.empty()) return;
		
		int baseX = chunk.pos().worldX();
		int baseY = chunk.pos().worldY();
		int baseZ = chunk.pos().worldZ();
		
		int xzSamples = 32 / xzSampleFreq + 1;
		int  ySamples = 32 /  ySampleFreq + 2;
		
		double[][][] noise = new double[xzSamples][xzSamples][ySamples];
		
		for(int x = 0; x < xzSamples; x++)
		{
			for(int z = 0; z < xzSamples; z++)
			{
				for(int y = ySamples - 1; y >= -2; y--)
				{
					double wx = baseX + x * xzSampleFreq;
					double wy = baseY + y *  ySampleFreq;
					double wz = baseZ + z * xzSampleFreq;
					
					// offsets are random primes
					double warpX = warpNoise.noiseXZ(wx + 109.0, wz + 73.0) * config.warpScale();
					double warpY = warpNoise.noiseXZ(wx - 31.0, wz + 53.0) * config.warpScale();
					double warpZ = warpNoise.noiseXZ(wx - 229.0, wz - 181.0) * config.warpScale();
					
					double c = carveNoise.noise(wx + warpX, wy * 2.0 + warpY, wz + warpZ);
					
					if(c > config.threshold())
					{
						if(y >= 0)
						{
							if(x > 0) noise[x - 1][z][y] = MathUtil.lerp(noise[x - 1][z][y], c, 0.2);
							if(z > 0) noise[x][z - 1][y] = MathUtil.lerp(noise[x][z - 1][y], c, 0.2);
						}
						
						if(y < ySamples - 1)
						{
							if(y >= -1)
							{
								double up = noise[x][z][y + 1];
								if(c > up) noise[x][z][y + 1] = MathUtil.lerp(up, c, 0.8);
							}
							
							if(y < ySamples - 2)
							{
								double up = noise[x][z][y + 2];
								if(c > up) noise[x][z][y + 2] = MathUtil.lerp(up, c, 0.35);
							}
						}
					}
					
					if(y >= 0) noise[x][z][y] = c;
				}
			}
		}
		
		int xzShift = MathUtil.floorLog2(xzSampleFreq);
		int  yShift = MathUtil.floorLog2( ySampleFreq);
		
		int xzMask = xzSampleFreq - 1;
		int  yMask =  ySampleFreq - 1;
		
		double xzDiv = xzSampleFreq;
		double  yDiv =  ySampleFreq;
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					int wy = baseY + y;
					
					if(chunk.blockIdxAt(x, y, z) != 0)
					{
						final int liquid = baseConfig.water().liquid().idx();
						
						double c = NoiseUtil.interpNoise(noise, x, y, z, xzShift, yShift, xzMask, yMask, xzDiv, yDiv);
						
						// TODO chunk boundaries
						if(c > config.threshold() && chunk.blockIdxAt(x, y, z) != liquid
								&& chunk.blockIdxAt(x + 1, y, z) != liquid && chunk.blockIdxAt(x - 1, y, z) != liquid
								&& chunk.blockIdxAt(x, y, z + 1) != liquid && chunk.blockIdxAt(x, y, z - 1) != liquid
								&& chunk.blockIdxAt(x, y + 1, z) != liquid && chunk.blockIdxAt(x, y - 1, z) != liquid)
						{
							setBlock(chunk, x, y, z, null);
							if(y > 0 && wy > baseConfig.water().seaLevel() + 1) setBlock(chunk, x, y - 1, z, baseConfig.terrain().top(), fillerPredicate);
						}
					}
				}
			}
		}
	}
	
	public static CellularCaveGenerator factory(GeneratorConfig baseConfig, IGenerationStageConfig config, long seed)
	{
		return new CellularCaveGenerator(baseConfig, config == null ? DEFAULT_CONFIG : (Config)config, seed);
	}
	
	@JsonDeserialize(builder = Config.Builder.class)
	public static class Config implements IGenerationStageConfig
	{
		private final NoiseConfig carveNoise;
		private final NoiseConfig warpNoise;
		private final SamplingConfig sampling;
		private final double threshold;
		private final double warpScale;
		
		private Config(NoiseConfig carveNoise, NoiseConfig warpNoise, SamplingConfig sampling, double threshold, double warpScale)
		{
			this.carveNoise = carveNoise;
			this.warpNoise = warpNoise;
			this.sampling = sampling;
			this.threshold = threshold;
			this.warpScale = warpScale;
		}
		
		@JsonProperty
		public NoiseConfig carveNoise()
		{
			return carveNoise;
		}
		
		@JsonProperty
		public NoiseConfig warpNoise()
		{
			return warpNoise;
		}
		
		@JsonProperty
		public SamplingConfig sampling()
		{
			return sampling;
		}
		
		@JsonProperty
		public double threshold()
		{
			return threshold;
		}
		
		@JsonProperty
		public double warpScale()
		{
			return warpScale;
		}
		
		public static Builder builder()
		{
			return new Builder();
		}
		
		@JsonPOJOBuilder(withPrefix = "")
		public static class Builder
		{
			private NoiseConfig carveNoise;
			private NoiseConfig warpNoise;
			private SamplingConfig sampling;
			private double threshold = 0.41;
			private double warpScale = 8.0;
			
			private Builder() {}
			
			@JsonProperty
			public Builder carveNoise(NoiseConfig carveNoise)
			{
				this.carveNoise = carveNoise;
				return this;
			}
			
			@JsonProperty
			public Builder warpNoise(NoiseConfig warpNoise)
			{
				this.warpNoise = warpNoise;
				return this;
			}
			
			@JsonProperty
			public Builder sampling(SamplingConfig sampling)
			{
				this.sampling = sampling;
				return this;
			}
			
			@JsonProperty
			public Builder threshold(double threshold)
			{
				this.threshold = threshold;
				return this;
			}
			
			@JsonProperty
			public Builder warpScale(double warpScale)
			{
				this.warpScale = warpScale;
				return this;
			}
			
			public Config build()
			{
				return new Config(carveNoise, warpNoise, sampling, threshold, warpScale);
			}
		}
	}
}
