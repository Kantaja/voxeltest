package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL45C.*;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.util.Lazy;
import info.kuonteje.voxeltest.util.MathUtil;

public class SingleTexture implements ITexture<SingleTexture>
{
	private static final int defaultFilter = GL_NEAREST;
	private static final int defaultWrapMode = GL_CLAMP_TO_EDGE;
	
	private static final int[] lastBound;
	
	static
	{
		lastBound = new int[glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS)];
	}
	
	private volatile boolean destroyed = false;
	
	private final int width, height, depth;
	private final int texture;
	
	private final Lazy<TextureHandle<SingleTexture>> bindlessHandle;
	
	private SingleTexture(int width, int height, int depth, int handle)
	{
		this.width = width;
		this.height = height;
		this.depth = depth;
		
		texture = handle;
		
		bindlessHandle = Lazy.of(() -> new TextureHandle<>(this, glGetTextureHandleARB(texture)));
	}
	
	@Override
	public int handle()
	{
		return texture;
	}
	
	@Override
	public TextureHandle<SingleTexture> getBindlessHandle(boolean makeResident)
	{
		TextureHandle<SingleTexture> handle = bindlessHandle.get();
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
		return depth;
	}
	
	@Override
	public void destroy()
	{
		destroyed = true;
		glDeleteTextures(texture);
		
		VoxelTest.removeDestroyable(this);
	}
	
	public boolean isDestroyed()
	{
		return destroyed;
	}
	
	@Override
	public void bind(int unit)
	{
		bind(unit, texture);
	}
	
	@Override
	public void unbind(int unit)
	{
		bind(unit, 0);
	}
	
	public void generateMipmaps()
	{
		glGenerateTextureMipmap(texture);
	}
	
	private static void bind(int unit, int handle)
	{
		if(unit >= 0 && unit < lastBound.length && handle != lastBound[unit])
		{
			glBindTextureUnit(unit, handle);
			lastBound[unit] = handle;
		}
	}
	
	public static SingleTexture wrap(int width, int height, int depth, int handle)
	{
		return new SingleTexture(width, height, depth, handle);
	}
	
	public static SingleTexture alloc1D(int width, int format, int levels)
	{
		int texture = glCreateTextures(GL_TEXTURE_1D);
		
		glTextureParameteri(texture, GL_TEXTURE_MIN_FILTER, defaultFilter);
		glTextureParameteri(texture, GL_TEXTURE_MAG_FILTER, defaultFilter);
		
		glTextureParameteri(texture, GL_TEXTURE_WRAP_S, defaultWrapMode);
		
		glTextureStorage1D(texture, levels, format, width);
		
		SingleTexture result = wrap(width, 1, 1, texture);
		
		VoxelTest.addDestroyable(result);
		
		return result;
	}
	
	public static SingleTexture alloc2D(int width, int height, int format, int levels)
	{
		int texture = glCreateTextures(GL_TEXTURE_2D);
		
		glTextureParameteri(texture, GL_TEXTURE_MIN_FILTER, defaultFilter);
		glTextureParameteri(texture, GL_TEXTURE_MAG_FILTER, defaultFilter);
		
		glTextureParameteri(texture, GL_TEXTURE_WRAP_S, defaultWrapMode);
		glTextureParameteri(texture, GL_TEXTURE_WRAP_T, defaultWrapMode);
		
		glTextureStorage2D(texture, levels, format, width, height);
		
		SingleTexture result = wrap(width, height, 1, texture);
		
		VoxelTest.addDestroyable(result);
		
		return result;
	}
	
	public static SingleTexture alloc3D(int width, int height, int depth, int format, int levels)
	{
		int texture = glCreateTextures(GL_TEXTURE_3D);
		
		glTextureParameteri(texture, GL_TEXTURE_MIN_FILTER, defaultFilter);
		glTextureParameteri(texture, GL_TEXTURE_MAG_FILTER, defaultFilter);
		
		glTextureParameteri(texture, GL_TEXTURE_WRAP_S, defaultWrapMode);
		glTextureParameteri(texture, GL_TEXTURE_WRAP_T, defaultWrapMode);
		glTextureParameteri(texture, GL_TEXTURE_WRAP_R, defaultWrapMode);
		
		glTextureStorage3D(texture, levels, format, width, height, depth);
		
		SingleTexture result = wrap(width, height, depth, texture);
		
		VoxelTest.addDestroyable(result);
		
		return result;
	}
	
	public static int calculateMipLevels(int width, int height, int depth)
	{
		return 1 + (int)Math.floor(MathUtil.log2((double)Math.max(width, Math.max(height, depth))));
	}
}
