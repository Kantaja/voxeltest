package info.kuonteje.voxeltest.world.worldgen.feature;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.RegistryEntry;
import info.kuonteje.voxeltest.world.worldgen.config.data.GeneratorConfig;
import info.kuonteje.voxeltest.world.worldgen.feature.generator.IFeatureGenerator;

@JsonSerialize(using = RegistryEntry.Serializer.class)
@JsonDeserialize(using = Feature.Deserializer.class)
public final class Feature<C extends IFeatureConfig> extends RegistryEntry<Feature<?>>
{
	public static interface IFeatureGeneratorFactory
	{
		IFeatureGenerator createGenerator(GeneratorConfig rootConfig, IFeatureConfig config, long seed);
	}
	
	// null for correct serialization
	public static final NoFeatureConfig NO_CONFIG = null;
	
	private final IFeatureGeneratorFactory factory;
	
	private final Class<C> configType;
	private final C defaultConfig;
	
	public Feature(EntryId id, Class<C> configType, C defaultConfig, IFeatureGeneratorFactory factory)
	{
		super(Feature.class, id);
		
		this.factory = factory;
		
		this.configType = configType;
		this.defaultConfig = defaultConfig;
	}
	
	public Feature(String id, Class<C> configType, C defaultConfig, IFeatureGeneratorFactory factory)
	{
		this(EntryId.create(id), configType, defaultConfig, factory);
	}
	
	public Class<C> configType()
	{
		return configType;
	}
	
	public C defaultConfig()
	{
		return defaultConfig;
	}
	
	public IFeatureGenerator createGenerator(GeneratorConfig rootConfig, IFeatureConfig config, long seed)
	{
		return factory.createGenerator(rootConfig, config, seed);
	}
	
	public static class Deserializer extends StdDeserializer<Feature<?>>
	{
		private static final long serialVersionUID = 1L;
		
		public Deserializer()
		{
			super(Feature.class);
		}
		
		@Override
		public Feature<?> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException
		{
			return DefaultRegistries.FEATURES.byId(parser.getCodec().readValue(parser, EntryId.class)).orElseThrow();
		}
	}
}
