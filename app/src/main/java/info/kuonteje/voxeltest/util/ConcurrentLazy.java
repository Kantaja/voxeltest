package info.kuonteje.voxeltest.util;

import java.util.Objects;
import java.util.function.Supplier;

public class ConcurrentLazy<T>
{
	private T value = null;
	private volatile Supplier<T> supplier;
	
	private final Object lock = new Object();
	
	private ConcurrentLazy(Supplier<T> supplier)
	{
		this.supplier = Objects.requireNonNull(supplier, "supplier cannot be null");
	}
	
	public T get()
	{
		if(supplier != null)
		{
			synchronized(lock)
			{
				if(supplier != null)
				{
					value = supplier.get();
					supplier = null;
				}
			}
		}
		
		return value;
	}
	
	public boolean got()
	{
		return supplier == null;
	}
	
	public static <T> ConcurrentLazy<T> of(Supplier<T> supplier)
	{
		return new ConcurrentLazy<>(supplier);
	}
}
