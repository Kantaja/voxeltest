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
import info.kuonteje.voxeltest.world.worldgen.config.data.SamplingConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IGenerationStageConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IWorldGenerator;

public class HeightmapBaseGenerator implements IWorldGenerator
{
	public static final Config DEFAULT_CONFIG = Config.builder()
			.heightNoise(NoiseConfig.builder()
					.type(NoiseType.OPENSIMPLEX2S)
					.xzFrequency(0.005221649073064327)
					.scale(2.0)
					.fractal(FractalConfig.builder()
							.type(FractalType.FBM)
							.octaves(16)
							.build())
					.offsetFactors(OffsetFactorConfig.builder()
							.all(256.0)
							.build())
					.build())
			.selectorNoise(NoiseConfig.builder()
					.type(NoiseType.OPENSIMPLEX2S)
					.xzFrequency(0.016709277406334877)
					.fractal(FractalConfig.builder()
							.type(FractalType.FBM)
							.octaves(8)
							.build())
					.build())
			.fillerNoise(NoiseConfig.builder()
					.type(NoiseType.OPENSIMPLEX2S)
					.xzFrequency(0.020886596292257308)
					.fractal(FractalConfig.builder()
							.type(FractalType.FBM)
							.octaves(4)
							.build())
					.build())
			.scaleNoise(NoiseConfig.builder()
					.type(NoiseType.OPENSIMPLEX2S)
					.xzFrequency(0.00146110822)
					.scale(2.0)
					.zeroToOne(true)
					.fractal(FractalConfig.builder()
							.type(FractalType.RIDGED)
							.octaves(12)
							.build())
					.offsetFactors(OffsetFactorConfig.builder()
							.all(256.0)
							.build())
					.build())
			.baseDirtHeight(3)
			.heightScale(32.0)
			.build();
	
	private final GeneratorConfig rootConfig;
	private final Config config;
	
	private final INoiseGenerator lowHeightGenerator, highHeightGenerator, selectorGenerator, fillerGenerator, scaleGenerator;
	
	private final int xzSampleFreq;
	
	public HeightmapBaseGenerator(GeneratorConfig rootConfig, Config config, long seed)
	{
		this.rootConfig = rootConfig;
		this.config = config;
		
		this.xzSampleFreq = NoiseUtil.correctSampleFrequency(config.sampling().xzFrequency(), DEFAULT_CONFIG.sampling().xzFrequency());
		
		Random seedifier = MiscUtil.randomGenerator(seed);
		
		this.lowHeightGenerator = NoiseUtil.generator(config.heightNoise(), seedifier);
		this.highHeightGenerator = NoiseUtil.generator(config.heightNoise(), seedifier);
		
		this.selectorGenerator = NoiseUtil.generator(config.selectorNoise(), seedifier);
		
		this.fillerGenerator = NoiseUtil.generator(config.fillerNoise(), seedifier);
		
		this.scaleGenerator = NoiseUtil.generator(config.scaleNoise(), seedifier);
	}
	
