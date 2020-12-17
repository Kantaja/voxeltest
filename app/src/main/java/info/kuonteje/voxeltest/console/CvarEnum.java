package info.kuonteje.voxeltest.console;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import info.kuonteje.voxeltest.console.CvarRegistry.SetResult;
import info.kuonteje.voxeltest.util.functional.ToBoolBiFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;

public final class CvarEnum<T extends Enum<T>> extends SealedGenericWorkaround
{
	private final Class<T> enumType;
	private final Map<String, T> typeMap = new Object2ObjectAVLTreeMap<>();
	
	private T value;
	private final T defaultValue;
	
	private final Function<T, T> transformer;
	private final ToBoolBiFunction<T, T> callback;
	
	private T latchValue = null;
	
	CvarEnum(CvarRegistry registry, Class<T> enumType, String name, T initialValue, int flags, Function<T, T> transformer, ToBoolBiFunction<T, T> callback)
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
		
		this.value = this.defaultValue = initialValue;
		
		this.transformer = transformer;
		this.callback = callback;
	}
	
	@Override
	public Type getType()
	{
		return Type.ENUM;
	}
	
	public Class<? extends Enum<?>> getEnumType()
	{
		return enumType;
	}
	
	SetResult set(T value, boolean loading)
	{
		if(!loading)
		{
			if(testFlag(Flags.CHEAT) && !getRegistry().getConsole().cheatsEnabled()) return SetResult.CHEATS_REQUIRED;
			if(testFlag(Flags.READ_ONLY)) return SetResult.READ_ONLY;
		}
		
		synchronized(lock)
		{
			T previousValue = this.value;
			
			if(transformer != null) value = transformer.apply(value);
			if(!loading && callback != null && !callback.apply(value, previousValue)) return SetResult.CALLBACK_CANCELED;
			
			if(loading) this.value = value;
			else set0(value);
		}
		
		return SetResult.ENUM_SET;
	}
	
	public SetResult set(T value)
	{
		return set(value, false);
	}
	
	private void set0(T value)
	{
		if(testFlag(Flags.LATCH))
		{
			synchronized(latchLock)
			{
				latchValue = value;
			}
		}
		else this.value = value;
	}
	
	public T get()
	{
		synchronized(lock)
		{
			return value;
		}
	}
	
	@Override
	SetResult setString(String value, boolean loading)
	{
		if(value == null) return SetResult.INVALID_ENUM;
		
		T val = typeMap.get(value.toLowerCase());
		return val == null ? SetResult.INVALID_ENUM : set(val, loading);
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
		if(testFlag(Flags.LATCH))
		{
			synchronized(latchLock)
			{
				return latchValue;
			}
		}
		else return null;
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
		synchronized(lock)
		{
			if(callback != null) callback.apply(defaultValue, value);
			
			set0(defaultValue);
		}
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " (allowed values: [" + String.join(", ", typeMap.keySet()) + "])";
	}
}
