package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.assets.TextureLoader;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;
import info.kuonteje.voxeltest.render.BlockTexture;
import info.kuonteje.voxeltest.render.TextureArray;

public class BlockTextures
{
	public static final Registry<BlockTexture> REGISTRY = DefaultRegistries.BLOCK_TEXTURES;
	
	public static final BlockTexture MISSING = REGISTRY.getDefaultValue();
	
	public static final BlockTexture STONE = REGISTRY.register(new BlockTexture("stone"));
	public static final BlockTexture DIRT = REGISTRY.register(new BlockTexture("dirt"));
	public static final BlockTexture GRASS_SIDE = REGISTRY.register(new BlockTexture("grass_side"));
	public static final BlockTexture GRASS_TOP = REGISTRY.register(new BlockTexture("grass_top"));
	public static final BlockTexture WATER = REGISTRY.register(new BlockTexture("water"));
	public static final BlockTexture SAND = REGISTRY.register(new BlockTexture("sand"));
	
	private static TextureArray array = null;
	
	public static void init()
	{
		REGISTRY.addFreezeCallback(r ->
		{
			array = new TextureArray(32, 32, r.size());
			
			array.addTexture(0, "", TextureLoader.MISSING_PROVIDER);
			
			for(BlockTexture texture : r)
			{
				if(texture == MISSING) continue;
				// would prefer not to have to get idx again
				array.addTexture(r.getIdx(texture), texture.getTextureId(), null);
			}
			
			array.finalizeArray();
		});
	}
	
	public static TextureArray getArray()
	{
		return array;
	}
}
