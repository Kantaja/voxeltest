package info.kuonteje.voxeltest.data;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.util.LazyInt;

@JsonDeserialize(using = EntryId.Deserializer.class)
public final class EntryId
{
	private final String domain, id;
	
	@JsonIgnore
	private final LazyInt hash;
	
	private EntryId(String domain, String id)
	{
		this.domain = domain.trim().toLowerCase();
		this.id = id.trim().toLowerCase();
		
		hash = LazyInt.of(() -> Objects.hash(domain, id));
	}
	
	public String getDomain()
	{
		return domain;
	}
	
	public String getId()
	{
		return id;
	}
	
	@Override
	@JsonValue
	public String toString()
	{
		return domain + ":" + id;
	}
	
	@Override
	public boolean equals(Object other)
	{
		return other instanceof EntryId o && o.domain.equals(domain) && o.id.equals(id);
	}
	
	@Override
	public int hashCode()
	{
		return hash.get();
	}
	
	public static EntryId create(String domain, String id)
	{
		return new EntryId(domain, id);
	}
	
	public static EntryId create(String id)
	{
		int colon = id.indexOf(':');
		return colon != -1 ? new EntryId(id.substring(0, colon), id.substring(colon + 1, id.length())) : new EntryId(VoxelTest.DEFAULT_DOMAIN, id);
	}
	
	public static EntryId createPrefixed(String domain, String id, String idPrefix)
	{
		return new EntryId(domain, idPrefix + "/" + id);
	}
	
	public static EntryId createPrefixed(String id, String idPrefix)
	{
		int colon = id.indexOf(':');
		return colon != -1 ? new EntryId(id.substring(0, colon), idPrefix + "/" + id.substring(colon + 1, id.length())) : new EntryId(VoxelTest.DEFAULT_DOMAIN, idPrefix + "/" + id);
	}
	
	public static EntryId createPrefixed(EntryId id, String idPrefix)
	{
		return createPrefixed(id.domain, id.id, idPrefix);
	}
	
	public static class Deserializer extends StdDeserializer<EntryId>
	{
		private static final long serialVersionUID = 1L;
		
		public Deserializer()
		{
			super(EntryId.class);
		}
		
		@Override
		public EntryId deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException
		{
			// TODO this complains about deserializing to a String when it receives e.g. a JSON object, instead of an EntryId
			// rewrite for better error messages?
			return EntryId.create(parser.getCodec().readValue(parser, String.class));
		}
	}
}
