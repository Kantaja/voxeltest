package info.kuonteje.voxeltest.world.worldgen.stage.generator;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import info.kuonteje.voxeltest.repack.fastnoise.FractalType;
import info.kuonteje.voxeltest.repack.fastnoise.NoiseType;
import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.util.MiscUtil;
import info.kuonteje.voxeltest.util.noise.INoiseGenerator;
import info.kuonteje.voxeltest.util.noise.NoiseUtil;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.worldgen.config.data.FractalConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.GeneratorConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.NoiseConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.OffsetFactorConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.SampledNoiseConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.SamplingConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IGenerationStageConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IWorldGenerator;

public class GradientCaveGenerator implements IWorldGenerator
{
	public static final Config DEFAULT_CONFIG = Config.builder()
			.noise(SampledNoiseConfig.builder()
					.noise(NoiseConfig.builder()
							.type(NoiseType.OPENSIMPLEX2)
							.xzFrequency(0.00355694247)
							.yFrequency(0.00711388494)
							.zeroToOne(true)
							.fractal(FractalConfig.builder()
									.type(FractalType.FBM)
									.octaves(8)
									.build())
							.offsetFactors(OffsetFactorConfig.builder()
									.all(1024.0)
									.build())
							.build())
					.sampling(SamplingConfig.builder()
							.xzFrequency(4)
							.yFrequency(4)
							.build())
					.build())
			.threshold(0.7)
			.falloffOffset(64)
			.falloffScale(0.006)
			.build();
	
	private final GeneratorConfig rootConfig;
	private final Config config;
	
	private final int xzSampleFreq, ySampleFreq;
	
	private final INoiseGenerator generator;
	
	public GradientCaveGenerator(GeneratorConfig rootConfig, Config config, long seed)
	{
		this.rootConfig = rootConfig;
		this.config = config;
		
		this.xzSampleFreq = NoiseUtil.correctSampleFrequency(config.noise.sampling().xzFrequency(), DEFAULT_CONFIG.noise.sampling().xzFrequency());
		this. ySampleFreq = NoiseUtil.correctSampleFrequency(config.noise.sampling(). yFrequency(), DEFAULT_CONFIG.noise.sampling(). yFrequency());
		
		Random seedifier = MiscUtil.randomGenerator(seed + 3L);
		
		this.generator = NoiseUtil.generator(config.noise().noise(), seedifier);
	}
	
	@Override
	public void processChunk(Chunk chunk)
	{
		if(chunk.empty()) return;
		
		int baseX = chunk.pos().worldX();
		int baseY = chunk.pos().worldY();
		int baseZ = chunk.pos().worldZ();
		
		int xzSamples = 32 / xzSampleFreq + 1;
		int  ySamples = 32 /  ySampleFreq + 1;
		
		double[][][] noise = new double[xzSamples][xzSamples][ySamples];
		
		for(int x = 0; x < xzSamples; x++)
		{
			double nx = baseX + x * xzSampleFreq;
			
			for(int z = 0; z < xzSamples; z++)
			{
				double nz = baseZ + z * xzSampleFreq;
				
				for(int y = 0; y < ySamples; y++)
				{
					double ny = baseY + y * ySampleFreq;
					
					noise[x][z][y] = generator.noise(nx, ny, nz);
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
					double carve = NoiseUtil.interpNoise(noise, x, y, z, xzShift, yShift, xzMask, yMask, xzDiv, yDiv) - Math.max(0.0, (baseY + y + config.falloffOffset() - rootConfig.water().seaLevel()) * config.falloffScale());
					if(carve > config.threshold()) setBlock(chunk, x, y, z, null);
				}
			}
		}
	}
	
	public static GradientCaveGenerator factory(GeneratorConfig rootConfig, IGenerationStageConfig config, long seed)
	{
		return new GradientCaveGenerator(rootConfig, config == null ? DEFAULT_CONFIG : (Config)config, seed);
	}
	
	@JsonDeserialize(builder = Config.Builder.class)
	public static class Config implements IGenerationStageConfig
	{
		private final SampledNoiseConfig noise;
		private final double threshold;
		private final int falloffOffset;
		private final double falloffScale;
		
		private Config(SampledNoiseConfig noise, double threshold, int falloffOffset, double falloffScale)
		{
			this.noise = noise;
			this.threshold = threshold;
			this.falloffOffset = falloffOffset;
			this.falloffScale = falloffScale;
		}
		
		@JsonProperty
		public SampledNoiseConfig noise()
		{
			return noise;
		}
		
		@JsonProperty
		public double threshold()
		{
			return threshold;
		}
		
		@JsonProperty
		public int falloffOffset()
		{
			return falloffOffset;
		}
		
		@JsonProperty
		public double falloffScale()
		{
			return falloffScale;
		}
		
		public static Builder builder()
		{
			return new Builder();
		}
		
		@JsonPOJOBuilder(withPrefix = "")
		public static class Builder
		{
			private SampledNoiseConfig noise;
			private double threshold;
			private int falloffOffset;
			private double falloffScale;
			
			private Builder() {}
			
			@JsonProperty
			public Builder noise(SampledNoiseConfig noise)
			{
				this.noise = noise;
				return this;
			}
			
			@JsonProperty
			public Builder threshold(double threshold)
			{
				this.threshold = threshold;
				return this;
			}
			
			@JsonProperty
			public Builder falloffOffset(int falloffOffset)
			{
				this.falloffOffset = falloffOffset;
				return this;
			}
			
			@JsonProperty
			public Builder falloffScale(double falloffScale)
			{
				this.falloffScale = falloffScale;
				return this;
			}
			
			public Config build()
			{
				return new Config(noise, threshold, falloffOffset, falloffScale);
			}
		}
	}
}
