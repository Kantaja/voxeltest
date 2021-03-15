package info.kuonteje.voxeltest.util.noise;

import java.util.Random;

import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.world.worldgen.config.data.NoiseConfig;

public class NoiseUtil
{
	public static double interpNoise(double[][][] noise, int x, int y, int z, int xzShift, int yShift, int xzMask, int yMask, double xzDiv, double yDiv)
	{
		int baseNx = x >> xzShift;
		int baseNy = y >>  yShift;
		int baseNz = z >> xzShift;
		
		double xd = (x & xzMask) / xzDiv;
		double yd = (y &  yMask) /  yDiv;
		double zd = (z & xzMask) / xzDiv;
		
		double c000 = noise[baseNx    ][baseNz    ][baseNy    ];
		double c001 = noise[baseNx    ][baseNz + 1][baseNy    ];
		double c010 = noise[baseNx    ][baseNz    ][baseNy + 1];
		double c011 = noise[baseNx    ][baseNz + 1][baseNy + 1];
		double c100 = noise[baseNx + 1][baseNz    ][baseNy    ];
		double c101 = noise[baseNx + 1][baseNz + 1][baseNy    ];
		double c110 = noise[baseNx + 1][baseNz    ][baseNy + 1];
		double c111 = noise[baseNx + 1][baseNz + 1][baseNy + 1];
		
		double c00 = MathUtil.lerp(c000, c100, xd);
		double c01 = MathUtil.lerp(c001, c101, xd);
		double c10 = MathUtil.lerp(c010, c110, xd);
		double c11 = MathUtil.lerp(c011, c111, xd);
		
		double c0 = MathUtil.lerp(c00, c10, yd);
		double c1 = MathUtil.lerp(c01, c11, yd);
		
		return MathUtil.lerp(c0, c1, zd);
	}
	
	public static double interpNoise(double[][][] noise, int x, int y, int z, int shift, int mask, double div)
	{
		return interpNoise(noise, x, y, z, shift, shift, mask, mask, div, div);
	}
	
	public static double interpNoise(double[][] noise, int x, int y, int xShift, int yShift, int xMask, int yMask, double xDiv, double yDiv)
	{
		int baseNx = x >> xShift;
		int baseNy = y >> yShift;
		
		double xd = (x & xMask) / xDiv;
		double yd = (y & yMask) / yDiv;
		
		double c00 = noise[baseNx    ][baseNy    ];
		double c01 = noise[baseNx    ][baseNy + 1];
		double c10 = noise[baseNx + 1][baseNy    ];
		double c11 = noise[baseNx + 1][baseNy + 1];
		
		double c0 = MathUtil.lerp(c00, c10, xd);
		double c1 = MathUtil.lerp(c01, c11, xd);
		
		return MathUtil.lerp(c0, c1, yd);
	}
	
	public static double interpNoise(double[][] noise, int x, int y, int shift, int mask, double div)
	{
		return interpNoise(noise, x, y, shift, shift, mask, mask, div, div);
	}
	
	public static int correctSampleFrequency(int freq, int defaultFreq)
	{
		int oldFreq = freq;
		
		freq = Integer.highestOneBit(freq);
		
		if(freq <= 0)
		{
			freq = defaultFreq;
			System.out.println("Using default " + freq + " for noise sampling frequency (" + oldFreq + " <= 0)");
		}
		else if(freq != oldFreq) System.out.println("Rounding noise sampling frequency to power of 2: " + oldFreq + " -> " + freq);
		
		return freq;
	}
	
	public static INoiseGenerator generator(NoiseConfig config, int noiseSeed, long offsetSeed)
	{
		return new DefaultNoiseGenerator(config, noiseSeed, offsetSeed);
	}
	
	public static INoiseGenerator generator(NoiseConfig config, Random seedifier)
	{
		return new DefaultNoiseGenerator(config, seedifier.nextInt(), (config.offsetFactors() == null || config.offsetFactors().zero()) ? 0L : seedifier.nextLong());
	}
}
