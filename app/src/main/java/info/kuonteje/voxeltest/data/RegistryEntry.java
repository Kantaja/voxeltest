package info.kuonteje.voxeltest.data;

public abstract class RegistryEntry<T extends RegistryEntry<T>>
{
	private final Class<? super T> registryType;
	private final EntryId id;
	
	protected RegistryEntry(Class<? super T> registryType, EntryId id)
	{
		this.registryType = registryType;
		this.id = id;
	}
	
	public final Class<? super T> getRegistryType()
	{
		return registryType;
	}
	
	public final EntryId getId()
	{
		return id;
	}
	
	public void onFrozen(Registry<T> registry)
	{
		//
	}
}
