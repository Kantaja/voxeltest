package info.kuonteje.voxeltest.util;

import java.util.Objects;
import java.util.function.LongSupplier;

public class LazyLong
{
	private long value;
	private LongSupplier supplier;
	
	private LazyLong(LongSupplier supplier)
	{
		this.supplier = Objects.requireNonNull(supplier, "supplier cannot be null");
	}
	
	public long get()
	{
		if(supplier != null)
		{
			value = supplier.getAsLong();
			supplier = null;
		}
		
		return value;
	}
	
	public boolean got()
	{
		return supplier == null;
	}
	
	public static LazyLong of(LongSupplier supplier)
	{
		return new LazyLong(supplier);
	}
}
