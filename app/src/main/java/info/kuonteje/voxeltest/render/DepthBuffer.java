package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL45C.*;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.util.IDestroyable;

public class DepthBuffer implements IDestroyable
{
	private final boolean stencil;
	
	private final int framebuffer;
	private final SingleTexture texture;
	
	public DepthBuffer(int width, int height, boolean stencil)
	{
		VoxelTest.addDestroyable(this);
		
		this.stencil = stencil;
		
		framebuffer = glCreateFramebuffers();
		
		texture = SingleTexture.alloc2D(width, height, stencil ? GL_DEPTH32F_STENCIL8 : GL_DEPTH_COMPONENT32F, 1);
		glNamedFramebufferTexture(framebuffer, stencil ? GL_DEPTH_STENCIL_ATTACHMENT : GL_DEPTH_ATTACHMENT, texture.handle(), 0);
		
		glNamedFramebufferDrawBuffer(framebuffer, GL_NONE);
	}
	
	public int handle()
	{
		return framebuffer;
	}
	
	public boolean hasStencil()
	{
		return stencil;
	}
	
	public SingleTexture texture()
	{
		return texture;
	}
	
	public void bind()
	{
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebuffer);
	}
	
	public void unbind()
	{
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}
	
	@Override
	public void destroy()
	{
		glDeleteFramebuffers(framebuffer);
		texture.destroy();
		
		VoxelTest.removeDestroyable(this);
	}
	
	public static DepthBuffer createShadowmap()
	{
		int size = Renderer.rShadowmapSize.asInt();
		DepthBuffer result = new DepthBuffer(size, size, false);
		
		glTextureParameteri(result.texture.handle(), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTextureParameteri(result.texture.handle(), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		glTextureParameteri(result.texture.handle(), GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTextureParameteri(result.texture.handle(), GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		return result;
	}
}
