package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL45C.*;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.util.IDestroyable;

public class ForwardFramebuffer implements IDestroyable
{
	public static final int vertexShaderObject;
	public static final ShaderProgram identityShader;
	
	private static final int vao;
	
	static
	{
		vertexShaderObject = ShaderProgram.loadShaderObject("framebuffer", ShaderType.VERTEX);
		
		identityShader = createFbShader("identity");
		vao = glCreateVertexArrays();
		
		VoxelTest.addShutdownHook(() ->
		{
			glDeleteShader(vertexShaderObject);
			glDeleteVertexArrays(vao);
		});
	}
	
	public static ShaderProgram createFbShader(ShaderProgram.Builder builder)
	{
		return builder.vertex(vertexShaderObject).create();
	}
	
	public static ShaderProgram createFbShader(String name)
	{
		return createFbShader(ShaderProgram.builder().fragment("post/" + name, "post/all"));
	}
	
	public static void drawFullscreenQuad()
	{
		glBindVertexArray(vao);
		glDrawArrays(GL_TRIANGLES, 0, 3);
	}
	
	private final int framebuffer;
	
	private final TextureHandle<SingleTexture> color;
	private final SingleTexture depth;
	
	public ForwardFramebuffer(DepthBuffer depthBuffer, int width, int height, boolean fp)
	{
		VoxelTest.addDestroyable(this);
		
		framebuffer = glCreateFramebuffers();
		
		SingleTexture color = SingleTexture.alloc2D(width, height, fp ? GL_RGBA16F : GL_RGBA16, 1);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0, color.handle(), 0);
		this.color = color.bindlessHandle();
		
		if(depthBuffer != null)
		{
			depth = depthBuffer.texture();
			glNamedFramebufferTexture(framebuffer, depthBuffer.hasStencil() ? GL_DEPTH_STENCIL_ATTACHMENT : GL_DEPTH_ATTACHMENT, depth.handle(), 0);
			
			depth.bindlessHandle();
		}
		else depth = null;
	}
	
	public int handle()
	{
		return framebuffer;
	}
	
	public SingleTexture colorTexture()
	{
		return color.texture();
	}
	
	public SingleTexture depthTexture()
	{
		return depth;
	}
	
	public int width()
	{
		return color.texture().width();
	}
	
	public int height()
	{
		return color.texture().height();
	}
	
	public void draw(ShaderProgram shader, SingleTexture depthTexture)
	{
		if(depthTexture == null) depthTexture = this.depth;
		
		shader.bind();
		
		shader.upload("colorSampler", color);
		if(depthTexture != null) shader.upload("depthSampler", depthTexture.bindlessHandle());
		
		drawFullscreenQuad();
	}
	
	public void draw()
	{
		draw(identityShader, null);
	}
	
	public void bind()
	{
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebuffer);
	}
	
	public void unbind()
	{
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}
	
	public void blitColorTo(ForwardFramebuffer other, boolean linear)
	{
		glBlitNamedFramebuffer(framebuffer, other.framebuffer, 0, 0, width(), height(), 0, 0, other.width(), other.height(), GL_COLOR_BUFFER_BIT, linear ? GL_LINEAR : GL_NEAREST);
	}
	
	@Override
	public void destroy()
	{
		glDeleteFramebuffers(framebuffer);
		color.texture().destroy();
		
		VoxelTest.removeDestroyable(this);
	}
}
