package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.assets.TextureLoader;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;
import info.kuonteje.voxeltest.render.BlockTexture;
import info.kuonteje.voxeltest.render.Renderer;
import info.kuonteje.voxeltest.render.TextureArray;
import info.kuonteje.voxeltest.render.TextureHandle;

public class BlockTextures
{
	public static final CvarI64 rBlockTextureSize;
	
	static
	{
		rBlockTextureSize = VoxelTest.CONSOLE.cvars().getCvarI64("r_block_texture_size", 16L, Cvar.Flags.CONFIG | Cvar.Flags.LATCH, v -> Long.highestOneBit(Math.max(0L, v)));
	}
	
	public static final Registry<BlockTexture> REGISTRY = DefaultRegistries.BLOCK_TEXTURES;
	
	public static final BlockTexture MISSING = REGISTRY.getDefaultValue();
	
	public static final BlockTexture STONE = REGISTRY.register(new BlockTexture("stone"));
	public static final BlockTexture DIRT = REGISTRY.register(new BlockTexture("dirt"));
	public static final BlockTexture GRASS_SIDE = REGISTRY.register(new BlockTexture("grass_side"));
	public static final BlockTexture GRASS_TOP = REGISTRY.register(new BlockTexture("grass_top"));
	public static final BlockTexture WATER = REGISTRY.register(new BlockTexture("water"));
	public static final BlockTexture SAND = REGISTRY.register(new BlockTexture("sand"));
	public static final BlockTexture GLASS = REGISTRY.register(new BlockTexture("glass"));
	public static final BlockTexture LOG_SIDE = REGISTRY.register(new BlockTexture("log_side"));
	public static final BlockTexture LOG_TOP = REGISTRY.register(new BlockTexture("log_top"));
	public static final BlockTexture LEAVES = REGISTRY.register(new BlockTexture("leaves"));
	
	private static TextureArray array = null;
	
	public static void init()
	{
		REGISTRY.addFreezeCallback(r ->
		{
			int blockTextureSize = rBlockTextureSize.getAsInt();
			array = new TextureArray(blockTextureSize, blockTextureSize, r.size());
			
			array.addTexture(0, "missing", TextureLoader.MISSING_PROVIDER);
			
			for(BlockTexture texture : r)
			{
				if(texture == MISSING) continue;
				// would prefer not to have to get idx again
				array.addTexture(r.getIdx(texture), texture.getTextureId(), null);
			}
			
			array.finalizeArray();
			
			TextureHandle<TextureArray> handle = array.getBindlessHandle();
			
			Renderer.getSolidShader().upload("texSamplers", handle);
			Renderer.getTranslucentShader().upload("texSamplers", handle);
			Renderer.getDepthShader().upload("texSamplers", handle);
		});
	}
	
	public static TextureArray getArray()
	{
		return array;
	}
}
