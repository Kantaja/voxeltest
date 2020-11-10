package info.kuonteje.voxeltest.util;

import java.util.Objects;
import java.util.function.IntSupplier;

public class LazyInt
{
	private int value;
	private IntSupplier supplier;
	
	private LazyInt(IntSupplier supplier)
	{
		this.supplier = Objects.requireNonNull(supplier, "supplier cannot be null");
	}
	
	public int get()
	{
		if(supplier != null)
		{
			value = supplier.getAsInt();
			supplier = null;
		}
		
		return value;
	}
	
	public boolean got()
	{
		return supplier == null;
	}
	
	public static LazyInt of(IntSupplier supplier)
	{
		return new LazyInt(supplier);
	}
}
