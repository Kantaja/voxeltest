package info.kuonteje.voxeltest.console;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleUnaryOperator;

import info.kuonteje.voxeltest.console.CvarRegistry.SetResult;
import info.kuonteje.voxeltest.util.ConcurrentLazy;
import info.kuonteje.voxeltest.util.functional.DoubleBiConsumer;

public final class CvarF64 extends Cvar
{
	private final AtomicLong value;
	private final double defaultValue;
	
	private final DoubleUnaryOperator transformer;
	private final DoubleBiConsumer callback;
	
	private final ConcurrentLazy<AtomicLong> latchValue;
	
	CvarF64(CvarRegistry registry, String name, double initialValue, int flags, DoubleUnaryOperator transformer, DoubleBiConsumer callback)
	{
		super(registry, name, flags);
		
		this.value = new AtomicLong(Double.doubleToRawLongBits(this.defaultValue = initialValue));
		
		this.transformer = transformer;
		this.callback = callback;
		
		latchValue = testFlag(Flags.LATCH) ? ConcurrentLazy.of(AtomicLong::new) : null;
	}
	
	@Override
	public Type type()
	{
		return Type.F64;
	}
	
	SetResult update(DoubleUnaryOperator op, boolean loading)
	{
		if(!loading)
		{
			if(testFlag(Flags.CHEAT) && !registry().console().cheatsEnabled()) return SetResult.CHEATS_REQUIRED;
			if(testFlag(Flags.READ_ONLY)) return SetResult.READ_ONLY;
		}
		
		if(transformer != null) op = op.andThen(transformer);
		
		if(loading) this.value.setRelease(Double.doubleToRawLongBits(op.applyAsDouble(defaultValue)));
		else
		{
			AtomicLong target = testFlag(Flags.LATCH) ? latchValue.get() : this.value;
			
			if(callback != null)
			{
				synchronized(lock)
				{
					double o = value.getPlain();
					double n = op.applyAsDouble(o);
					
					callback.accept(n, o);
					
					target.setRelease(Double.doubleToRawLongBits(n));
				}
			}
			else
			{
				final DoubleUnaryOperator opf = op;
				target.updateAndGet(v -> Double.doubleToRawLongBits(opf.applyAsDouble(Double.longBitsToDouble(v))));
			}
		}
		
		return SetResult.F64_SET;
	}
	
	public SetResult update(DoubleUnaryOperator op)
	{
		return update(op, false);
	}
	
	public SetResult set(double value)
	{
		return update(v -> value, false);
	}
	
	public double get()
	{
		return Double.longBitsToDouble(value.getAcquire());
	}
	
	public float asFloat()
	{
		return (float)get();
	}
	
	@Override
	SetResult setString(String value, boolean loading)
	{
		try
		{
			double n = Double.parseDouble(value);
			return update(v -> n, loading);
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
		return testFlag(Flags.LATCH) && latchValue.got() ? Double.longBitsToDouble(latchValue.get().getAcquire()) : null;
	}
	
	@Override
	public String latchValueAsString(boolean quoteStrings)
	{
		Double latch = latchValue();
		return latch == null ? null : latch.toString();
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
		
		value.setRelease(Double.doubleToRawLongBits(defaultValue));
	}
}
