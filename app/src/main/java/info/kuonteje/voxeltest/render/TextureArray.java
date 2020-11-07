package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL45C.*;

import info.kuonteje.voxeltest.assets.AssetType;
import info.kuonteje.voxeltest.assets.TextureLoader;
import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.util.MathUtil;

public class TextureArray
{
	private static final int maxArrayTextures;
	
	static
	{
		maxArrayTextures = glGetInteger(GL_MAX_ARRAY_TEXTURE_LAYERS);
	}
	
	private final Texture texture;
	private final int width, height, layers;
	private final boolean mipmap;
	
	private boolean finalized = false;
	
	public TextureArray(int width, int height, int layers)
	{
		if(layers > maxArrayTextures) throw new RuntimeException("Cannot create texture array of " + layers + " layers, max is " + maxArrayTextures);
		
		int array = glCreateTextures(GL_TEXTURE_2D_ARRAY);
		
		this.width = width;
		this.height = height;
		this.layers = layers;
		
		mipmap = TextureLoader.rForceMipmap.getAsBool() || (width == height && MathUtil.isPowerOf2(width) && TextureLoader.rMipmap.getAsBool());
		
		glTextureParameteri(array, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTextureParameteri(array, GL_TEXTURE_WRAP_T, GL_REPEAT);
		
		glTextureParameteri(array, GL_TEXTURE_MIN_FILTER, mipmap ? GL_NEAREST_MIPMAP_LINEAR : GL_NEAREST);
		glTextureParameteri(array, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		glTextureStorage3D(array, mipmap ? (1 + (int)Math.round(MathUtil.log2((double)width))) : 1, GL_RGBA8, width, height, layers);
		
		texture = Texture.wrap(width, height, array);
	}
	
	public void addTexture(int layer, EntryId textureId, ITextureProvider provider)
	{
		if(finalized) throw new RuntimeException("Failed to add asset \"" + textureId.toString() + "\" of type " +
				AssetType.TEXTURE.toString() + " to texture array on layer " + layer + ": array already finalized");
		
		if(layer >= layers) throw new RuntimeException("Failed to add asset \"" + textureId.toString() + "\" of type " +
				AssetType.TEXTURE.toString() + " to texture array on layer " + layer + ": layer out of range [0," + (layers - 1) + ")");
		
		provider = provider == null ? TextureLoader.DEFAULT_PROVIDER : provider;
		
		ITextureProvider.TextureData data;
		
		try
		{
			data = provider.getTextureData(textureId.getDomain(), textureId.getId(), TextureLoader.MISSING_PROVIDER);
		}
		catch(Exception e)
		{
			throw new RuntimeException("Failed to add asset \"" + textureId.toString() + "\" of type " + AssetType.TEXTURE.toString() + " to texture array on layer " + layer, e);
		}
		
		try
		{
			if(width != data.width() || height != data.height()) throw new RuntimeException("Failed to add asset \"" + textureId.toString() + "\" of type " +
					AssetType.TEXTURE.toString() + " to texture array on layer " + layer + ": dimensions do not match (object " +
					data.width() + "x" + data.height() + ", array " + width + "x" + height + ")");
			
			glTextureSubImage3D(texture.handle(), 0, 0, 0, layer, width, height, 1, GL_RGBA, GL_UNSIGNED_BYTE, data.data());
		}
		finally
		{
			data.destroy();
		}
	}
	
	public void addTexture(int layer, String id, ITextureProvider provider)
	{
		addTexture(layer, EntryId.create(id), provider);
	}
	
	public void finalizeArray()
	{
		finalized = true;
		
		if(mipmap) glGenerateTextureMipmap(texture.handle());
	}
	
	public void bind(int unit)
	{
		texture.bind(unit);
	}
	
	public void unbind(int unit)
	{
		texture.unbind(unit);
	}
	
	public void destroy()
	{
		texture.destroy();
	}
}
