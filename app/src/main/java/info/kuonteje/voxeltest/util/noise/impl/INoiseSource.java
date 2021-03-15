package info.kuonteje.voxeltest.util.noise.impl;

public interface INoiseSource
{
	// I don't like this workaround, but it saves creating an object for every invocation
	static interface _IPassthrough2d
	{
		double apply(int seed, double x, double y);
	}
	
	default double _transform2d(int seed, double x, double y, _IPassthrough2d passthrough)
	{
		return passthrough.apply(seed, x, y);
	}
	
	static interface _IPassthrough3d
	{
		double apply(int seed, double x, double y, double z);
	}
	
	default double _transform3d(int seed, double x, double y, double z, _IPassthrough3d passthrough)
	{
		return passthrough.apply(seed, x, y, z);
	}
	
	double _noiseImpl(int seed, double x, double y);
	double _noiseImpl(int seed, double x, double y, double z);
	
	default double noise(int seed, double x, double y)
	{
		return _noiseImpl(seed, x, y);
	}
	
	default double noise(int seed, double x, double y, double z)
	{
		return _noiseImpl(seed, x, y, z);
	}
}
