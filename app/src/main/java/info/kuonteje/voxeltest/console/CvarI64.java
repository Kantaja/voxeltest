package info.kuonteje.voxeltest.console;

import info.kuonteje.voxeltest.console.CvarRegistry.SetResult;
import info.kuonteje.voxeltest.util.functional.ToBoolLongBiFunction;
import it.unimi.dsi.fastutil.longs.Long2LongFunction;

public final class CvarI64 extends Cvar
{
	public static final Long2LongFunction BOOL_TRANSFORMER = v -> v == 0 ? 0 : 1;
	
	private long value;
	private final long defaultValue;
	
	private final Long2LongFunction transformer;
	private final ToBoolLongBiFunction callback;
	
	private Long latchValue = null;
	
	CvarI64(CvarRegistry registry, String name, long initialValue, int flags, Long2LongFunction transformer, ToBoolLongBiFunction callback)
	{
		super(registry, name, flags);
		
		this.value = this.defaultValue = initialValue;
		
		this.transformer = transformer;
		this.callback = callback;
	}
	
	@Override
	public Type getType()
	{
		return Type.I64;
	}
	
	SetResult set(long value, boolean loading)
	{
		if(!loading)
		{
			if(testFlag(Flags.CHEAT) && !getRegistry().getConsole().cheatsEnabled()) return SetResult.CHEATS_REQUIRED;
			if(testFlag(Flags.READ_ONLY)) return SetResult.READ_ONLY;
		}
		
		synchronized(lock)
		{
			long previousValue = this.value;
			
			if(transformer != null) value = transformer.apply(value);
			if(!loading && callback != null && !callback.apply(value, previousValue)) return SetResult.CALLBACK_CANCELED;
			
			if(loading) this.value = value;
			else set0(value);
		}
		
		return SetResult.I64_SET;
	}
	
	public SetResult set(long value)
	{
		return set(value, false);
	}
	
	private void set0(long value)
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
	
	public long get()
	{
		synchronized(lock)
		{
			return value;
		}
	}
	
	public boolean getAsBool()
	{
		return get() != 0L;
	}
	
	@Override
	SetResult setString(String value, boolean loading)
	{
		try
		{
			return set(Long.parseLong(value), loading);
		}
		catch(NullPointerException | NumberFormatException e)
		{
			return SetResult.INVALID_I64;
		}
	}
	
	@Override
	public String asString(boolean quoteStrings)
	{
		return String.valueOf(get());
	}
	
	public long defaultValue()
	{
		return defaultValue;
	}
	
	@Override
	public String defaultValueAsString(boolean quoteStrings)
	{
		return String.valueOf(defaultValue);
	}
	
	public Long latchValue()
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
		return String.valueOf(latchValue());
	}
	
	@Override
	public void reset()
	{
		synchronized(lock)
		{
			if(callback != null) callback.apply(defaultValue, value);
			value = defaultValue;
		}
	}
}
