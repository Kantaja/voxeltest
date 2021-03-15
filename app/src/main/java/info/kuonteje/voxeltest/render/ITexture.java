package info.kuonteje.voxeltest.render;

import info.kuonteje.voxeltest.util.IDestroyable;

public interface ITexture<T extends ITexture<T>> extends IDestroyable
{
	int handle();
	
	TextureHandle<T> bindlessHandle(boolean makeResident);
	
	default TextureHandle<T> bindlessHandle()
	{
		return bindlessHandle(true);
	}
	
	int width();
	int height();
	int depth();
	
	void bind(int unit);
	void unbind(int unit);
}
