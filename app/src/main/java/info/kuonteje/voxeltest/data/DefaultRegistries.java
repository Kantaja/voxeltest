package info.kuonteje.voxeltest.data;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.render.BlockTexture;
import info.kuonteje.voxeltest.render.block.BlockModel;
import info.kuonteje.voxeltest.render.block.CubeModel;
import info.kuonteje.voxeltest.world.biome.Biome;

public class DefaultRegistries
{
	public static final Registry<Block> BLOCKS = RegistryManager.getRegistry(Block.class);
	public static final Registry<BlockTexture> BLOCK_TEXTURES = RegistryManager.getRegistry(BlockTexture.class, new BlockTexture("missing"));
	public static final Registry<BlockModel> BLOCK_MODELS = RegistryManager.getRegistry(BlockModel.class, new CubeModel("missing"));
	public static final Registry<Biome> BIOMES = RegistryManager.getRegistry(Biome.class);
	
	public static void init()
	{
		// static init
	}
}
