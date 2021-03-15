package info.kuonteje.voxeltest.render;

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

@JsonSerialize(using = RegistryEntry.Serializer.class)
@JsonDeserialize(using = BlockTexture.Deserializer.class)
public final class BlockTexture extends RegistryEntry<BlockTexture>
{
	private final EntryId textureId;
	
	public BlockTexture(EntryId id)
	{
		super(BlockTexture.class, id);
		this.textureId = EntryId.createPrefixed(id, "blocks");
	}
	
	public BlockTexture(String id)
	{
		this(EntryId.create(id));
	}
	
	public EntryId textureId()
	{
		return textureId;
	}
	
	public static class Deserializer extends StdDeserializer<BlockTexture>
	{
		private static final long serialVersionUID = 1L;
		
		public Deserializer()
		{
			super(BlockTexture.class);
		}
		
		@Override
		public BlockTexture deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException
		{
			return DefaultRegistries.BLOCK_TEXTURES.byId(parser.getCodec().readValue(parser, EntryId.class)).orElseThrow();
		}
	}
}
