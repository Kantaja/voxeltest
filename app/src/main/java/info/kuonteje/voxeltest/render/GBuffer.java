package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL45C.*;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import info.kuonteje.voxeltest.VoxelTest;

public class GBuffer
{
	private static final int ALBEDO_ATTACHMENT = 0;
	private static final int POSITION_ATTACHMENT = 1;
	private static final int NORMAL_ATTACHMENT = 2;
	
	private static final ShaderProgram shadeShader; // xd
	
	static
	{
		shadeShader = ShaderProgram.builder().vertex("framebuffer").fragment("deferred_shade").create();
		VoxelTest.addShutdownHook(() -> shadeShader.destroy());
	}
	
	private final int framebuffer;
	private final Texture albedo, position, normal, depth;
	
	public GBuffer(Texture depthTexture, int width, int height)
	{
		framebuffer = glCreateFramebuffers();
		
		int albedoTexture = glCreateTextures(GL_TEXTURE_2D);
		glTextureStorage2D(albedoTexture, 1, GL_RGBA8, width, height);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0 + ALBEDO_ATTACHMENT, albedoTexture, 0);
		
		int positionTexture = glCreateTextures(GL_TEXTURE_2D);
		glTextureStorage2D(positionTexture, 1, GL_RGBA32F, width, height);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0 + POSITION_ATTACHMENT, positionTexture, 0);
		
		int normalTexture = glCreateTextures(GL_TEXTURE_2D);
		glTextureStorage2D(normalTexture, 1, GL_RGBA16F, width, height);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0 + NORMAL_ATTACHMENT, normalTexture, 0);
		
		albedo = Texture.wrap(width, height, albedoTexture);
		position = Texture.wrap(width, height, positionTexture);
		normal = Texture.wrap(width, height, normalTexture);
		
		if(depthTexture != null)
		{
			glNamedFramebufferTexture(framebuffer, GL_DEPTH_ATTACHMENT, depthTexture.handle(), 0);
			depth = depthTexture;
		}
		else depth = null;
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer buffers = stack.mallocInt(3);
			
			buffers.put(GL_COLOR_ATTACHMENT0 + ALBEDO_ATTACHMENT);
			buffers.put(GL_COLOR_ATTACHMENT0 + POSITION_ATTACHMENT);
			buffers.put(GL_COLOR_ATTACHMENT0 + NORMAL_ATTACHMENT);
			
			glNamedFramebufferDrawBuffers(framebuffer, buffers.flip());
		}
	}
	
	public void draw(ShaderProgram shader)
	{
		shader.bind();
		
		albedo.bind(0);
		position.bind(1);
		normal.bind(2);
		if(depth != null) depth.bind(3);
		
		glEnablei(GL_BLEND, NORMAL_ATTACHMENT);
		
		glBindVertexArray(ForwardFramebuffer.getVao());
		glDrawArrays(GL_TRIANGLES, 0, 3);
	}
	
	public void draw()
	{
		draw(shadeShader);
	}
	
	public void bind()
	{
		bindDraw(GL_FRAMEBUFFER, framebuffer);
	}
	
	public void unbind()
	{
		bindDraw(GL_FRAMEBUFFER, 0);
	}
	
	public void bindAsRead()
	{
		glBindFramebuffer(GL_READ_FRAMEBUFFER, framebuffer);
	}
	
	public void unbindAsRead()
	{
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
	}
	
	public void bindAsDraw()
	{
		bindDraw(GL_DRAW_FRAMEBUFFER, framebuffer);
	}
	
	public void unbindAsDraw()
	{
		bindDraw(GL_DRAW_FRAMEBUFFER, 0);
	}
	
	private static void bindDraw(int target, int fbo)
	{
		glBindFramebuffer(target, fbo);
		glDisablei(GL_BLEND, NORMAL_ATTACHMENT); // normal is written with 0 alpha
	}
	
	public void destroy()
	{
		glDeleteFramebuffers(framebuffer);
		
		albedo.destroy();
		position.destroy();
		normal.destroy();
	}
}
