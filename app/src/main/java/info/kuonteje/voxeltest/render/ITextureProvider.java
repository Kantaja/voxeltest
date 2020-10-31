package info.kuonteje.voxeltest.render;

import static org.lwjgl.system.MemoryUtil.*;

import java.nio.ByteBuffer;

import info.kuonteje.voxeltest.assets.AssetNotFoundException;

public interface ITextureProvider
{
	public static record TextureData(int width, int height, ByteBuffer data)
	{
		public void destroy()
		{
			memFree(data);
		}
	}
	
	TextureData getTextureData(String domain, String id) throws Exception;
	
	default TextureData getTextureData(String domain, String id, ITextureProvider fallback) throws Exception
	{
		try
		{
			return getTextureData(domain, id);
		}
		catch(AssetNotFoundException e)
		{
			e.printStackTrace();
			return fallback.getTextureData(domain, id);
		}
	}
}
