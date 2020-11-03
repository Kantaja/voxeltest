package info.kuonteje.voxeltest.data;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.objects.BlockModels;
import info.kuonteje.voxeltest.data.objects.BlockTextures;
import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.data.objects.Features;
import info.kuonteje.voxeltest.render.BlockTexture;
import info.kuonteje.voxeltest.render.block.BlockModel;
import info.kuonteje.voxeltest.render.block.CubeModel;
import info.kuonteje.voxeltest.world.biome.Biome;
import info.kuonteje.voxeltest.world.worldgen.feature.Feature;

public class DefaultRegistries
{
	public static final Registry<Block> BLOCKS = RegistryManager.getRegistry(Block.class);
	public static final Registry<BlockTexture> BLOCK_TEXTURES = RegistryManager.getRegistry(BlockTexture.class, new BlockTexture("missing"));
	public static final Registry<BlockModel> BLOCK_MODELS = RegistryManager.getRegistry(BlockModel.class, new CubeModel("missing"));
	public static final Registry<Feature> FEATURES = RegistryManager.getRegistry(Feature.class, new Feature("identity", null));
	public static final Registry<Biome> BIOMES = RegistryManager.getRegistry(Biome.class);
	
	public static void init()
	{
		Blocks.init();
		BlockTextures.init();
		BlockModels.init();
		Features.init();
		// Biomes.init();
	}
}
