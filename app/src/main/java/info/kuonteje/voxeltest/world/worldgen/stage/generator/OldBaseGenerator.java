package info.kuonteje.voxeltest.world.worldgen.stage.generator;

import com.fasterxml.jackson.annotation.JsonProperty;

import info.kuonteje.voxeltest.repack.fastnoise.FractalType;
import info.kuonteje.voxeltest.repack.fastnoise.NoiseType;
import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.util.noise.INoiseGenerator;
import info.kuonteje.voxeltest.util.noise.NoiseUtil;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.worldgen.config.data.DensityConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.FractalConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.GeneratorConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.NoiseConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.SampledNoiseConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.SamplingConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IGenerationStageConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IWorldGenerator;

public class OldBaseGenerator implements IWorldGenerator
{
	public static final Config DEFAULT_CONFIG = Config.builder()
			.terrainNoise(SampledNoiseConfig.builder()
					.noise(NoiseConfig.builder()
							.type(NoiseType.OPENSIMPLEX2)
							.frequency(0.006)
							.zeroToOne(true)
							.fractal(FractalConfig.builder()
									.type(FractalType.FBM)
									.octaves(4)
									.build())
							.build())
					.sampling(SamplingConfig.builder()
							.xzFrequency(4)
							.yFrequency(4)
							.build())
					.build())
			.terrainDensity(DensityConfig.builder()
					.yOffset(0)
					.yScale(0.006)
					.power(1.0)
					.invert(false)
					.build())
			.baseThreshold(0.5)
			.solidThreshold(0.44)
			.build();
	
	private final GeneratorConfig rootConfig;
	private final Config config;
	
	private final INoiseGenerator terrainNoise;
	
	private final int xzSampleFreq, ySampleFreq;
	
	public OldBaseGenerator(GeneratorConfig rootConfig, Config config, long seed)
	{
		this.rootConfig = rootConfig;
		this.config = config;
		
		this.xzSampleFreq = NoiseUtil.correctSampleFrequency(config.terrainNoise().sampling().xzFrequency(), DEFAULT_CONFIG.terrainNoise().sampling().xzFrequency());
		this. ySampleFreq = NoiseUtil.correctSampleFrequency(config.terrainNoise().sampling(). yFrequency(), DEFAULT_CONFIG.terrainNoise().sampling(). yFrequency());
		
		int terrainSeed = (int)(seed & 0xFFFFFFFF);
		
		terrainNoise = NoiseUtil.generator(config.terrainNoise.noise(), terrainSeed, 0L);
	}
	
	@Override
	public void processChunk(Chunk chunk)
	{
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
				for(int y = 0; y < ySamples; y++)
				{
					noise[x][z][y] = terrainNoise.noise(baseX + x * xzSampleFreq, baseY + y * ySampleFreq, baseZ + z * xzSampleFreq);
				}
			}
		}
		
		int xzShift = MathUtil.floorLog2(xzSampleFreq);
		int  yShift = MathUtil.floorLog2( ySampleFreq);
		
		int xzMask = xzSampleFreq - 1;
		int  yMask = ySampleFreq - 1;
		
		double xzDiv = xzSampleFreq;
		double  yDiv =  ySampleFreq;
		
		double[] noiseColumn = new double[33];
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 33; y++)
				{
					double scale = (config.density.yScale() > 0.0 ? (baseY + y + config.density.yOffset() - rootConfig.water().seaLevel()) : (rootConfig.water().seaLevel() - (baseY + y + config.density.yOffset()))) * config.density.yScale();
					noiseColumn[y] = NoiseUtil.interpNoise(noise, x, y, z, xzShift, yShift, xzMask, yMask, xzDiv, yDiv) - scale;
					
					if(config.density.power() > 0.0 && config.density.power() != 1.0) noiseColumn[y] = Math.pow(noiseColumn[y], config.density.power());
					if(config.density.invert()) noiseColumn[y] = 1.0 - noiseColumn[y];
				}
				
				for(int y = 0; y < 32; y++)
				{
					double density = noiseColumn[y];
					
					if(density > config.baseThreshold()) setBlock(chunk, x, y, z, rootConfig.terrain().base());
					else if(density > config.solidThreshold()) setBlock(chunk, x, y, z, noiseColumn[y + 1] < config.solidThreshold() ? rootConfig.terrain().top() : rootConfig.terrain().filler());
				}
			}
		}
	}
	
	public static OldBaseGenerator factory(GeneratorConfig rootConfig, IGenerationStageConfig config, long seed)
	{
		return new OldBaseGenerator(rootConfig, (Config)config, seed);
	}
	
	public static class Config implements IGenerationStageConfig
	{
		private final SampledNoiseConfig terrainNoise;
		private final DensityConfig density;
		private final double baseThreshold;
		private final double solidThreshold;
		
		private Config(SampledNoiseConfig terrainNoise, DensityConfig density, double baseThreshold, double solidThreshold)
		{
			this.terrainNoise = terrainNoise;
			this.density = density;
			this.baseThreshold = baseThreshold;
			this.solidThreshold = solidThreshold;
		}
		
		@JsonProperty
		public SampledNoiseConfig terrainNoise()
		{
			return terrainNoise;
		}
		
		@JsonProperty
		public DensityConfig density()
		{
			return density;
		}
		
		@JsonProperty
		public double baseThreshold()
		{
			return baseThreshold;
		}
		
		@JsonProperty
		public double solidThreshold()
		{
			return solidThreshold;
		}
		
		public static Builder builder()
		{
			return new Builder();
		}
		
		public static class Builder
		{
			private SampledNoiseConfig terrainNoise;
			private DensityConfig terrainDensity;
			private double baseThreshold;
			private double solidThreshold;
			
			private Builder() {}
			
			@JsonProperty
			public Builder terrainNoise(SampledNoiseConfig terrainNoise)
			{
				this.terrainNoise = terrainNoise;
				return this;
			}
			
			@JsonProperty
			public Builder terrainDensity(DensityConfig terrainDensity)
			{
				this.terrainDensity = terrainDensity;
				return this;
			}
			
			@JsonProperty
			public Builder baseThreshold(double baseThreshold)
			{
				this.baseThreshold = baseThreshold;
				return this;
			}
			
			@JsonProperty
			public Builder solidThreshold(double solidThreshold)
			{
				this.solidThreshold = solidThreshold;
				return this;
			}
			
			public Config build()
			{
				return new Config(terrainNoise, terrainDensity, baseThreshold, solidThreshold);
			}
		}
	}
}
