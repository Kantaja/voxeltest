package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;
import info.kuonteje.voxeltest.render.block.BlockModel;
import info.kuonteje.voxeltest.render.block.CubeModel;

public class BlockModels
{
	public static final Registry<BlockModel> REGISTRY = DefaultRegistries.BLOCK_MODELS;
	
	public static final BlockModel MISSING = REGISTRY.defaultValue();
	
	public static final CubeModel STONE = REGISTRY.register(new CubeModel(Blocks.STONE).setAll(BlockTextures.STONE));
	public static final CubeModel DIRT = REGISTRY.register(new CubeModel(Blocks.DIRT).setAll(BlockTextures.DIRT));
	public static final CubeModel GRASS = REGISTRY.register(new CubeModel(Blocks.GRASS).setTop(BlockTextures.GRASS_TOP).setSide(BlockTextures.GRASS_SIDE).setBottom(BlockTextures.DIRT));
	public static final CubeModel WATER = REGISTRY.register(new CubeModel(Blocks.WATER).setAll(BlockTextures.WATER));
	public static final CubeModel SAND = REGISTRY.register(new CubeModel(Blocks.SAND).setAll(BlockTextures.SAND));
	public static final CubeModel GLASS = REGISTRY.register(new CubeModel(Blocks.GLASS).setAll(BlockTextures.GLASS));
	public static final CubeModel LOG = REGISTRY.register(new CubeModel(Blocks.LOG).setTopBottom(BlockTextures.LOG_TOP).setSide(BlockTextures.LOG_SIDE));
	public static final CubeModel LEAVES = REGISTRY.register(new CubeModel(Blocks.LEAVES).setAll(BlockTextures.LEAVES));
	public static final CubeModel EMERALD_ORE = REGISTRY.register(new CubeModel(Blocks.EMERALD_ORE).setAll(BlockTextures.EMERALD_ORE));
	public static final CubeModel GRAVEL = REGISTRY.register(new CubeModel(Blocks.GRAVEL).setAll(BlockTextures.GRAVEL));
	
	private static BlockModel[] modelCache;
	
	public static BlockModel getCachedModel(int blockIdx)
	{
		return modelCache[blockIdx - 1];
	}
	
	public static void init()
	{
		REGISTRY.addFreezeCallback(r ->
		{
			modelCache = new BlockModel[DefaultRegistries.BLOCKS.size()];
			
			for(BlockModel model : r.withoutDefault())
			{
				System.out.println("Caching block model " + model.id());
				modelCache[DefaultRegistries.BLOCKS.byIdRaw(model.id()).idx() - 1] = model;
			}
		});
	}
}
