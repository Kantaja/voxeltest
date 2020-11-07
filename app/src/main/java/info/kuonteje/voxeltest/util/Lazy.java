package info.kuonteje.voxeltest.util;

import java.util.Objects;
import java.util.function.Supplier;

public class Lazy<T>
{
	private T value = null;
	private Supplier<T> supplier;
	
	private Lazy(Supplier<T> supplier)
	{
		this.supplier = Objects.requireNonNull(supplier, "supplier cannot be null");
	}
	
	public T get()
	{
		if(supplier != null)
		{
			value = supplier.get();
			supplier = null;
		}
		
		return value;
	}
	
	public boolean got()
	{
		return supplier == null;
	}
	
	public static <T> Lazy<T> of(Supplier<T> supplier)
	{
		return new Lazy<>(supplier);
	}
}
