package info.kuonteje.voxeltest.assets;

public class AssetNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	
	public AssetNotFoundException(String domain, String id, AssetType type)
	{
		super("Failed to load asset \"" + domain + ":" + id + "\" of type " + type.id().toString() + " - asset not found");
	}
}
