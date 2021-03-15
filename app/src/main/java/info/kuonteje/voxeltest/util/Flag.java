package info.kuonteje.voxeltest.util;

public class Flag
{
	private boolean set = false;
	
	public void set()
	{
		set = true;
	}
	
	public void clear()
	{
		set = false;
	}
	
	public boolean test()
	{
		return set;
	}
}
