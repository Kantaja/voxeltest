package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;
import info.kuonteje.voxeltest.world.worldgen.feature.Feature;
import info.kuonteje.voxeltest.world.worldgen.feature.generator.BlockReplacementFeatureGenerator;
import info.kuonteje.voxeltest.world.worldgen.feature.generator.TreeFeatureGenerator;

public class Features
{
	public static final Registry<Feature<?>> REGISTRY = DefaultRegistries.FEATURES;
	
	public static final Feature<?> IDENTITY = REGISTRY.defaultValue();
	
	public static final Feature<TreeFeatureGenerator.Config> TREE = REGISTRY.register(new Feature<>("tree", TreeFeatureGenerator.Config.class, TreeFeatureGenerator.DEFAULT_CONFIG, TreeFeatureGenerator::factory));
	public static final Feature<BlockReplacementFeatureGenerator.Config> SINGLE_BLOCK_REPLACEMENT = REGISTRY.register(new Feature<>("single_block_replacement", BlockReplacementFeatureGenerator.Config.class, null, BlockReplacementFeatureGenerator::factory));
	
	public static void init()
	{
		// static init
	}
}
