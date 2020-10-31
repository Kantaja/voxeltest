package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL45.*;

public class Texture
{
	private static boolean initialized = false;
	
	private static int[] lastBound = null;
	
	private static void init()
	{
		if(initialized) return;
		
		lastBound = new int[glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS)];
		
		initialized = true;
	}
	
	private volatile boolean destroyed = false;
	
	private final int width, height;
	private final int texture;
	
	private Texture(int width, int height, int handle)
	{
		this.width = width;
		this.height = height;
		
		texture = handle;
	}
	
	public int handle()
	{
		return texture;
	}
	
	public int width()
	{
		return width;
	}
	
	public int height()
	{
		return height;
	}
	
	public void destroy()
	{
		destroyed = true;
		glDeleteTextures(texture);
	}
	
	public boolean isDestroyed()
	{
		return destroyed;
	}
	
	public void bind(int unit)
	{
		bind(unit, texture);
	}
	
	public void unbind(int unit)
	{
		bind(unit, 0);
	}
	
	private static void bind(int unit, int handle)
	{
		if(unit >= 0 && unit < lastBound.length && handle != lastBound[unit])
		{
			glBindTextureUnit(unit, handle);
			lastBound[unit] = handle;
		}
	}
	
	public static Texture wrap(int width, int height, int handle)
	{
		init();
		return new Texture(width, height, handle);
	}
}
