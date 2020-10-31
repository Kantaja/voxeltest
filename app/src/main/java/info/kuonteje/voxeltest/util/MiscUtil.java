package info.kuonteje.voxeltest.util;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MiscUtil
{
	public static <T> List<T> flatten(List<List<T>> list)
	{
		List<T> result = new ObjectArrayList<>();
		list.forEach(sub -> sub.forEach(result::add));
		return result;
	}
}
