package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL21C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL45C.*;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.assets.AssetType;
import info.kuonteje.voxeltest.assets.TextureLoader;
import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.util.Lazy;
import info.kuonteje.voxeltest.util.LazyInt;

public class TextureArray implements ITexture<TextureArray>
{
	private static final LazyInt maxArrayTextures = LazyInt.of(() -> glGetInteger(GL_MAX_ARRAY_TEXTURE_LAYERS));
	
	private final SingleTexture texture;
	private final int width, height, layers;
	private final boolean mipmap;
	
	private final Lazy<TextureHandle<TextureArray>> bindlessHandle;
	
	private boolean finalized = false;
	
	public TextureArray(int width, int height, int layers)
	{
		if(layers > maxArrayTextures.get()) throw new RuntimeException("Cannot create texture array of " + layers + " layers, max is " + maxArrayTextures.get());
		
		VoxelTest.addDestroyable(this);
		
		int array = glCreateTextures(GL_TEXTURE_2D_ARRAY);
		
		this.width = width;
		this.height = height;
		this.layers = layers;
		
		mipmap = TextureLoader.rMipmap.getAsBool();
		
		glTextureParameteri(array, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTextureParameteri(array, GL_TEXTURE_WRAP_T, GL_REPEAT);
		
		glTextureParameteri(array, GL_TEXTURE_MIN_FILTER, mipmap ? GL_NEAREST_MIPMAP_LINEAR : GL_NEAREST);
		glTextureParameteri(array, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		glTextureParameterf(array, GL_TEXTURE_MAX_ANISOTROPY_EXT, TextureLoader.rTextureAnisotropy.get());
		
		glTextureStorage3D(array, mipmap ? SingleTexture.calculateMipLevels(width, height, 1) : 1, GL_SRGB8_ALPHA8, width, height, layers);
		
		texture = SingleTexture.wrap(width, height, layers, array);
		
		bindlessHandle = Lazy.of(() -> new TextureHandle<>(this, glGetTextureHandleARB(array)));
	}
	
	public void addTexture(int layer, EntryId textureId, ITextureProvider provider)
	{
		if(finalized) throw new RuntimeException("Failed to add asset \"" + textureId.toString() + "\" of type " +
				AssetType.TEXTURE.toString() + " to texture array on layer " + layer + ": array already finalized");
		
		if(layer >= layers) throw new RuntimeException("Failed to add asset \"" + textureId.toString() + "\" of type " +
				AssetType.TEXTURE.toString() + " to texture array on layer " + layer + ": layer out of range [0," + (layers - 1) + ")");
		
		if(provider == null) provider = TextureLoader.DEFAULT_PROVIDER;
		
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
		
		if(mipmap) texture.generateMipmaps();
	}
	
	@Override
	public int handle()
	{
		return texture.handle();
	}
	
	@Override
	public TextureHandle<TextureArray> getBindlessHandle(boolean makeResident)
	{
		TextureHandle<TextureArray> handle = bindlessHandle.get();
		return makeResident ? handle.makeResident() : handle;
	}
	
	@Override
	public int width()
	{
		return width;
	}
	
	@Override
	public int height()
	{
		return height;
	}
	
	@Override
	public int depth()
	{
		return layers;
	}
	
	@Override
	public void bind(int unit)
	{
		texture.bind(unit);
	}
	
	@Override
	public void unbind(int unit)
	{
		texture.unbind(unit);
	}
	
	@Override
	public void destroy()
	{
		texture.destroy();
		VoxelTest.removeDestroyable(this);
	}
}
