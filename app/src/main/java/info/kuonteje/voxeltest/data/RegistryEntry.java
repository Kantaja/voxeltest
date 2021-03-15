package info.kuonteje.voxeltest.data;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public abstract class RegistryEntry<T extends RegistryEntry<T>>
{
	private final Class<? super T> registryType;
	private final EntryId id;
	
	private int idx = -1;
	
	protected RegistryEntry(Class<? super T> registryType, EntryId id)
	{
		this.registryType = registryType;
		this.id = id;
	}
	
	public final Class<? super T> registryType()
	{
		return registryType;
	}
	
	public final EntryId id()
	{
		return id;
	}
	
	public final int idx()
	{
		return idx;
	}
	
	final void idx(int idx)
	{
		if(this.idx != -1) throw new IllegalStateException("???");
		this.idx = idx;
	}
	
	protected void onFrozen(Registry<T> registry)
	{
		//
	}
	
	// used by subclasses
	public static class Serializer extends StdSerializer<RegistryEntry<?>>
	{
		private static final long serialVersionUID = 1L;
		
		public Serializer()
		{
			super(RegistryEntry.class, false);
		}
		
		@Override
		public void serialize(RegistryEntry<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException
		{
			gen.writeObject(value.id());
		}
	}
}
