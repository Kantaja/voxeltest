package info.kuonteje.voxeltest.console;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;

import info.kuonteje.voxeltest.console.CvarRegistry.SetResult;
import info.kuonteje.voxeltest.util.ConcurrentLazy;
import info.kuonteje.voxeltest.util.functional.LongBiConsumer;

public final class CvarI64 extends Cvar
{
	public static final LongUnaryOperator BOOL_TRANSFORMER = v -> v == 0 ? 0 : 1;
	
	private final AtomicLong value;
	private final long defaultValue;
	
	private final LongUnaryOperator transformer;
	private final LongBiConsumer callback;
	
	private final ConcurrentLazy<AtomicLong> latchValue;
	
	CvarI64(CvarRegistry registry, String name, long initialValue, int flags, LongUnaryOperator transformer, LongBiConsumer callback)
	{
		super(registry, name, flags);
		
		this.value = new AtomicLong(this.defaultValue = initialValue);
		
		this.transformer = transformer;
		this.callback = callback;
		
		latchValue = testFlag(Flags.LATCH) ? ConcurrentLazy.of(AtomicLong::new) : null;
	}
	
	@Override
	public Type type()
	{
		return Type.I64;
	}
	
	SetResult update(LongUnaryOperator op, boolean loading)
	{
		if(!loading)
		{
			if(testFlag(Flags.CHEAT) && !registry().console().cheatsEnabled()) return SetResult.CHEATS_REQUIRED;
			if(testFlag(Flags.READ_ONLY)) return SetResult.READ_ONLY;
		}
		
		if(transformer != null) op = op.andThen(transformer);
		
		if(loading) this.value.setRelease(op.applyAsLong(defaultValue));
		else
		{
			AtomicLong target = testFlag(Flags.LATCH) ? latchValue.get() : this.value;
			
			if(callback != null)
			{
				synchronized(lock)
				{
					long o = value.getPlain();
					long n = op.applyAsLong(o);
					
					callback.accept(n, o);
					
					target.setRelease(n);
				}
			}
			else target.updateAndGet(op);
		}
		
		return SetResult.I64_SET;
	}
	
	public SetResult update(LongUnaryOperator op)
	{
		return update(op, false);
	}
	
	public SetResult set(long value)
	{
		return update(v -> value, false);
	}
	
	public long get()
	{
		return value.getAcquire();
	}
	
	public int asInt()
	{
		return (int)get();
	}
	
	public boolean asBool()
	{
		return get() != 0L;
	}
	
	public void toggleBool()
	{
		update(v -> v == 0L ? 1L : 0L, false);
	}
	
	@Override
	SetResult setString(String value, boolean loading)
	{
		try
		{
			long n = Long.parseLong(value);
			return update(v -> n, loading);
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
		return testFlag(Flags.LATCH) && latchValue.got() ? latchValue.get().getAcquire() : null;
	}
	
	@Override
	public String latchValueAsString(boolean quoteStrings)
	{
		Long latch = latchValue();
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
		
		value.setRelease(defaultValue);
	}
}
