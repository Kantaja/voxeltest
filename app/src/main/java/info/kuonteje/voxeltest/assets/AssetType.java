package info.kuonteje.voxeltest.assets;

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
@JsonDeserialize(using = AssetType.Deserializer.class)
public final class AssetType extends RegistryEntry<AssetType>
{
	private final String subdir, ext;
	
	public AssetType(EntryId id, String subdir, String ext)
	{
		super(AssetType.class, id);
		
		this.subdir = subdir;
		this.ext = ext;
	}
	
	public AssetType(String id, String subdir, String ext)
	{
		this(EntryId.create(id), subdir, ext);
	}
	
	public String subdir()
	{
		return subdir;
	}
	
	public String extension()
	{
		return ext;
	}
	
	public String resolveAsset(String id)
	{
		return subdir + "/" + id + "." + ext;
	}
	
	public static class Deserializer extends StdDeserializer<AssetType>
	{
		private static final long serialVersionUID = 1L;
		
		public Deserializer()
		{
			super(AssetType.class);
		}
		
		@Override
		public AssetType deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException
		{
			return DefaultRegistries.ASSET_TYPES.byId(parser.getCodec().readValue(parser, EntryId.class)).orElseThrow();
		}
	}
}
