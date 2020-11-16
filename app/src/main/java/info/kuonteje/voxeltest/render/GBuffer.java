package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL45C.*;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import info.kuonteje.voxeltest.VoxelTest;

public class GBuffer
{
	// These are also texture units
	private static final int ALBEDO_INDEX = 0;
	private static final int POSITION_INDEX = 1;
	private static final int NORMAL_INDEX = 2;
	private static final int DEPTH_INDEX = 3;
	
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
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0 + ALBEDO_INDEX, albedoTexture, 0);
		
		int positionTexture = glCreateTextures(GL_TEXTURE_2D);
		glTextureStorage2D(positionTexture, 1, GL_RGBA32F, width, height);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0 + POSITION_INDEX, positionTexture, 0);
		
		int normalTexture = glCreateTextures(GL_TEXTURE_2D);
		glTextureStorage2D(normalTexture, 1, GL_RGBA16F, width, height);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0 + NORMAL_INDEX, normalTexture, 0);
		
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
			
			buffers.put(GL_COLOR_ATTACHMENT0 + ALBEDO_INDEX);
			buffers.put(GL_COLOR_ATTACHMENT0 + POSITION_INDEX);
			buffers.put(GL_COLOR_ATTACHMENT0 + NORMAL_INDEX);
			
			glNamedFramebufferDrawBuffers(framebuffer, buffers.flip());
		}
	}
	
	public void draw(ShaderProgram shader)
	{
		shader.bind();
		
		albedo.bind(ALBEDO_INDEX);
		position.bind(POSITION_INDEX);
		normal.bind(NORMAL_INDEX);
		if(depth != null) depth.bind(DEPTH_INDEX);
		
		glEnablei(GL_BLEND, NORMAL_INDEX);
		
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
		glDisablei(GL_BLEND, NORMAL_INDEX); // normal is written with 0 alpha
	}
	
	public void destroy()
	{
		glDeleteFramebuffers(framebuffer);
		
		albedo.destroy();
		position.destroy();
		normal.destroy();
	}
	
	public static ShaderProgram getDefaultShader()
	{
		return shadeShader;
	}
}
