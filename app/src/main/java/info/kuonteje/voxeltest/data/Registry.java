package info.kuonteje.voxeltest.data;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Registry<T extends RegistryEntry<T>> implements Iterable<T>
{
	private final Class<? super T> type;
	private final String typeName;
	
	private final Object registerLock = new Object();
	
	private final Object2IntMap<EntryId> idToIdx = new Object2IntOpenHashMap<>();
	private final Int2ObjectMap<T> idxToObj = new Int2ObjectAVLTreeMap<>();
	private final Object2ObjectMap<EntryId, T> idToObj = new Object2ObjectOpenHashMap<>();
	
	private final T defaultValue;
	
	private int maxIdx = 0;
	
	private final AtomicBoolean frozen = new AtomicBoolean(false);
	private List<Consumer<Registry<T>>> freezeCallbacks = new ObjectArrayList<>();
	
	Registry(Class<? super T> type, T defaultValue)
	{
		this.type = type;
		typeName = type.getSimpleName();
		
		this.defaultValue = defaultValue;
		
		System.out.println("Creating registry for type " + typeName);
		
		if(defaultValue != null)
		{
			idToIdx.put(defaultValue.getId(), 0);
			idxToObj.put(0, defaultValue);
			idToObj.put(defaultValue.getId(), defaultValue);
		}
	}
	
	public Class<? super T> getRegistryType()
	{
		return type;
	}
	
	public <V extends T> V register(V obj)
	{
		EntryId id = obj.getId();
		
		if(frozen.getAcquire()) throw new IllegalStateException("Cannot register " + obj.getClass().getName() + " with id " + id.toString() + " in frozen registry " + typeName);
		
		System.out.println("Registering " + obj.getClass().getName() + " with id " + id.toString() + " at index " + (maxIdx + 1) + " in registry " + typeName);
		
		synchronized(registerLock)
		{
			if(idToIdx.containsKey(id)) throw new DuplicateEntryException("Duplicate registry entry " + id.toString());
			
			int idx = ++maxIdx;
			
			idToIdx.put(id, idx);
			idxToObj.put(idx, obj);
			idToObj.put(id, obj);
		}
		
		return obj;
	}
	
	public T getDefaultValue()
	{
		return defaultValue;
	}
	
	public int getIdx(EntryId id)
	{
		return idToIdx.getOrDefault(id, 0);
	}
	
	public int getIdx(T obj)
	{
		return idToIdx.getOrDefault(obj.getId(), 0);
	}
	
	public T getByIdx(int idx)
	{
		return idxToObj.getOrDefault(idx, defaultValue);
	}
	
	public T getById(EntryId id)
	{
		return idToObj.getOrDefault(id, defaultValue);
	}
	
	public boolean isRegistered(EntryId id)
	{
		return idToIdx.containsKey(id);
	}
	
	public void freeze()
	{
		if(!frozen.compareAndExchangeRelease(false, true))
		{
			synchronized(frozen)
			{
				freezeCallbacks.forEach(c -> c.accept(this));
				freezeCallbacks = null;
				
				for(T t : this)
				{
					t.onFrozen(this);
				}
			}
		}
		else System.out.println("Attempted to re-freeze registry " + typeName + "?");
	}
	
	public boolean isFrozen()
	{
		return frozen.getAcquire();
	}
	
	public void addFreezeCallback(Consumer<Registry<T>> callback)
	{
		if(!frozen.getAcquire())
		{
			synchronized(frozen)
			{
				if(!frozen.getPlain()) // necessary?
				{
					freezeCallbacks.add(callback);
				}
			}
		}
	}
	
	public int size()
	{
		return defaultValue == null ? maxIdx : maxIdx + 1;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		if(!frozen.getAcquire()) throw new IllegalStateException("Cannot iterate over unfrozen registry " + typeName);
		return idxToObj.values().iterator();
	}
}
