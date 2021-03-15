package info.kuonteje.voxeltest.data;

import info.kuonteje.voxeltest.assets.AssetType;
import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.objects.AssetTypes;
import info.kuonteje.voxeltest.data.objects.BlockModels;
import info.kuonteje.voxeltest.data.objects.BlockTextures;
import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.data.objects.Features;
import info.kuonteje.voxeltest.data.objects.GenerationStages;
import info.kuonteje.voxeltest.data.objects.WorldgenProfiles;
import info.kuonteje.voxeltest.render.BlockTexture;
import info.kuonteje.voxeltest.render.block.BlockModel;
import info.kuonteje.voxeltest.render.block.CubeModel;
import info.kuonteje.voxeltest.world.worldgen.config.WorldgenProfile;
import info.kuonteje.voxeltest.world.worldgen.feature.Feature;
import info.kuonteje.voxeltest.world.worldgen.stage.GenerationStage;

public class DefaultRegistries
{
	public static final Registry<AssetType> ASSET_TYPES = RegistryManager.getRegistry(AssetType.class);
	public static final Registry<Block> BLOCKS = RegistryManager.getRegistry(Block.class);
	public static final Registry<BlockTexture> BLOCK_TEXTURES = RegistryManager.getRegistry(BlockTexture.class, new BlockTexture("missing"));
	public static final Registry<BlockModel> BLOCK_MODELS = RegistryManager.getRegistry(BlockModel.class, new CubeModel("missing"));
	public static final Registry<Feature<?>> FEATURES = RegistryManager.getRegistry(Feature.class);
	public static final Registry<GenerationStage<?>> GENERATION_STAGES = RegistryManager.getRegistry(GenerationStage.class);
	public static final Registry<WorldgenProfile> WORLDGEN_PROFILES = RegistryManager.getRegistry(WorldgenProfile.class, new WorldgenProfile("default"));
	
	public static void init()
	{
		AssetTypes.init();
		Blocks.init();
		BlockTextures.init();
		BlockModels.init();
		Features.init();
		GenerationStages.init();
		WorldgenProfiles.init();
	}
}
