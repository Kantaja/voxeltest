package info.kuonteje.voxeltest.util;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;

public class MathUtil
{
	private static final double LOG2 = Math.log(2.0);
	
	public static float clamp(float v, float min, float max)
	{
		return v < min ? min : (v > max ? max : v);
	}
	
	public static double clamp(double v, double min, double max)
	{
		return v < min ? min : (v > max ? max : v);
	}
	
	public static int clamp(int v, int min, int max)
	{
		return v < min ? min : (v > max ? max : v);
	}
	
	public static long clamp(long v, long min, long max)
	{
		return v < min ? min : (v > max ? max : v);
	}
	
	public static float saturate(float v)
	{
		return clamp(v, 0.0F, 1.0F);
	}
	
	public static double saturate(double v)
	{
		return clamp(v, 0.0, 1.0);
	}
	
	public static boolean isPowerOf2(int i)
	{
		return i > 0 && (i & (i - 1)) == 0;
	}
	
	public static int fastFloor(float f)
	{
		int fi = (int)f;
		return f < fi ? fi - 1 : fi;
	}
	
	public static int fastFloor(double d)
	{
		int dl = (int)d;
		return d < dl ? dl - 1 : dl;
	}
	
	public static int fastCeil(float f)
	{
		int fi = (int)f;
		return f > fi ? fi + 1 : fi;
	}
	
	public static int fastCeil(double d)
	{
		int dl = (int)d;
		return d > dl ? dl + 1 : dl;
	}
	
	public static float log2(float f)
	{
		return (float)(Math.log(f) / LOG2);
	}
	
	public static double log2(double d)
	{
		return Math.log(d) / LOG2;
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
	
	public static Vector2f lerp(Vector2f v0, Vector2f v1, float t, Vector2f dst)
	{
		return dst.set(lerp(v0.x, v1.x, t), lerp(v0.y, v1.y, t));
	}
	
	public static Vector2d lerp(Vector2d v0, Vector2d v1, double t, Vector2d dst)
	{
		return dst.set(lerp(v0.x, v1.x, t), lerp(v0.y, v1.y, t));
	}
	
	public static Vector3f lerp(Vector3f v0, Vector3f v1, float t, Vector3f dst)
	{
		return dst.set(lerp(v0.x, v1.x, t), lerp(v0.y, v1.y, t), lerp(v0.z, v1.z, t));
	}
	
	public static Vector3d lerp(Vector3d v0, Vector3d v1, double t, Vector3d dst)
	{
		return dst.set(lerp(v0.x, v1.x, t), lerp(v0.y, v1.y, t), lerp(v0.z, v1.z, t));
	}
	
	public static Vector4f lerp(Vector4f v0, Vector4f v1, float t, Vector4f dst)
	{
		return dst.set(lerp(v0.x, v1.x, t), lerp(v0.y, v1.y, t), lerp(v0.z, v1.z, t), lerp(v0.w, v1.w, t));
	}
	
	public static Vector4d lerp(Vector4d v0, Vector4d v1, double t, Vector4d dst)
	{
		return dst.set(lerp(v0.x, v1.x, t), lerp(v0.y, v1.y, t), lerp(v0.z, v1.z, t), lerp(v0.w, v1.w, t));
	}
	
	public static float lerpClamped(float v0, float v1, float t)
	{
		return t <= 0.0F ? v0 : (t >= 1.0F ? v1 : (v0 + t * (v1 - v0)));
	}
	
	public static double lerpClamped(double v0, double v1, double t)
	{
		return t <= 0.0 ? v0 : (t >= 1.0 ? v1 : (v0 + t * (v1 - v0)));
	}
	
	public static long ipow(long x, long n)
	{
		if(n == 0L) return 1L;
		
		if(n < 0L)
		{
			x = 1L / x;
			n = -n;
		}
		
		long y = 1L;
		
		while(n > 1L)
		{
			if((n & 1L) == 0)
			{
				x *= x;
				n /= 2;
			}
			else
			{
				y *= x;
				x *= x;
				n = (n - 1L) / 2L;
			}
		}
		
		return x * y;
	}
	
	public static double roundPlaces(double d, int places)
	{
		if(places < 1) return Math.floor(d);
		
		double m = ipow(10L, places);
		return Math.round(d * m) / m;
	}
	
	public static double roundDisplay(double d)
	{
		return Math.round(d * 100.0) / 100.0;
	}
	
	public static double signedPow(double d, double power)
	{
		return Math.copySign(Math.pow(Math.abs(d), power), d);
	}
	
	public static int ceilDiv(int a, int b)
	{
		return (a + b - 1) / b;
	}
}
