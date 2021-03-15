package info.kuonteje.voxeltest.assets;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.data.EntryId;

public class AssetLoader
{
	public static InputStream assetStream(AssetType type, String domain, String id)
	{
		InputStream stream = VoxelTest.class.getResourceAsStream("/assets/" + domain + "/" + type.resolveAsset(id));
		if(stream == null) throw new AssetNotFoundException(domain, id, type);
		return stream;
	}
	
	public static InputStream assetStream(AssetType type, String id)
	{
		int colon = id.indexOf(':');
		return colon != -1 ? assetStream(type, id.substring(0, colon), id.substring(colon + 1, id.length())) : assetStream(type, VoxelTest.DEFAULT_DOMAIN, id);
	}
	
	public static InputStream assetStream(AssetType type, EntryId id)
	{
		return assetStream(type, id.getDomain(), id.getId());
	}
	
	public static String loadTextAsset(AssetType type, String domain, String id)
	{
		try(InputStream stream = assetStream(type, domain, id))
		{
			StringWriter writer = new StringWriter();
			IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
			return writer.toString();
		}
		catch(IOException e)
		{
			throw new RuntimeException("Failed to load asset \"" + domain + ":" + id + "\" of type " + type.id().toString(), e);
		}
	}
	
	public static String loadTextAsset(AssetType type, String id)
	{
		int colon = id.indexOf(':');
		return colon != -1 ? loadTextAsset(type, id.substring(0, colon), id.substring(colon + 1, id.length())) : loadTextAsset(type, VoxelTest.DEFAULT_DOMAIN, id);
	}
	
	public static String loadTextAsset(AssetType type, EntryId id)
	{
		return loadTextAsset(type, id.getDomain(), id.getId());
	}
}
