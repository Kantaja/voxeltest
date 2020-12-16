package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.ARBBindlessTexture.*;

public class TextureHandle<T extends ITexture<T>>
{
	private final T texture;
	private final long handle;
	
	private boolean resident = false;
	
	TextureHandle(T texture, long handle)
	{
		this.texture = texture;
		this.handle = handle;
	}
	
	public T texture()
	{
		return texture;
	}
	
	public long handle()
	{
		return handle;
	}
	
	public TextureHandle<T> makeResident()
	{
		if(!resident)
		{
			glMakeTextureHandleResidentARB(handle);
			resident = true;
		}
		
		return this;
	}
	
	public TextureHandle<T> makeNonResident()
	{
		if(resident)
		{
			glMakeTextureHandleNonResidentARB(handle);
			resident = false;
		}
		
		return this;
	}
}
