package info.kuonteje.voxeltest.console;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import info.kuonteje.voxeltest.console.CvarRegistry.SetResult;
import info.kuonteje.voxeltest.util.ConcurrentLazy;
import info.kuonteje.voxeltest.util.Flag;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;

public final class CvarEnum<T extends Enum<T>> extends SealedGenericWorkaround
{
	public static final Class<? extends Enum<?>> ANY_TYPE = null;
	
	private final Class<T> enumType;
	private final Map<String, T> typeMap = new Object2ObjectAVLTreeMap<>();
	
	private final AtomicReference<T> value;
	private final T defaultValue;
	
	private final Function<T, T> transformer;
	private final BiConsumer<T, T> callback;
	
	private ConcurrentLazy<AtomicReference<T>> latchValue;
	
	CvarEnum(CvarRegistry registry, Class<T> enumType, String name, T initialValue, int flags, Function<T, T> transformer, BiConsumer<T, T> callback)
	{
		super(registry, name, flags);
		
		Objects.requireNonNull(initialValue, "initial value cannot be null");
		
		this.enumType = enumType;
		
		try
		{
			@SuppressWarnings("unchecked")
			T[] values = (T[])enumType.getDeclaredMethod("values").invoke(null);
			
			for(T t : values)
			{
				typeMap.put(t.name().toLowerCase(), t);
			}
		}
		catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			throw new RuntimeException(e);
		}
		
		this.value = new AtomicReference<>(this.defaultValue = initialValue);
		
		this.transformer = transformer;
		this.callback = callback;
		
		latchValue = testFlag(Flags.LATCH) ? ConcurrentLazy.of(AtomicReference<T>::new) : null;
	}
	
	@Override
	public Type type()
	{
		return Type.ENUM;
	}
	
	public Class<? extends Enum<?>> enumType()
	{
		return enumType;
	}
	
	SetResult update(Function<T, T> op, boolean loading)
	{
		if(!loading)
		{
			if(testFlag(Flags.CHEAT) && !registry().console().cheatsEnabled()) return SetResult.CHEATS_REQUIRED;
			if(testFlag(Flags.READ_ONLY)) return SetResult.READ_ONLY;
		}
		
		if(transformer != null) op = op.andThen(transformer);
		
		if(loading)
		{
			T n = op.apply(defaultValue);
			if(n == null) return SetResult.INVALID_ENUM;
			value.setRelease(n);
		}
		else
		{
			AtomicReference<T> target = testFlag(Flags.LATCH) ? latchValue.get() : value;
			
			if(callback != null)
			{
				synchronized(lock)
				{
					T o = value.getPlain();
					T n = op.apply(o);
					
					if(n == null) return SetResult.INVALID_ENUM;
					
					callback.accept(n, o);
					
					target.setRelease(n);
				}
			}
			else
			{
				final Function<T, T> opf = op;
				Flag invalid = new Flag();
				
				target.updateAndGet(v ->
				{
					T n = opf.apply(v);
					
					if(n == null)
					{
						invalid.set();
						return v;
					}
					else
					{
						invalid.clear();
						return n;
					}
				});
				
				if(invalid.test()) return SetResult.INVALID_ENUM;
			}
		}
		
		return SetResult.ENUM_SET;
	}
	
	public SetResult update(Function<T, T> op)
	{
		return update(op, false);
	}
	
	public SetResult set(T value)
	{
		return update(v -> value, false);
	}
	
	public T get()
	{
		return value.getAcquire();
	}
	
	@Override
	SetResult setString(String value, boolean loading)
	{
		if(value == null) return SetResult.INVALID_ENUM;
		
		T val = typeMap.get(value.toLowerCase());
		return val == null ? SetResult.INVALID_ENUM : update(v -> val, loading);
	}
	
	@Override
	public String asString(boolean quoteStrings)
	{
		return get().name().toLowerCase();
	}
	
	public T defaultValue()
	{
		return defaultValue;
	}
	
	@Override
	public String defaultValueAsString(boolean quoteStrings)
	{
		return defaultValue.name().toLowerCase();
	}
	
	public T latchValue()
	{
		return testFlag(Flags.LATCH) && latchValue.got() ? latchValue.get().getAcquire() : null;
	}
	
	@Override
	public String latchValueAsString(boolean quoteStrings)
	{
		T latch = latchValue();
		return latch == null ? null : latch.name().toLowerCase();
	}
	
	@Override
	public void reset()
	{
		if(callback != null)
		{
			synchronized(lock)
			{
				callback.accept(defaultValue, value.getPlain());
			}
		}
		
		value.setRelease(defaultValue);
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " (allowed values: [" + String.join(", ", typeMap.keySet()) + "])";
	}
}
