package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.assets.AssetType;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;

public class AssetTypes
{
	public static final Registry<AssetType> REGISTRY = DefaultRegistries.ASSET_TYPES;
	
	public static final AssetType SHADER = REGISTRY.register(new AssetType("shader", "shaders", "glsl"));
	public static final AssetType TEXTURE = REGISTRY.register(new AssetType("texture", "textures", "png"));
	public static final AssetType WORLDGEN_PROFILE = REGISTRY.register(new AssetType("worldgen_profile", "worldgen", "json"));
	
	public static void init()
	{
		// static init
	}
}
