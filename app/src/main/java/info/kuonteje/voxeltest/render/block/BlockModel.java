package info.kuonteje.voxeltest.render.block;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
@JsonDeserialize(using = BlockModel.Deserializer.class)
public abstract class BlockModel extends RegistryEntry<BlockModel>
{
	public BlockModel(EntryId id)
	{
		super(BlockModel.class,  id);
	}
	
	public BlockModel(String id)
	{
		this(EntryId.create(id));
	}
	
	public abstract void getVertices(FloatBuffer buf, int x, int y, int z, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible);
	public abstract void getTextureCoords(FloatBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible);
	public abstract void getTextureLayers(IntBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible);
	public abstract void getTints(ByteBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible);
	
	public static class Deserializer extends StdDeserializer<BlockModel>
	{
		private static final long serialVersionUID = 1L;
		
		public Deserializer()
		{
			super(BlockModel.class);
		}
		
		@Override
		public BlockModel deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException
		{
			return DefaultRegistries.BLOCK_MODELS.byId(parser.getCodec().readValue(parser, EntryId.class)).orElseThrow();
		}
	}
}
