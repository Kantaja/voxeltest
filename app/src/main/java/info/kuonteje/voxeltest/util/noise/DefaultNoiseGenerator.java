package info.kuonteje.voxeltest.util.noise;

import java.util.Random;

import info.kuonteje.voxeltest.repack.fastnoise.FractalType;
import info.kuonteje.voxeltest.util.MiscUtil;
import info.kuonteje.voxeltest.util.noise.impl.INoiseSource;
import info.kuonteje.voxeltest.util.noise.impl.fractal.FractalBillowNoiseGenerator;
import info.kuonteje.voxeltest.util.noise.impl.fractal.FractalFbmNoiseGenerator;
import info.kuonteje.voxeltest.util.noise.impl.fractal.FractalPingPongNoiseGenerator;
import info.kuonteje.voxeltest.util.noise.impl.fractal.FractalRidgedNoiseGenerator;
import info.kuonteje.voxeltest.util.noise.impl.single.SingleCellularNoiseGenerator;
import info.kuonteje.voxeltest.util.noise.impl.single.SingleOpenSimplex2NoiseGenerator;
import info.kuonteje.voxeltest.util.noise.impl.single.SingleOpenSimplex2SNoiseGenerator;
import info.kuonteje.voxeltest.util.noise.impl.single.SinglePerlinNoiseGenerator;
import info.kuonteje.voxeltest.util.noise.impl.single.SingleValueCubicNoiseGenerator;
import info.kuonteje.voxeltest.util.noise.impl.single.SingleValueNoiseGenerator;
import info.kuonteje.voxeltest.world.worldgen.config.data.NoiseConfig;

public class DefaultNoiseGenerator implements INoiseGenerator
{
	private final INoiseSource source;
	private final int seed;
	
	private final double xzFrequency;
	private final double yFrequency;
	
	private final double offX, offY, offZ;
	
	private final double noiseScale;
	private final boolean zeroToOne;
	
	public DefaultNoiseGenerator(NoiseConfig config, int noiseSeed, long offsetSeed)
	{
		this.seed = noiseSeed;
		
		this.xzFrequency = config.xzFrequency();
		this.yFrequency = config.yFrequency();
		
		this.noiseScale = config.scale();
		this.zeroToOne = config.zeroToOne();
		
		INoiseSource source = switch(config.type()) {
		case OPENSIMPLEX2 -> new SingleOpenSimplex2NoiseGenerator();
		case OPENSIMPLEX2S -> new SingleOpenSimplex2SNoiseGenerator();
		case CELLULAR -> new SingleCellularNoiseGenerator(config.cellular());
		case PERLIN -> new SinglePerlinNoiseGenerator();
		case VALUE_CUBIC -> new SingleValueCubicNoiseGenerator();
		case VALUE -> new SingleValueNoiseGenerator();
		};
		
		if(config.fractal() != null && config.fractal().type() != FractalType.NONE) source = switch(config.fractal().type()) {
		case FBM -> new FractalFbmNoiseGenerator(config.fractal(), source);
		case RIDGED -> new FractalRidgedNoiseGenerator(config.fractal(), source);
		case PING_PONG -> new FractalPingPongNoiseGenerator(config.fractal(), source);
		case BILLOW -> new FractalBillowNoiseGenerator(config.fractal(), source);
		default -> throw new RuntimeException("???");
		};
		
		this.source = source;
		
		if(config.offsetFactors() != null && (config.offsetFactors().x() != 0.0 || config.offsetFactors().y() != 0.0 || config.offsetFactors().z() != 0.0))
		{
			Random offsetRandom = MiscUtil.randomGenerator(offsetSeed);
			
			this.offX = config.offsetFactors().x() * (offsetRandom.nextDouble() - 0.5) * 2.0;
			this.offY = config.offsetFactors().y() * (offsetRandom.nextDouble() - 0.5) * 2.0;
			this.offZ = config.offsetFactors().z() * (offsetRandom.nextDouble() - 0.5) * 2.0;
		}
		else
		{
			this.offX = 0.0;
			this.offY = 0.0;
			this.offZ = 0.0;
		}
	}
	
	@Override
	public double noiseXY(double x, double y)
	{
		x = x * xzFrequency + offX;
		y = y *  yFrequency + offY;
		
		double noise = source.noise(seed, x, y);
		return (zeroToOne ? noise * 0.5 + 0.5 : noise) * noiseScale;
	}
	
	@Override
	public double noiseXZ(double x, double z)
	{
		x = x * xzFrequency + offX;
		z = z * xzFrequency + offZ;
		
		double noise = source.noise(seed, x, z);
		return (zeroToOne ? noise * 0.5 + 0.5 : noise) * noiseScale;
	}
	
	@Override
	public double noise(double x, double y, double z)
	{
		x = x * xzFrequency + offX;
		y = y *  yFrequency + offY;
		z = z * xzFrequency + offZ;
		
		double noise = source.noise(seed, x, y, z);
		return (zeroToOne ? noise * 0.5 + 0.5 : noise) * noiseScale;
	}
	
	@Override
	public double min()
	{
		return zeroToOne ? 0.0 : -noiseScale;
	}
	
	@Override
	public double max()
	{
		return noiseScale;
	}
}
