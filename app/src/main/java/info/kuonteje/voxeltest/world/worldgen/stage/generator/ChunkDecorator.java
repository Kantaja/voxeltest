package info.kuonteje.voxeltest.world.worldgen.stage.generator;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.util.Pair;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.worldgen.config.data.FeatureConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.GeneratorConfig;
import info.kuonteje.voxeltest.world.worldgen.feature.Feature;
import info.kuonteje.voxeltest.world.worldgen.feature.generator.IFeatureGenerator;
import info.kuonteje.voxeltest.world.worldgen.stage.IGenerationStageConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IWorldGenerator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ChunkDecorator implements IWorldGenerator
{
	private List<Pair<EntryId, IFeatureGenerator>> features = new ObjectArrayList<>();
	
	public ChunkDecorator(GeneratorConfig rootConfig, Config config, long seed)
	{
		for(FeatureConfig featureConfig : config.features)
		{
			if(!featureConfig.enabled()) continue;
			
			Feature<?> feature = featureConfig.feature();
			features.add(Pair.of(feature.id(), feature.createGenerator(rootConfig, featureConfig.config(), seed)));
		}
	}
	
	@Override
	public void processChunk(Chunk chunk)
	{
		features.forEach(f ->
		{
			try
			{
				f.right().tryGenerateIn(chunk.world(), chunk);
			}
			catch(Exception e)
			{
				new RuntimeException("Failed to generate feature " + f.left().toString() + " in chunk " + chunk.pos().toString(), e).printStackTrace();
			}
		});
	}
	
	public static ChunkDecorator factory(GeneratorConfig rootConfig, IGenerationStageConfig config, long seed)
	{
		return new ChunkDecorator(rootConfig, (Config)config, seed);
	}
	
	@JsonDeserialize(builder = Config.Builder.class)
	public static class Config implements IGenerationStageConfig
	{
		private final List<FeatureConfig> features;
		
		private Config(List<FeatureConfig> features)
		{
			this.features = features;
		}
		
		@JsonProperty
		public List<FeatureConfig> features()
		{
			return features;
		}
		
		public static Builder builder()
		{
			return new Builder();
		}
		
		@JsonPOJOBuilder(withPrefix = "")
		public static class Builder
		{
			private List<FeatureConfig> features;
			
			private Builder() {}
			
			@JsonProperty
			public Builder features(List<FeatureConfig> features)
			{
				this.features = features;
				return this;
			}
			
			public Config build()
			{
				return new Config(features);
			}
		}
	}
}
