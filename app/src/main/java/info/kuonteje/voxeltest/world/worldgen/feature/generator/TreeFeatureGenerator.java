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

public class TreeFeatureGenerator implements IFeatureGenerator
{
	public static Config DEFAULT_CONFIG = Config.builder()
			.chance(768)
			.grass(Blocks.GRASS)
			.dirt(Blocks.DIRT)
			.log(Blocks.LOG)
			.leaves(Blocks.LEAVES)
			.build();
	
	private final Config config;
	private final BlockPredicate grassPredicate;
	
	public TreeFeatureGenerator(Config config)
	{
		this.config = config;
		this.grassPredicate = BlockPredicate.forBlock(config.grass());
	}
	
	@Override
	public void tryGenerateIn(World world, Chunk chunk)
	{
		if(chunk.empty() || chunk.pos().y() < 0) return;
		
		Random random = MiscUtil.randomGenerator(chunk.pos().chunkSeed(world.seed()));
		
		/*
		new PoissonSampling.Disk(chunk.pos().chunkSeed(world.seed()), (float)Math.sqrt(16 * 16 + 16 * 16), 12.0F, 25, (x, z) ->
		{
			x += 16.0F;
			z += 16.0F;
			
			if(x < 0.0F || x >= 32.0F || z < 0.0F || z >= 32.0F) return;
			
			for(int y = 31; y >= 0; y--)
			{
				int idx = chunk.blockIdxAt((int)x, y, (int)z);
				
				if(idx != 0)
				{
					if(idx == Blocks.GRASS.idx()) generate(world, random, chunk.pos().worldX() + (int)x, chunk.pos().worldY() + y + 1, chunk.pos().worldZ() + (int)z);
					continue;
				}
			}
		});
		 */
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 31; y >= 0; y--)
				{
					int idx = chunk.blockIdxAt(x, y, z);
					
					if(idx != 0)
					{
						if(idx == config.grass().idx() && random.nextInt(config.chance()) == 0)
							generate(world, random, chunk.pos().worldX() + x, chunk.pos().worldY() + y + 1, chunk.pos().worldZ() + z);
						
						continue;
					}
				}
			}
		}
	}
	
	private void generate(World world, Random random, int x, int y, int z)
	{
		setBlock(world, x, y - 1, z, config.dirt(), grassPredicate);
		
		for(int i = 0; i < 2; i++)
		{
			setBlock(world, x, y + i, z, config.log());
		}
		
		for(int i = 2; i < 4; i++)
		{
			if(random.nextInt(i == 2 ? 5 : 10) == 0) setBlock(world, x - 2, y + i, z - 2, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x - 1, y + i, z - 2, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x, y + i, z - 2, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x + 1, y + i, z - 2, config.leaves(), BlockPredicate.EMPTY);
			if(random.nextInt(i == 2 ? 5 : 10) == 0) setBlock(world, x + 2, y + i, z - 2, config.leaves(), BlockPredicate.EMPTY);
			
			setBlock(world, x - 2, y + i, z - 1, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x - 1, y + i, z - 1, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x, y + i, z - 1, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x + 1, y + i, z - 1, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x + 2, y + i, z - 1, config.leaves(), BlockPredicate.EMPTY);
			
			setBlock(world, x - 2, y + i, z, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x - 1, y + i, z, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x, y + i, z, config.log());
			setBlock(world, x + 1, y + i, z, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x + 2, y + i, z, config.leaves(), BlockPredicate.EMPTY);
			
			setBlock(world, x - 2, y + i, z + 1, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x - 1, y + i, z + 1, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x, y + i, z + 1, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x + 1, y + i, z + 1, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x + 2, y + i, z + 1, config.leaves(), BlockPredicate.EMPTY);
			
			if(random.nextInt(i == 2 ? 5 : 10) == 0) setBlock(world, x - 2, y + i, z + 2, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x - 1, y + i, z + 2, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x, y + i, z + 2, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x + 1, y + i, z + 2, config.leaves(), BlockPredicate.EMPTY);
			if(random.nextInt(i == 2 ? 5 : 10) == 0) setBlock(world, x + 2, y + i, z + 2, config.leaves(), BlockPredicate.EMPTY);
		}
		
		for(int i = 4; i < 6; i++)
		{
			if(random.nextInt(i == 4 ? 5 : 10) == 0) setBlock(world, x - 1, y + i, z - 1, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x, y + i, z - 1, config.leaves());
			if(random.nextInt(i == 4 ? 5 : 10) == 0) setBlock(world, x + 1, y + i, z - 1, config.leaves(), BlockPredicate.EMPTY);
			
			setBlock(world, x - 1, y + i, z, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x, y + i, z, i == 4 ? config.log() : config.leaves(), i == 4 ? null : BlockPredicate.EMPTY);
			setBlock(world, x + 1, y + i, z, config.leaves(), BlockPredicate.EMPTY);
			
			if(random.nextInt(i == 4 ? 5 : 10) == 0) setBlock(world, x - 1, y + i, z + 1, config.leaves(), BlockPredicate.EMPTY);
			setBlock(world, x, y + i, z + 1, config.leaves(), BlockPredicate.EMPTY);
			if(random.nextInt(i == 4 ? 5 : 10) == 0) setBlock(world, x + 1, y + i, z + 1, config.leaves(), BlockPredicate.EMPTY);
		}
	}
	
	public static TreeFeatureGenerator factory(GeneratorConfig rootConfig, IFeatureConfig config, long seed)
	{
		return new TreeFeatureGenerator(config == null ? DEFAULT_CONFIG : (Config)config);
	}
	
	@JsonDeserialize(builder = Config.Builder.class)
	public static class Config implements IFeatureConfig
	{
		private final int chance;
		private final Block grass;
		private final Block dirt;
		private final Block log;
		private final Block leaves;
		
		private Config(int chance, Block grass, Block dirt, Block log, Block leaves)
		{
			this.chance = chance;
			this.grass = grass;
			this.dirt = dirt;
			this.log = log;
			this.leaves = leaves;
		}
		
		@JsonProperty
		public int chance()
		{
			return chance;
		}
		
		@JsonProperty
		public Block grass()
		{
			return grass;
		}
		
		@JsonProperty
		public Block dirt()
		{
			return dirt;
		}
		
		@JsonProperty
		public Block log()
		{
			return log;
		}
		
		@JsonProperty
		public Block leaves()
		{
			return leaves;
		}
		
		public static Builder builder()
		{
			return new Builder();
		}
		
		@JsonPOJOBuilder(withPrefix = "")
		public static class Builder
		{
			private int chance = 768;
			private Block grass = Blocks.GRASS;
			private Block dirt = Blocks.DIRT;
			private Block log = Blocks.LOG;
			private Block leaves = Blocks.LEAVES;
			
			private Builder() {}
			
			@JsonProperty
			public Builder chance(int chance)
			{
				this.chance = chance;
				return this;
			}
			
			@JsonProperty
			public Builder grass(Block grass)
			{
				this.grass = grass;
				return this;
			}
			
			@JsonProperty
			public Builder dirt(Block dirt)
			{
				this.dirt = dirt;
				return this;
			}
			
			@JsonProperty
			public Builder log(Block log)
			{
				this.log = log;
				return this;
			}
			
			@JsonProperty
			public Builder leaves(Block leaves)
			{
				this.leaves = leaves;
				return this;
			}
			
			public Config build()
			{
				return new Config(chance, grass, dirt, log, leaves);
			}
		}
	}
}
