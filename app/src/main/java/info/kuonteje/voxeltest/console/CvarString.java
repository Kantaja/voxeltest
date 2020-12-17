package info.kuonteje.voxeltest.console;

import java.util.Objects;
import java.util.function.Function;

import info.kuonteje.voxeltest.console.CvarRegistry.SetResult;
import info.kuonteje.voxeltest.util.functional.ToBoolBiFunction;

public final class CvarString extends Cvar
{
	private String value;
	private final String defaultValue;
	
	private final Function<String, String> transformer;
	private final ToBoolBiFunction<String, String> callback;
	
	private String latchValue = null;
	
	CvarString(CvarRegistry registry, String name, String initialValue, int flags, Function<String, String> transformer, ToBoolBiFunction<String, String> callback)
	{
		super(registry, name, flags);
		
		Objects.requireNonNull(initialValue, "initial value cannot be null");
		
		this.value = this.defaultValue = initialValue;
		
		this.transformer = transformer;
		this.callback = callback;
	}
	
	@Override
	public Type getType()
	{
		return Type.STRING;
	}
	
	SetResult set(String value, boolean loading)
	{
		if(!loading)
		{
			if(testFlag(Flags.CHEAT) && !getRegistry().getConsole().cheatsEnabled()) return SetResult.CHEATS_REQUIRED;
			if(testFlag(Flags.READ_ONLY)) return SetResult.READ_ONLY;
		}
		
		if(value == null) return SetResult.INVALID_STRING;
		
		synchronized(lock)
		{
			String previousValue = this.value;
			
			if(transformer != null) value = transformer.apply(value);
			if(!loading && callback != null && !callback.apply(value, previousValue)) return SetResult.CALLBACK_CANCELED;
			
			if(loading) this.value = value;
			else set0(value);
		}
		
		return SetResult.STRING_SET;
	}
	
	public SetResult set(String value)
	{
		return set(value, false);
	}
	
	private void set0(String value)
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
	
	public String get()
	{
		synchronized(lock)
		{
			return value;
		}
	}
	
	@Override
	SetResult setString(String value, boolean loading)
	{
		return set(value, loading);
	}
	
	@Override
	public String asString(boolean quoteStrings)
	{
		return quoteStrings ? ("\"" + get() + "\"") : get();
	}
	
	public String defaultValue()
	{
		return defaultValue;
	}
	
	@Override
	public String defaultValueAsString(boolean quoteStrings)
	{
		return quoteStrings ? ("\"" + defaultValue + "\"") : defaultValue;
	}
	
	public String latchValue()
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
		String latch = latchValue();
		return latch == null ? null : (quoteStrings ? ("\"" + latch.toString() + "\"") : latch.toString());
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
}
