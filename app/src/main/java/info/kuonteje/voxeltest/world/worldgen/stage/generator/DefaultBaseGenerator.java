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

public class DefaultBaseGenerator implements IWorldGenerator
{
	public static final Config DEFAULT_CONFIG = Config.builder()
			.densityNoise(NoiseConfig.builder()
					.type(NoiseType.OPENSIMPLEX2S)
					.xzFrequency(0.0026)
					.yFrequency(0.0052)
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
					.frequency(0.0167)
					.fractal(FractalConfig.builder()
							.type(FractalType.FBM)
							.octaves(8)
							.build())
					.offsetFactors(OffsetFactorConfig.builder()
							.all(256.0)
							.build())
					.build())
			.flattenNoise(NoiseConfig.builder()
					.type(NoiseType.OPENSIMPLEX2S)
					.xzFrequency(0.001)
					.scale(8.0)
					.fractal(FractalConfig.builder()
							.type(FractalType.FBM)
							.octaves(8)
							.build())
					.offsetFactors(OffsetFactorConfig.builder()
							.all(256.0)
							.build())
					.build())
			.fillerNoise(NoiseConfig.builder()
					.type(NoiseType.OPENSIMPLEX2S)
					.xzFrequency(0.02)
					.fractal(FractalConfig.builder()
							.type(FractalType.FBM)
							.octaves(4)
							.build())
					.build())
			.sampling(SamplingConfig.builder()
					.xzFrequency(8)
					.yFrequency(4)
					.build())
			.baseDirtHeight(3)
			.falloffOffset(3)
			.falloffScale(0.006)
			.flattenOffset(1)
			.flattenScale(3.0)
			.build();
	
	private final GeneratorConfig rootConfig;
	private final Config config;
	
	private final INoiseGenerator lowGenerator, highGenerator;
	private final INoiseGenerator selectorGenerator;
	private final INoiseGenerator flattenGenerator;
	private final INoiseGenerator fillerGenerator;
	
	private final int xzSampleFreq, ySampleFreq;
	
	public DefaultBaseGenerator(GeneratorConfig rootConfig, Config config, long seed)
	{
		this.rootConfig = rootConfig;
		this.config = config;
		
		this.xzSampleFreq = NoiseUtil.correctSampleFrequency(config.sampling().xzFrequency(), DEFAULT_CONFIG.sampling().xzFrequency());
		this. ySampleFreq = NoiseUtil.correctSampleFrequency(config.sampling(). yFrequency(), DEFAULT_CONFIG.sampling(). yFrequency());
		
		Random seedifier = MiscUtil.randomGenerator(seed);
		
		this.lowGenerator = NoiseUtil.generator(config.densityNoise, seedifier);
		this.highGenerator = NoiseUtil.generator(config.densityNoise, seedifier);
		
		this.selectorGenerator = NoiseUtil.generator(config.selectorNoise, seedifier);
		
		this.flattenGenerator = NoiseUtil.generator(config.flattenNoise, seedifier);
		
		this.fillerGenerator = NoiseUtil.generator(config.fillerNoise, seedifier);
	}
	
