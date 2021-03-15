package info.kuonteje.voxeltest.data;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import info.kuonteje.voxeltest.util.ConcurrentLazy;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class Registry<T extends RegistryEntry<T>> implements Iterable<T>
{
	private static record ChildData<T>(Class<? super T> type, Object2ObjectMap<EntryId, T> data) {}
	
	private final Class<? super T> type;
	
	private final Object registerLock = new Object();
	
	private final Int2ObjectMap<T> idxToObj = new Int2ObjectAVLTreeMap<>();
	private final Object2ObjectMap<EntryId, T> idToObj = new Object2ObjectOpenHashMap<>();
	
	private final T defaultValue;
	
	private int maxIdx = 0;
	
	private final AtomicBoolean frozen = new AtomicBoolean(false);
	private ObjectList<Consumer<Registry<T>>> freezeCallbacks = new ObjectArrayList<>();
	
	private ConcurrentLazy<Object2ObjectMap<EntryId, ChildData<?>>> childData = ConcurrentLazy.of(Object2ObjectOpenHashMap::new);
	
	Registry(Class<? super T> type, T defaultValue)
	{
		// TODO
		//if(!Modifier.isFinal(type.getModifiers())) throw new RuntimeException("Cannot create registry for non-final type " + type.getName());
		
		this.type = type;
		
		this.defaultValue = defaultValue;
		
		System.out.println("Creating registry for type " + type.getName());
		
		if(defaultValue != null)
		{
			idxToObj.put(0, defaultValue);
			idToObj.put(defaultValue.id(), defaultValue);
			
			defaultValue.idx(0);
		}
	}
	
	public Class<? super T> registryType()
	{
		return type;
	}
	
	public <V extends T> V register(V obj)
	{
		EntryId id = obj.id();
		
		if(frozen.getAcquire()) throw new IllegalStateException("Cannot register " + id.toString() + " in frozen registry " + type.getName());
		
		synchronized(registerLock)
		{
			System.out.println("Registering " + id.toString() + " of " + type.getName() + " at index " + (maxIdx + 1));
			
			if(idToObj.containsKey(id)) throw new DuplicateEntryException("Duplicate registry entry " + id.toString() + " of " + type.getName());
			
			int idx = ++maxIdx;
			
			idxToObj.put(idx, obj);
			idToObj.put(id, obj);
			
			obj.idx(idx);
		}
		
		return obj;
	}
	
	public boolean hasDefault()
	{
		return defaultValue == null;
	}
	
	public T defaultValue()
	{
		return defaultValue;
	}
	
	public T byIdxRaw(int idx)
	{
		return idxToObj.getOrDefault(idx, defaultValue);
	}
	
	public Optional<T> byIdx(int idx)
	{
		return Optional.ofNullable(byIdxRaw(idx));
	}
	
	public T byIdRaw(EntryId id)
	{
		return id == null ? defaultValue : idToObj.getOrDefault(id, defaultValue);
	}
	
	public Optional<T> byId(EntryId id)
	{
		return Optional.ofNullable(byIdRaw(id));
	}
	
	public boolean isRegistered(EntryId id)
	{
		return idToObj.containsKey(id);
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
		else System.out.println("Attempted to re-freeze registry " + type.getName() + "?");
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
				// necessary?
				if(!frozen.getPlain()) freezeCallbacks.add(callback);
			}
		}
	}
	
	public boolean createChildDataMap(EntryId id, Class<?> type)
	{
		synchronized(registerLock)
		{
			if(maxIdx > 0) throw new IllegalStateException("Cannot create child data map " + id.toString() + " in non-empty registry " + this.type.getName());
			
			ChildData<?> existing = childData.get().computeIfAbsent(id, _id -> new ChildData<>(type, new Object2ObjectOpenHashMap<>()));
			boolean success = existing.type == type;
			
			if(!success) System.err.println("Tried to create child data map " + id.toString() + " of type " + type.getName() + " for " + this.type.getName() + ", but it already exists with type " + existing.type.getName());
			
			return success;
		}
	}
	
	public boolean createChildDataMap(String id, Class<?> type)
	{
		return createChildDataMap(EntryId.create(id), type);
	}
	
	@SuppressWarnings("unchecked")
	public <D> Object2ObjectMap<EntryId, D> childDataMap(EntryId id, Class<? super D> type)
	{
		if(!childData.got()) return null;
		
		ChildData<?> map = childData.get().get(id);
		return map != null && map.type == type ? (Object2ObjectMap<EntryId, D>)map.data : null;
	}
	
	public int maxIdx()
	{
		return maxIdx;
	}
	
	public int size()
	{
		return defaultValue == null ? maxIdx : maxIdx + 1;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		if(!frozen.getAcquire()) throw new IllegalStateException("Cannot iterate over unfrozen registry " + type.getName());
		return idxToObj.values().iterator();
	}
	
	public Iterable<T> withoutDefault()
	{
		return defaultValue == null ? this : () ->
		{
			Iterator<T> iter = iterator();
			iter.next();
			return iter;
		};
	}
}
