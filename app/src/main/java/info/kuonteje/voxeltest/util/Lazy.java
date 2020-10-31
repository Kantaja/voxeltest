package info.kuonteje.voxeltest.util;

import java.util.function.Supplier;

public class Lazy<T>
{
	private T value = null;
	private Supplier<T> supplier;
	
	private Lazy(Supplier<T> supplier)
	{
		this.supplier = supplier;
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
	
	public static <T> Lazy<T> of(Supplier<T> supplier)
	{
		return new Lazy<>(supplier);
	}
}