	// TODO maybe turn down the spice a bit
	@Override
	public void processChunk(Chunk chunk)
	{
		final int maxDirtHeight = (int)(fillerGenerator.max() / 3.0 + config.baseDirtHeight() + 0.25);
		
		int baseX = chunk.pos().worldX();
		int baseY = chunk.pos().worldY();
		int baseZ = chunk.pos().worldZ();
		
		int xzSamples = 32 / xzSampleFreq + 1;
		int  ySamples = MathUtil.ceilDiv(32 + maxDirtHeight + 2, ySampleFreq) + 1;
		
		Random columnRandom = MiscUtil.randomGenerator(chunk.columnSeed());
		
		double[][][] lowNoise = new double[xzSamples][xzSamples][ySamples];
		double[][][] highNoise = new double[xzSamples][xzSamples][ySamples];
		
		double[][][] selectorNoise = new double[xzSamples][xzSamples][ySamples];
		
		double[][] flattenNoise = new double[xzSamples][xzSamples];
		
		double[][] fillerNoise = new double[xzSamples][xzSamples];
		
		for(int x = 0; x < xzSamples; x++)
		{
			double nx = baseX + x * xzSampleFreq;
			
			for(int z = 0; z < xzSamples; z++)
			{
				double nz = baseZ + z * xzSampleFreq;
				
				flattenNoise[x][z] = flattenGenerator.noiseXZ(nx, nz);
				
				fillerNoise[x][z] = fillerGenerator.noiseXZ(nx, nz) / 3.0 + columnRandom.nextDouble() * 0.25;
				
				for(int y = 0; y < ySamples; y++)
				{
					double ny = baseY + y * ySampleFreq;
					
					lowNoise[x][z][y] = lowGenerator.noise(nx, ny, nz);
					highNoise[x][z][y] = highGenerator.noise(nx, ny, nz);
					
					selectorNoise[x][z][y] = selectorGenerator.noise(nx, ny, nz) * 0.05 + 0.1;
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
				int flatten = rootConfig.water().seaLevel() + (int)Math.round(NoiseUtil.interpNoise(flattenNoise, x, z, xzShift, xzMask, xzDiv));
				
				int dirtHeight = (int)(NoiseUtil.interpNoise(fillerNoise, x, z, xzShift, xzMask, xzDiv) + 1.0 + config.baseDirtHeight());
				
				int dirt = 0;
				
				for(int y = 31 + maxDirtHeight + 2; y >= 0; y--)
				{
					int wy = y + baseY;
					
					double falloff = (wy - config.falloffOffset() - rootConfig.water().seaLevel()) * config.falloffScale();
					if(wy < flatten - config.flattenOffset()) falloff *= config.flattenScale();
					
					double low = NoiseUtil.interpNoise(lowNoise, x, y, z, xzShift, yShift, xzMask, yMask, xzDiv, yDiv);
					double high = NoiseUtil.interpNoise(highNoise, x, y, z, xzShift, yShift, xzMask, yMask, xzDiv, yDiv);
					
					double selector = NoiseUtil.interpNoise(selectorNoise, x, y, z, xzShift, yShift, xzMask, yMask, xzDiv, yDiv);
					
					double noise = MathUtil.lerpClamped(low, high, selector) - falloff;
					
					if(noise <= 0.0) dirt = dirtHeight;
					else if(noise > 0.0)
					{
						if(dirt > 0)
						{
							if(y < 32) setBlock(chunk, x, y, z, dirt == dirtHeight ? rootConfig.terrain().top() : rootConfig.terrain().filler());
							dirt--;
						}
						else if(y < 32) setBlock(chunk, x, y, z, rootConfig.terrain().base());
					}
				}
			}
		}
	}
	
	public static DefaultBaseGenerator factory(GeneratorConfig rootConfig, IGenerationStageConfig config, long seed)
	{
		return new DefaultBaseGenerator(rootConfig, (Config)config, seed);
	}
	
	@JsonDeserialize(builder = Config.Builder.class)
	public static class Config implements IGenerationStageConfig
	{
		private final NoiseConfig densityNoise;
		private final NoiseConfig selectorNoise;
		private final NoiseConfig flattenNoise;
		private final NoiseConfig fillerNoise;
		private final SamplingConfig sampling;
		private final int baseDirtHeight;
		private final int falloffOffset;
		private final double falloffScale;
		private final int flattenOffset;
		private final double flattenScale;
		
		private Config(NoiseConfig densityNoise, NoiseConfig selectorNoise, NoiseConfig flattenNoise, NoiseConfig fillerNoise, SamplingConfig sampling, int baseDirtHeight, int falloffOffset, double falloffScale, int flattenOffset, double flattenScale)
		{
			this.densityNoise = densityNoise;
			this.selectorNoise = selectorNoise;
			this.flattenNoise = flattenNoise;
			this.fillerNoise = fillerNoise;
			this.sampling = sampling;
			this.baseDirtHeight = baseDirtHeight;
			this.falloffOffset = falloffOffset;
			this.falloffScale = falloffScale;
			this.flattenOffset = flattenOffset;
			this.flattenScale = flattenScale;
		}
		
		@JsonProperty
		public NoiseConfig densityNoise()
		{
			return densityNoise;
		}
		
		@JsonProperty
		public NoiseConfig selectorNoise()
		{
			return selectorNoise;
		}
		
		@JsonProperty
		public NoiseConfig flattenNoise()
		{
			return flattenNoise;
		}
		
		@JsonProperty
		public NoiseConfig fillerNoise()
		{
			return fillerNoise;
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
		public int falloffOffset()
		{
			return falloffOffset;
		}
		
		@JsonProperty
		public double falloffScale()
		{
			return falloffScale;
		}
		
		@JsonProperty
		public int flattenOffset()
		{
			return flattenOffset;
		}
		
		@JsonProperty
		public double flattenScale()
		{
			return flattenScale;
		}
		
		public static Builder builder()
		{
			return new Builder();
		}
		
		@JsonPOJOBuilder(withPrefix = "")
		public static class Builder
		{
			private NoiseConfig densityNoise;
			private NoiseConfig selectorNoise;
			private NoiseConfig flattenNoise;
			private NoiseConfig fillerNoise;
			private SamplingConfig sampling;
			private int baseDirtHeight;
			private int falloffOffset;
			private double falloffScale;
			private int flattenOffset;
			private double flattenScale;
			
			private Builder() {}
			
			@JsonProperty
			public Builder densityNoise(NoiseConfig densityNoise)
			{
				this.densityNoise = densityNoise;
				return this;
			}
			
			@JsonProperty
			public Builder selectorNoise(NoiseConfig selectorNoise)
			{
				this.selectorNoise = selectorNoise;
				return this;
			}
			
			@JsonProperty
			public Builder flattenNoise(NoiseConfig flattenNoise)
			{
				this.flattenNoise = flattenNoise;
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
			
			@JsonProperty
			public Builder flattenOffset(int flattenOffset)
			{
				this.flattenOffset = flattenOffset;
				return this;
			}
			
			@JsonProperty
			public Builder flattenScale(double flattenScale)
			{
				this.flattenScale = flattenScale;
				return this;
			}
			
			public Config build()
			{
				return new Config(densityNoise, selectorNoise, flattenNoise, fillerNoise, sampling, baseDirtHeight, falloffOffset, falloffScale, flattenOffset, flattenScale);
			}
		}
	}
}
