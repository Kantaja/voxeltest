package info.kuonteje.voxeltest.data;

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
	
	public final Class<? super T> getRegistryType()
	{
		return registryType;
	}
	
	public final EntryId getId()
	{
		return id;
	}
	
	public final int getIdx()
	{
		return idx;
	}
	
	final void setIdx(int idx)
	{
		if(this.idx == -1) this.idx = idx;
	}
	
	protected void onFrozen(Registry<T> registry)
	{
		//
	}
}
