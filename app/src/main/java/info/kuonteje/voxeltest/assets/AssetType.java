package info.kuonteje.voxeltest.assets;

public enum AssetType
{
	SHADER("shaders", "glsl"),
	TEXTURE("textures", "png");
	
	private final String subdir, ext;
	
	private AssetType(String subdir, String ext)
	{
		this.subdir = subdir;
		this.ext = ext;
	}
	
	public String resolveAsset(String id)
	{
		return subdir + "/" + id + "." + ext;
	}
}
