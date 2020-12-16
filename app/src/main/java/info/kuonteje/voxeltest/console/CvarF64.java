package info.kuonteje.voxeltest.console;

import info.kuonteje.voxeltest.console.CvarRegistry.SetResult;
import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.util.functional.ToBoolDoubleBiFunction;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;

public final class CvarF64 extends Cvar
{
	public static final Double2DoubleFunction ZERO_TO_ONE_TRANSFORMER = v -> MathUtil.clamp(v, 0.0, 1.0);
	
	private double value;
	private final double defaultValue;
	
	private final Double2DoubleFunction transformer;
	private final ToBoolDoubleBiFunction callback;
	
	private Double latchValue = null;
	
	CvarF64(CvarRegistry registry, String name, double initialValue, int flags, Double2DoubleFunction transformer, ToBoolDoubleBiFunction callback)
	{
		super(registry, name, flags);
		
		this.value = this.defaultValue = initialValue;
		
		this.transformer = transformer;
		this.callback = callback;
	}
	
	@Override
	public Type getType()
	{
		return Type.F64;
	}
	
	SetResult set(double value, boolean loading)
	{
		if(!loading)
		{
			if(testFlag(Flags.CHEAT) && !getRegistry().getConsole().cheatsEnabled()) return SetResult.CHEATS_REQUIRED;
			if(testFlag(Flags.READ_ONLY)) return SetResult.READ_ONLY;
		}
		
		synchronized(lock)
		{
			double previousValue = this.value;
			
			if(transformer != null) value = transformer.apply(value);
			if(!loading && callback != null && !callback.apply(value, previousValue)) return SetResult.CALLBACK_CANCELED;
			
			if(loading) this.value = value;
			else set0(value);
		}
		
		return SetResult.F64_SET;
	}
	
	public SetResult set(double value)
	{
		return set(value, false);
	}
	
	private void set0(double value)
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
	
	public double get()
	{
		synchronized(lock)
		{
			return value;
		}
	}
	
	public float getAsFloat()
	{
		return (float)get();
	}
	
	@Override
	SetResult setString(String value, boolean loading)
	{
		try
		{
			return set(Double.parseDouble(value), loading);
		}
		catch(NullPointerException | NumberFormatException e)
		{
			return SetResult.INVALID_F64;
		}
	}
	
	@Override
	public String asString(boolean quoteStrings)
	{
		return String.valueOf(get());
	}
	
	public double defaultValue()
	{
		return defaultValue;
	}
	
	@Override
	public String defaultValueAsString(boolean quoteStrings)
	{
		return String.valueOf(defaultValue);
	}
	
	public Double latchValue()
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
			
			set0(defaultValue);
		}
	}
}
