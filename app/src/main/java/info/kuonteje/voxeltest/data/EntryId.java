package info.kuonteje.voxeltest.data;

import java.util.Objects;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.util.LazyInt;

public final class EntryId
{
	private final String domain, id;
	
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
}
