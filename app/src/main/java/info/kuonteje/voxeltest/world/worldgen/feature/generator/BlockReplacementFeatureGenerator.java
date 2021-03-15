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
	
	public BlockReplacementFeatureGenerator(IFeatureConfig config)
	{
		this((Config)config);
	}
	
	@Override
	public void tryGenerateIn(World world, Chunk chunk)
	{
		int baseY = chunk.pos().worldY();
		
		if(baseY > config.maxY()) return;
		
		Random random = MiscUtil.randomGenerator(chunk.pos().chunkSeed(world.seed()));
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					if(baseY + y <= config.maxY() && random.nextFloat() < config.chance()) setBlock(chunk, x, y, z, config.replacement(), basePredicate);
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
		private final float chance;
		private final int maxY;
		
		private Config(Block base, Block replacement, float chance, int maxY)
		{
			this.base = base;
			this.replacement = replacement;
			this.chance = chance;
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
		public float chance()
		{
			return chance;
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
			private float chance;
			private int maxY;
			
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
			public Builder chance(float chance)
			{
				this.chance = chance;
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
				return new Config(base, replacement, chance, maxY);
			}
		}
	}
}
