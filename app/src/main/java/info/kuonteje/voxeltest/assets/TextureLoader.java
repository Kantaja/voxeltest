package info.kuonteje.voxeltest.assets;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
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
import info.kuonteje.voxeltest.data.objects.AssetTypes;
import info.kuonteje.voxeltest.data.objects.BlockTextures;
import info.kuonteje.voxeltest.render.ITextureProvider;
import info.kuonteje.voxeltest.render.SingleTexture;
import info.kuonteje.voxeltest.util.MathUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class TextureLoader
{
	public static final CvarI64 rMipmap = VoxelTest.CONSOLE.cvars().cvarBool("r_mipmap", true, Cvar.Flags.CONFIG | Cvar.Flags.LATCH, null);
	
	public static final CvarI64 rTextureAnisotropy = VoxelTest.CONSOLE.cvars().cvarI64("r_texture_anisotropy", 1L, Cvar.Flags.CONFIG | Cvar.Flags.LATCH, v -> MathUtil.clamp(Long.highestOneBit(v), 1L, 16L));
	
	private static final Map<String, SingleTexture> cache = Collections.synchronizedMap(new Object2ObjectOpenHashMap<>());
	
	public static final ITextureProvider DEFAULT_PROVIDER = (domain, id) ->
	{
		BufferedImage image;
		
		try(InputStream stream = AssetLoader.assetStream(AssetTypes.TEXTURE, domain, id))
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
		int blockTextureSize = BlockTextures.rBlockTextureSize.asInt();
		int halfShift = MathUtil.floorLog2(blockTextureSize) - 1;
		
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
	
	public static SingleTexture loadTexture(String domain, String id, ITextureProvider provider, int mipmapBase)
	{
		String cacheId = domain + ":" + id;
		SingleTexture cached = cache.get(cacheId);
		
		if(cached != null)
		{
			if(cached.isDestroyed()) cache.remove(cacheId);
			else return cached;
		}
		
		if(provider == null) provider = DEFAULT_PROVIDER;
		
		ITextureProvider.TextureData data;
		
		try
		{
			data = provider.getTextureData(domain, id, MISSING_PROVIDER);
		}
		catch(Exception e)
		{
			throw new RuntimeException("Failed to load asset \"" + domain + ":" + id + "\" of type " + AssetTypes.TEXTURE.id().toString(), e);
		}
		
		int width, height;
		boolean mipmap;
		
		SingleTexture texture;
		
		try
		{
			width = data.width();
			height = data.height();
			
			mipmap = rMipmap.asBool();
			
			texture = SingleTexture.alloc2D(width, height, GL_SRGB8_ALPHA8, mipmap ? SingleTexture.calculateMipLevels(width, height, 1) - mipmapBase : 1);
			glTextureSubImage2D(texture.handle(), 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, data.data());
		}
		finally
		{
			data.destroy();
		}
		
		if(mipmap) texture.generateMipmaps();
		
		glTextureParameteri(texture.handle(), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTextureParameteri(texture.handle(), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		glTextureParameteri(texture.handle(), GL_TEXTURE_MIN_FILTER, mipmap ? GL_NEAREST_MIPMAP_LINEAR : GL_NEAREST);
		glTextureParameteri(texture.handle(), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		glTextureParameterf(texture.handle(), GL_TEXTURE_MAX_ANISOTROPY_EXT, rTextureAnisotropy.get());
		
		cache.put(cacheId, texture);
		
		return texture;
	}
	
	public static SingleTexture loadTexture(String id, ITextureProvider provider, int mipmapBase)
	{
		int colon = id.indexOf(':');
		return colon != -1 ? loadTexture(id.substring(0, colon), id.substring(colon + 1, id.length()), provider, mipmapBase) : loadTexture(VoxelTest.DEFAULT_DOMAIN, id, provider, mipmapBase);
	}
}
