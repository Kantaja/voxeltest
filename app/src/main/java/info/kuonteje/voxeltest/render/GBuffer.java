package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL45C.*;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.util.IDestroyable;

public class GBuffer implements IDestroyable
{
	private static final int ALBEDO_INDEX = 0;
	private static final int POSITION_INDEX = 1;
	private static final int NORMAL_INDEX = 2;
	
	public static ShaderProgram createShader(String name)
	{
		return ShaderProgram.builder().vertex(ForwardFramebuffer.vertexShaderObject).fragment(name, "deferred_shade").create();
	}
	
	private final int framebuffer;
	private final TextureHandle<SingleTexture> albedo, position, normal, depth;
	
	public GBuffer(DepthBuffer depthBuffer, int width, int height)
	{
		VoxelTest.addDestroyable(this);
		
		framebuffer = glCreateFramebuffers();
		
		SingleTexture albedo = SingleTexture.alloc2D(width, height, GL_RGBA8, 1);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0 + ALBEDO_INDEX, albedo.handle(), 0);
		this.albedo = albedo.getBindlessHandle();
		
		// TODO position from depth
		SingleTexture position = SingleTexture.alloc2D(width, height, GL_RGBA32F, 1);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0 + POSITION_INDEX, position.handle(), 0);
		this.position = position.getBindlessHandle();
		
		SingleTexture normal = SingleTexture.alloc2D(width, height, GL_RGBA16F, 1);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0 + NORMAL_INDEX, normal.handle(), 0);
		this.normal = normal.getBindlessHandle();
		
		if(depthBuffer != null)
		{
			SingleTexture depth = depthBuffer.getTexture();
			glNamedFramebufferTexture(framebuffer, depthBuffer.hasStencil() ? GL_DEPTH_STENCIL_ATTACHMENT : GL_DEPTH_ATTACHMENT, depth.handle(), 0);
			this.depth = depth.getBindlessHandle();
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
	
	public SingleTexture getAlbedoTexture()
	{
		return albedo.texture();
	}
	
	public SingleTexture getPositionTexture()
	{
		return position.texture();
	}
	
	public SingleTexture getNormalTexture()
	{
		return normal.texture();
	}
	
	public SingleTexture getDepthTexture()
	{
		return depth == null ? null : depth.texture();
	}
	
	public int width()
	{
		return albedo.texture().width();
	}
	
	public int height()
	{
		return albedo.texture().height();
	}
	
	public void draw(ShaderProgram shader)
	{
		shader.bind();
		ForwardFramebuffer.drawFullscreenQuad();
	}
	
	public void bind()
	{
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
	}
	
	public void unbind()
	{
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public void uploadTextureHandles(ShaderProgram shader)
	{
		shader.upload("albedo", albedo);
		shader.upload("position", position);
		shader.upload("normal", normal);
		if(depth != null) shader.upload("depth", depth);
	}
	
	@Override
	public void destroy()
	{
		glDeleteFramebuffers(framebuffer);
		
		albedo.texture().destroy();
		position.texture().destroy();
		normal.texture().destroy();
		
		VoxelTest.removeDestroyable(this);
	}
}
