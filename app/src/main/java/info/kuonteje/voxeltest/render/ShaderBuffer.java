package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.opengl.GL45C.*;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.util.IDestroyable;

public class ShaderBuffer implements IDestroyable
{
	private static final int[] lastBound;
	
	static
	{
		lastBound = new int[glGetInteger(GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS)];
	}
	
	private final int buffer;
	private long size;
	
	private boolean mutable;
	
	private ShaderBuffer(int buffer, long size, boolean mutable)
	{
		this.buffer = buffer;
		this.size = size;
		this.mutable = mutable;
		
		VoxelTest.addDestroyable(this);
	}
	
	public int handle()
	{
		return buffer;
	}
	
	public long size()
	{
		return size;
	}
	
	public void resize(long size, int usage)
	{
		if(mutable)
		{
			glNamedBufferData(buffer, size, usage);
			this.size = size;
		}
		else System.err.println("Tried to resize immutable SSBO");
	}
	
	public void bind(int target)
	{
		bind(target, buffer);
	}
	
	public void unbind(int target)
	{
		bind(target, 0);
	}
	
	private static void bind(int target, int handle)
	{
		if(target >= 0 && target < lastBound.length && handle != lastBound[target])
		{
			glBindBufferBase(GL_SHADER_STORAGE_BUFFER, target, handle);
			lastBound[target] = handle;
		}
	}
	
	@Override
	public void destroy()
	{
		VoxelTest.removeDestroyable(this);
		
		glDeleteBuffers(buffer);
	}
	
	public static ShaderBuffer allocEmpty(long size, int usage)
	{
		int buffer = glCreateBuffers();
		
		glNamedBufferStorage(buffer, size, usage);
		
		return new ShaderBuffer(buffer, size, false);
	}
	
	public static ShaderBuffer createUninitialized()
	{
		return new ShaderBuffer(glCreateBuffers(), 0L, true);
	}
}
