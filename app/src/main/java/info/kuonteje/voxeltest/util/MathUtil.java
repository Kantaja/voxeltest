package info.kuonteje.voxeltest.util;

import org.joml.Vector3d;
import org.joml.Vector3f;

public class MathUtil
{
	private static final double LOG2 = Math.log(2.0);
	
	public static double clamp(double v, double min, double max)
	{
		return v < min ? min : (v > max ? max : v);
	}
	
	public static long clamp(long v, long min, long max)
	{
		return v < min ? min : (v > max ? max : v);
	}
	
	public static boolean isPowerOf2(int i)
	{
		return i > 0 && (i & (i - 1)) == 0;
	}
	
	public static int fastFloor(double d)
	{
		int dl = (int)d;
		return d < dl ? dl - 1 : dl;
	}
	
	public static int fastFloor(float f)
	{
		int fi = (int)f;
		return f < fi ? fi - 1 : fi;
	}
	
	public static int fastCeil(double d)
	{
		int dl = (int)d;
		return d > dl ? dl + 1 : dl;
	}
	
	public static int fastCeil(float f)
	{
		int fi = (int)f;
		return f > fi ? fi + 1 : fi;
	}
	
	public static double log2(double d)
	{
		return Math.log(d) / LOG2;
	}
	
	public static float log2(float f)
	{
		return (float)(Math.log(f) / LOG2);
	}
	
	public static int floorLog2(int i)
	{
		return 31 - Integer.numberOfLeadingZeros(i);
	}
	
	public static int ceilLog2(int i)
	{
		return 32 - Integer.numberOfLeadingZeros(i - 1);
	}
	
	public static int bitLength(int i)
	{
		return 32 - Integer.numberOfLeadingZeros(i);
	}
	
	public static float lerp(float v0, float v1, float t)
	{
		return v0 + t * (v1 - v0);
	}
	
	public static double lerp(double v0, double v1, double t)
	{
		return v0 + t * (v1 - v0);
	}
	
	public static Vector3f lerp(Vector3f v0, Vector3f v1, float t, Vector3f dst)
	{
		return dst.set(lerp(v0.x, v1.x, t), lerp(v0.y, v1.y, t), lerp(v0.z, v1.z, t));
	}
	
	public static Vector3d lerp(Vector3d v0, Vector3d v1, double t, Vector3d dst)
	{
		return dst.set(lerp(v0.x, v1.x, t), lerp(v0.y, v1.y, t), lerp(v0.z, v1.z, t));
	}
}
