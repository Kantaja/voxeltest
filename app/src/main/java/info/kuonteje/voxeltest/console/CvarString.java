package info.kuonteje.voxeltest.console;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import info.kuonteje.voxeltest.console.CvarRegistry.SetResult;
import info.kuonteje.voxeltest.util.ConcurrentLazy;
import info.kuonteje.voxeltest.util.Flag;

public final class CvarString extends Cvar
{
	private final AtomicReference<String> value;
	private final String defaultValue;
	
	private final Function<String, String> transformer;
	private final BiConsumer<String, String> callback;
	
	private final ConcurrentLazy<AtomicReference<String>> latchValue;
	
	CvarString(CvarRegistry registry, String name, String initialValue, int flags, Function<String, String> transformer, BiConsumer<String, String> callback)
	{
		super(registry, name, flags);
		
		Objects.requireNonNull(initialValue, "initial value cannot be null");
		
		this.value = new AtomicReference<>(this.defaultValue = initialValue);
		
		this.transformer = transformer;
		this.callback = callback;
		
		latchValue = testFlag(Flags.LATCH) ? ConcurrentLazy.of(AtomicReference<String>::new) : null;
	}
	
	@Override
	public Type type()
	{
		return Type.STRING;
	}
	
	SetResult update(Function<String, String> op, boolean loading)
	{
		if(!loading)
		{
			if(testFlag(Flags.CHEAT) && !registry().console().cheatsEnabled()) return SetResult.CHEATS_REQUIRED;
			if(testFlag(Flags.READ_ONLY)) return SetResult.READ_ONLY;
		}
		
		if(transformer != null) op = op.andThen(transformer);
		
		if(loading)
		{
			String n = op.apply(defaultValue);
			if(n == null) return SetResult.INVALID_STRING;
			value.setRelease(n);
		}
		else
		{
			AtomicReference<String> target = testFlag(Flags.LATCH) ? latchValue.get() : this.value;
			
			if(callback != null)
			{
				synchronized(lock)
				{
					String o = value.getPlain();
					String n = op.apply(o);
					
					if(n == null) return SetResult.INVALID_STRING;
					
					callback.accept(n, o);
					
					target.setRelease(n);
				}
			}
			else
			{
				final Function<String, String> opf = op;
				Flag invalid = new Flag();
				
				target.updateAndGet(v ->
				{
					String n = opf.apply(v);
					
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
				
				if(invalid.test()) return SetResult.INVALID_STRING;
			}
		}
		
		return SetResult.STRING_SET;
	}
	
	public SetResult update(Function<String, String> op)
	{
		return update(op, false);
	}
	
	public SetResult set(String value)
	{
		return update(v -> value, false);
	}
	
	public String get()
	{
		return value.getAcquire();
	}
	
	@Override
	SetResult setString(String value, boolean loading)
	{
		return update(v -> value, loading);
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
		return testFlag(Flags.LATCH) && latchValue.got() ? latchValue.get().getAcquire() : null;
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