	@Override
	public void processChunk(Chunk chunk)
	{
		int baseX = chunk.pos().worldX();
		int baseY = chunk.pos().worldY();
		int baseZ = chunk.pos().worldZ();
		
		int xzSamples = 32 / xzSampleFreq + 1;
		
		Random random = MiscUtil.randomGenerator(chunk.chunkSeed());
		
		double[][] lowHeightNoise = new double[xzSamples][xzSamples];
		double[][] highHeightNoise = new double[xzSamples][xzSamples];
		
		double[][] selectorNoise = new double[xzSamples][xzSamples];
		
		double[][] fillerNoise = new double[xzSamples][xzSamples];
		
		double[][] scaleNoise = new double[xzSamples][xzSamples];
		
		for(int x = 0; x < xzSamples; x++)
		{
			double nx = baseX + x * xzSampleFreq;
			
			for(int z = 0; z < xzSamples; z++)
			{
				double nz = baseZ + z * xzSampleFreq;
				
				lowHeightNoise[x][z] = lowHeightGenerator.noiseXZ(nx, nz);
				highHeightNoise[x][z] = highHeightGenerator.noiseXZ(nx, nz);
				
				selectorNoise[x][z] = selectorGenerator.noiseXZ(nx, nz) * 0.05 + 0.5;
				
				fillerNoise[x][z] = fillerGenerator.noiseXZ(nx, nz) / 3.0 + config.baseDirtHeight() + random.nextDouble() * 0.25;
				
				scaleNoise[x][z] = scaleGenerator.noiseXZ(nx, nz);
			}
		}
		
		int xzShift = MathUtil.floorLog2(xzSampleFreq);
		int xzMask = xzSampleFreq - 1;
		double xzDiv = xzSampleFreq;
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				int dirtHeight = (int)NoiseUtil.interpNoise(fillerNoise, x, z, xzShift, xzMask, xzDiv);
				
				double low = NoiseUtil.interpNoise(lowHeightNoise, x, z, xzShift, xzMask, xzDiv);
				double high = NoiseUtil.interpNoise(highHeightNoise, x, z, xzShift, xzMask, xzDiv);
				double selector = NoiseUtil.interpNoise(selectorNoise, x, z, xzShift, xzMask, xzDiv);
				
				double scale = NoiseUtil.interpNoise(scaleNoise, x, z, xzShift, xzMask, xzDiv);
				
				double noise = MathUtil.signedPow(MathUtil.lerpClamped(low, high, selector), scale);
				
				int height = (int)(noise * config.heightScale()) + rootConfig.water().seaLevel();
				
				for(int y = 0; y < 32 && y + baseY <= height; y++)
				{
					int wy = y + baseY;
					
					if(wy < height - dirtHeight) setBlock(chunk, x, y, z, rootConfig.terrain().base());
					else if(wy == height) setBlock(chunk, x, y, z, rootConfig.terrain().top());
					else setBlock(chunk, x, y, z, rootConfig.terrain().filler());
				}
			}
		}
	}
	
	public static HeightmapBaseGenerator factory(GeneratorConfig rootConfig, IGenerationStageConfig config, long seed)
	{
		return new HeightmapBaseGenerator(rootConfig, (Config)config, seed);
	}
	
	@JsonDeserialize(builder = Config.Builder.class)
	public static class Config implements IGenerationStageConfig
	{
		private final NoiseConfig heightNoise;
		private final NoiseConfig selectorNoise;
		private final NoiseConfig fillerNoise;
		private final NoiseConfig scaleNoise;
		private final SamplingConfig sampling;
		private final int baseDirtHeight;
		private final double heightScale;
		
		private Config(NoiseConfig heightNoise, NoiseConfig selectorNoise, NoiseConfig fillerNoise, NoiseConfig scaleNoise, SamplingConfig sampling, int baseDirtHeight, double heightScale)
		{
			this.heightNoise = heightNoise;
			this.selectorNoise = selectorNoise;
			this.fillerNoise = fillerNoise;
			this.scaleNoise = scaleNoise;
			this.sampling = sampling;
			this.baseDirtHeight = baseDirtHeight;
			this.heightScale = heightScale;
		}
		
		@JsonProperty
		public NoiseConfig heightNoise()
		{
			return heightNoise;
		}
		
		@JsonProperty
		public NoiseConfig selectorNoise()
		{
			return selectorNoise;
		}
		
		@JsonProperty
		public NoiseConfig fillerNoise()
		{
			return fillerNoise;
		}
		
		@JsonProperty
		public NoiseConfig scaleNoise()
		{
			return scaleNoise;
		}
		
		@JsonProperty
		public SamplingConfig sampling()
		{
			return sampling;
		}
		
		@JsonProperty
		public int baseDirtHeight()
		{
			return baseDirtHeight;
		}
		
		@JsonProperty
		public double heightScale()
		{
			return heightScale;
		}
		
		public static Builder builder()
		{
			return new Builder();
		}
		
		@JsonPOJOBuilder(withPrefix = "")
		public static class Builder
		{
			private NoiseConfig heightNoise;
			private NoiseConfig selectorNoise;
			private NoiseConfig fillerNoise;
			private NoiseConfig scaleNoise;
			private SamplingConfig sampling;
			private int baseDirtHeight;
			private double heightScale;
			
			private Builder() {}
			
			@JsonProperty
			public Builder heightNoise(NoiseConfig heightNoise)
			{
				this.heightNoise = heightNoise;
				return this;
			}
			
			@JsonProperty
			public Builder selectorNoise(NoiseConfig selectorNoise)
			{
				this.selectorNoise = selectorNoise;
				return this;
			}
			
			@JsonProperty
			public Builder scaleNoise(NoiseConfig scaleNoise)
			{
				this.scaleNoise = scaleNoise;
				return this;
			}
			
			@JsonProperty
			public Builder fillerNoise(NoiseConfig fillerNoise)
			{
				this.fillerNoise = fillerNoise;
				return this;
			}
			
			@JsonProperty
			public Builder sampling(SamplingConfig sampling)
			{
				this.sampling = sampling;
				return this;
			}
			
			@JsonProperty
			public Builder baseDirtHeight(int baseDirtHeight)
			{
				this.baseDirtHeight = baseDirtHeight;
				return this;
			}
			
			@JsonProperty
			public Builder heightScale(double heightScale)
			{
				this.heightScale = heightScale;
				return this;
			}
			
			public Config build()
			{
				return new Config(heightNoise, selectorNoise, fillerNoise, scaleNoise, sampling, baseDirtHeight, heightScale);
			}
		}
	}
}
