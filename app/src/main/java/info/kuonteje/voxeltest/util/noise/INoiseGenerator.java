package info.kuonteje.voxeltest.util.noise;

public interface INoiseGenerator
{
	double noiseXY(double x, double y);
	double noiseXZ(double x, double z);
	double noise(double x, double y, double z);
	
	double min();
	double max();
}
