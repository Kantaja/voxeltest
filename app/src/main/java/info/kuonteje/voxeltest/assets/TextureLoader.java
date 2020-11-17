package info.kuonteje.voxeltest.assets;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL21C.*;
import static org.lwjgl.opengl.GL45C.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.system.MemoryUtil;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.data.objects.BlockTextures;
import info.kuonteje.voxeltest.render.ITextureProvider;
import info.kuonteje.voxeltest.render.Texture;
import info.kuonteje.voxeltest.util.MathUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class TextureLoader
{
	public static final CvarI64 rMipmap = VoxelTest.CONSOLE.cvars().getCvarI64("r_mipmap", 1, Cvar.Flags.CONFIG | Cvar.Flags.LATCH, CvarI64.BOOL_TRANSFORMER);
	public static final CvarI64 rForceMipmap = VoxelTest.CONSOLE.cvars().getCvarI64("r_force_mipmap", 0, Cvar.Flags.CONFIG | Cvar.Flags.LATCH, CvarI64.BOOL_TRANSFORMER);
	
	private static final Map<String, Texture> cache = Collections.synchronizedMap(new Object2ObjectOpenHashMap<>());
	
	static
	{
		VoxelTest.addShutdownHook(() ->
		{
			cache.forEach((i, t) ->
			{
				if(!t.isDestroyed()) t.destroy();
			});
		});
	}
	
	public static final ITextureProvider DEFAULT_PROVIDER = (domain, id) ->
	{
		BufferedImage image;
		
		try(InputStream stream = AssetLoader.getAssetStream(AssetType.TEXTURE, domain, id))
		{
			image = ImageIO.read(stream);
		}
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		int[] rgba = new int[width * height];
		image.getRGB(0, 0, width, height, rgba, 0, width);
		
		ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int pixel = rgba[y * width + x];
				
				buffer.put((byte)((pixel >> 16) & 0xFF));
				buffer.put((byte)((pixel >> 8) & 0xFF));
				buffer.put((byte)(pixel & 0xFF));
				buffer.put((byte)((pixel >> 24) & 0xFF));
			}
		}
		
		return new ITextureProvider.TextureData(width, height, buffer.flip());
	};
	
	public static final ITextureProvider MISSING_PROVIDER = (domain, id) ->
	{
		int blockTextureSize = BlockTextures.getBlockTextureSize();
		int halfShift = (int)BlockTextures.rLog2BlockTextureSize.get() - 1;
		
		ByteBuffer buffer = MemoryUtil.memAlloc(blockTextureSize * blockTextureSize * 4);
		
		for(int y = 0; y < blockTextureSize; y++)
		{
			for(int x = 0; x < blockTextureSize; x++)
			{
				boolean magenta = (x >> halfShift == 0) == (y >> halfShift == 0);
				
				buffer.put((byte)(magenta ? 0xFF : 0x00));
				buffer.put((byte)0x00);
				buffer.put((byte)(magenta ? 0xFF : 0x00));
				buffer.put((byte)0xFF);
			}
		}
		
		return new ITextureProvider.TextureData(blockTextureSize, blockTextureSize, buffer.flip());
	};
	
	public static Texture loadTexture(String domain, String id, ITextureProvider provider, int mipmapBase)
	{
		String cacheId = domain + ":" + id;
		Texture cached = cache.get(cacheId);
		
		if(cached != null)
		{
			if(cached.isDestroyed()) cache.remove(cacheId);
			else return cached;
		}
		
		provider = provider == null ? DEFAULT_PROVIDER : provider;
		
		ITextureProvider.TextureData data;
		
		try
		{
			data = provider.getTextureData(domain, id, MISSING_PROVIDER);
		}
		catch(Exception e)
		{
			throw new RuntimeException("Failed to load asset \"" + domain + ":" + id + "\" of type " + AssetType.TEXTURE.toString(), e);
		}
		
		int width, height;
		boolean mipmap;
		int texture;
		
		try
		{
			width = data.width();
			height = data.height();
			
			mipmap = rForceMipmap.getAsBool() || (width == height && MathUtil.isPowerOf2(width) && rMipmap.getAsBool());
			
			texture = glCreateTextures(GL_TEXTURE_2D);
			
			glTextureStorage2D(texture, mipmap ? (1 + (int)Math.round(MathUtil.log2((double)width)) - mipmapBase) : 1, GL_SRGB8_ALPHA8, width, height);
			glTextureSubImage2D(texture, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, data.data());
		}
		finally
		{
			data.destroy();
		}
		
		if(mipmap) glGenerateTextureMipmap(texture);
		
		glTextureParameteri(texture, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTextureParameteri(texture, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		glTextureParameteri(texture, GL_TEXTURE_MIN_FILTER, mipmap ? GL_NEAREST_MIPMAP_LINEAR : GL_NEAREST);
		glTextureParameteri(texture, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		Texture result = Texture.wrap(width, height, texture);
		
		cache.put(cacheId, result);
		
		return result;
	}
	
	public static Texture loadTexture(String id, ITextureProvider provider, int mipmapBase)
	{
		int colon = id.indexOf(':');
		return colon != -1 ? loadTexture(id.substring(0, colon), id.substring(colon + 1, id.length()), provider, mipmapBase) : loadTexture(VoxelTest.DEFAULT_DOMAIN, id, provider, mipmapBase);
	}
}
