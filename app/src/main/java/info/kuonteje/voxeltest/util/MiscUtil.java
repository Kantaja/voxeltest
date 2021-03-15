package info.kuonteje.voxeltest.util;

import java.util.List;
import java.util.Random;

import info.kuonteje.voxeltest.repack.it.unimi.dsi.util.XoShiRo256PlusPlusRandom;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MiscUtil
{
	public static <T> List<T> flatten(List<List<T>> list)
	{
		List<T> result = new ObjectArrayList<>();
		list.forEach(sub -> sub.forEach(result::add));
		return result;
	}
	
	private static final XoShiRo256PlusPlusRandom seedGenerator = new XoShiRo256PlusPlusRandom(System.nanoTime());
	
	public static Random randomGenerator(long seed)
	{
		return new XoShiRo256PlusPlusRandom(seed);
	}
	
	public static Random randomGenerator()
	{
		return randomGenerator(randomSeed());
	}
	
	// avoid dependency on it.unimi.dsi.Util
	// do we really need the nanoTime?
	public static long randomSeed()
	{
		long nanoTime = System.nanoTime();
		
		synchronized(seedGenerator)
		{
			return seedGenerator.nextLong() ^ nanoTime;
		}
	}
}
