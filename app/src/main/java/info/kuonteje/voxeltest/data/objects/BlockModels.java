package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;
import info.kuonteje.voxeltest.render.block.BlockModel;
import info.kuonteje.voxeltest.render.block.CubeModel;

public class BlockModels
{
	public static final Registry<BlockModel> REGISTRY = DefaultRegistries.BLOCK_MODELS;
	
	public static final BlockModel MISSING = REGISTRY.getDefaultValue();
	
	public static final BlockModel STONE = REGISTRY.register(new CubeModel(Blocks.STONE).setAll(BlockTextures.STONE));
	public static final BlockModel DIRT = REGISTRY.register(new CubeModel(Blocks.DIRT).setAll(BlockTextures.DIRT));
	public static final BlockModel GRASS = REGISTRY.register(new CubeModel(Blocks.GRASS).setTop(BlockTextures.GRASS_TOP).setSide(BlockTextures.GRASS_SIDE).setBottom(BlockTextures.DIRT));
	public static final BlockModel WATER = REGISTRY.register(new CubeModel(Blocks.WATER)/*.setTint(0x3F76E4)*/.setAll(BlockTextures.WATER));
	public static final BlockModel SAND = REGISTRY.register(new CubeModel(Blocks.SAND).setAll(BlockTextures.SAND));
	public static final BlockModel GLASS = REGISTRY.register(new CubeModel(Blocks.GLASS).setAll(BlockTextures.GLASS));
	public static final BlockModel LOG = REGISTRY.register(new CubeModel(Blocks.LOG).setTopBottom(BlockTextures.LOG_TOP).setSide(BlockTextures.LOG_SIDE));
	public static final BlockModel LEAVES = REGISTRY.register(new CubeModel(Blocks.LEAVES).setAll(BlockTextures.LEAVES));
	
	public static void init()
	{
		// static init
	}
}
