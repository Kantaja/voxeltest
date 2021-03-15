package info.kuonteje.voxeltest.block;

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
import info.kuonteje.voxeltest.render.block.RenderType;
import info.kuonteje.voxeltest.world.World;

@JsonSerialize(using = RegistryEntry.Serializer.class)
@JsonDeserialize(using = Block.Deserializer.class)
public class Block extends RegistryEntry<Block>
{
	private final RenderType renderType;
	
	public Block(EntryId id, RenderType renderType)
	{
		super(Block.class, id);
		this.renderType = renderType;
	}
	
	public Block(String id, RenderType renderType)
	{
		this(EntryId.create(id), renderType);
	}
	
	public Block(EntryId id)
	{
		this(id, RenderType.SOLID);
	}
	
	public Block(String id)
	{
		this(id, RenderType.SOLID);
	}
	
	public RenderType renderType()
	{
		return renderType;
	}
	
	public void update(World world, int x, int y, int z)
	{
		//
	}
	
	public void onPlaced(World world, int x, int y, int z)
	{
		//
	}
	
	public static class Deserializer extends StdDeserializer<Block>
	{
		private static final long serialVersionUID = 1L;
		
		public Deserializer()
		{
			super(Block.class);
		}
		
		@Override
		public Block deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException
		{
			return DefaultRegistries.BLOCKS.byId(parser.getCodec().readValue(parser, EntryId.class)).orElseThrow();
		}
	}
}
