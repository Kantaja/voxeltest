package info.kuonteje.voxeltest.world.worldgen.feature.generator;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.util.MiscUtil;
import info.kuonteje.voxeltest.world.BlockPredicate;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.World;
import info.kuonteje.voxeltest.world.worldgen.config.data.GeneratorConfig;
import info.kuonteje.voxeltest.world.worldgen.feature.IFeatureConfig;

public class BlockReplacementFeatureGenerator implements IFeatureGenerator
{
	public static final Config EMERALD_ORE_CONFIG = Config.builder()
			.base(Blocks.STONE)
			.replacement(Blocks.EMERALD_ORE)
			.chance(0.0004F)
			.maxY(-48)
			.build();
	
	private final Config config;
	private final BlockPredicate basePredicate;
	
	public BlockReplacementFeatureGenerator(Config config)
	{
		this.config = config;
		this.basePredicate = BlockPredicate.forBlock(config.base);
	}
	
	@Override
	public void tryGenerateIn(World world, Chunk chunk)
	{
		int baseY = chunk.pos().worldY();
		
		if(baseY > config.maxY()) return;
		if(baseY + 31 < config.minY()) return;
		
		Random random = MiscUtil.randomGenerator(chunk.pos().chunkSeed(world.seed()));
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					if(baseY + y >= config.minY() && baseY + y <= config.maxY() && random.nextFloat() < config.chance()) setBlock(chunk, x, y, z, config.replacement(), basePredicate);
				}
			}
		}
	}
	
	public static BlockReplacementFeatureGenerator factory(GeneratorConfig rootConfig, IFeatureConfig config, long seed)
	{
		return new BlockReplacementFeatureGenerator(config == null ? EMERALD_ORE_CONFIG : (Config)config);
	}
	
	@JsonDeserialize(builder = Config.Builder.class)
	public static class Config implements IFeatureConfig
	{
		private final Block base;
		private final Block replacement;
		private final double chance;
		private final int minY;
		private final int maxY;
		
		private Config(Block base, Block replacement, double chance, int minY, int maxY)
		{
			this.base = base;
			this.replacement = replacement;
			this.chance = chance;
			this.minY = minY;
			this.maxY = maxY;
		}
		
		@JsonProperty
		public Block base()
		{
			return base;
		}
		
		@JsonProperty
		public Block replacement()
		{
			return replacement;
		}
		
		@JsonProperty
		public double chance()
		{
			return chance;
		}
		
		@JsonProperty
		public int minY()
		{
			return minY;
		}
		
		@JsonProperty
		public int maxY()
		{
			return maxY;
		}
		
		public static Builder builder()
		{
			return new Builder();
		}
		
		@JsonPOJOBuilder(withPrefix = "")
		public static class Builder
		{
			private Block base;
			private Block replacement;
			private double chance;
			private int minY = Integer.MIN_VALUE;
			private int maxY = Integer.MAX_VALUE;
			
			private Builder() {}
			
			@JsonProperty
			public Builder base(Block base)
			{
				this.base = base;
				return this;
			}
			
			@JsonProperty
			public Builder replacement(Block replacement)
			{
				this.replacement = replacement;
				return this;
			}
			
			@JsonProperty
			public Builder chance(double chance)
			{
				this.chance = chance;
				return this;
			}
			
			@JsonProperty
			public Builder minY(int minY)
			{
				this.minY = minY;
				return this;
			}
			
			@JsonProperty
			public Builder maxY(int maxY)
			{
				this.maxY = maxY;
				return this;
			}
			
			public Config build()
			{
				return new Config(base, replacement, chance, minY, maxY);
			}
		}
	}
}
