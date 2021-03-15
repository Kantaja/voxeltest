package info.kuonteje.voxeltest.world.worldgen.stage;

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

@JsonSerialize(using = RegistryEntry.Serializer.class)
@JsonDeserialize(using = GenerationStage.Deserializer.class)
public final class GenerationStage<C extends IGenerationStageConfig> extends RegistryEntry<GenerationStage<?>>
{
	public static interface IWorldGeneratorFactory
	{
		IWorldGenerator createWorldGenerator(GeneratorConfig rootConfig, IGenerationStageConfig config, long seed);
	}
	
	// null for correct serialization
	public static final NoGenerationStageConfig NO_CONFIG = null;
	
	private final IWorldGeneratorFactory factory;
	
	private final Class<C> configType;
	private final C defaultConfig;
	
	public GenerationStage(EntryId id, Class<C> configType, C defaultConfig, IWorldGeneratorFactory factory)
	{
		super(GenerationStage.class, id);
		
		this.factory = factory;
		
		this.configType = configType;
		this.defaultConfig = defaultConfig;
	}
	
	public GenerationStage(String id, Class<C> configType, C defaultConfig, IWorldGeneratorFactory factory)
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
	
	public IWorldGenerator createGenerator(GeneratorConfig rootConfig, IGenerationStageConfig config, long seed)
	{
		return factory.createWorldGenerator(rootConfig, config, seed);
	}
	
	public static class Deserializer extends StdDeserializer<GenerationStage<?>>
	{
		private static final long serialVersionUID = 1L;
		
		public Deserializer()
		{
			super(GenerationStage.class);
		}
		
		@Override
		public GenerationStage<?> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException
		{
			return DefaultRegistries.GENERATION_STAGES.byId(parser.getCodec().readValue(parser, EntryId.class)).orElseThrow();
		}
	}
}
