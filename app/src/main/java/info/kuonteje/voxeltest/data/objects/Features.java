package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;
import info.kuonteje.voxeltest.world.worldgen.feature.Feature;
import info.kuonteje.voxeltest.world.worldgen.feature.TreeFeatureGenerator;

public class Features
{
	public static final Registry<Feature> REGISTRY = DefaultRegistries.FEATURES;
	
	public static final Feature IDENTITY = REGISTRY.getDefaultValue();
	
	public static final Feature TREE = REGISTRY.register(new Feature("tree", seed -> new TreeFeatureGenerator()));
	
	public static void init()
	{
		// static init
	}
}
