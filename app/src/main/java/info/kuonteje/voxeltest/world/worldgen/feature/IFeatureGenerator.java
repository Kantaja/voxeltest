package info.kuonteje.voxeltest.world.worldgen.feature;

import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.World;

public interface IFeatureGenerator
{
	void tryGenerateIn(World world, Chunk chunk);
}
