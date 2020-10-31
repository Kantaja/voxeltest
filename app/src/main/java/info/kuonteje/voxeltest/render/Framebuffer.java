package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.*;

import java.util.Comparator;
import java.util.Set;

import info.kuonteje.voxeltest.VoxelTest;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;

public class Framebuffer
{
	private static final int vertexShaderObject;
	public static final ShaderProgram identityShader;
	private static final int vao;
	
	private static final Set<ShaderProgram> fbShaders = new ObjectAVLTreeSet<>(Comparator.comparingInt(ShaderProgram::handle));
	
	static
	{
		vertexShaderObject = ShaderProgram.loadShaderObject("framebuffer", ShaderProgram.ShaderType.VERTEX);
		
		identityShader = createFbShader("identity");
		vao = glCreateVertexArrays();
		
		VoxelTest.addShutdownHook(() ->
		{
			fbShaders.forEach(ShaderProgram::destroy);
			glDeleteShader(vertexShaderObject);
			glDeleteVertexArrays(vao);
		});
	}
	
	public static ShaderProgram createFbShader(String name)
	{
		ShaderProgram shader = new ShaderProgram(vertexShaderObject, "post/" + name);
		fbShaders.add(shader);
		return shader;
	}
	
	private final int framebuffer;
	private final Texture colorBuffer, depthBuffer;
	
	public Framebuffer(int width, int height)
	{
		framebuffer = glCreateFramebuffers();
		
		int colorBuffer = glCreateTextures(GL_TEXTURE_2D);
		glTextureStorage2D(colorBuffer, 1, GL_RGB10_A2, width, height);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0, colorBuffer, 0);
		
		int depthBuffer = glCreateTextures(GL_TEXTURE_2D);
		glTextureStorage2D(depthBuffer, 1, GL_DEPTH_COMPONENT32F, width, height);
		glNamedFramebufferTexture(framebuffer, GL_DEPTH_ATTACHMENT, depthBuffer, 0);
		
		this.colorBuffer = Texture.wrap(width, height, colorBuffer);
		this.depthBuffer = Texture.wrap(width, height, depthBuffer);
	}
	
	public void draw(ShaderProgram shader)
	{
		colorBuffer.bind(0);
		depthBuffer.bind(1);
		
		shader.bind();
		
		glBindVertexArray(vao);
		glDrawArrays(GL_TRIANGLES, 0, 3);
	}
	
	public void draw()
	{
		draw(identityShader);
	}
	
	public void bind()
	{
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
	}
	
	public void unbind()
	{
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
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
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebuffer);
	}
	
	public void unbindAsDraw()
	{
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}
	
	public void destroy()
	{
		glDeleteFramebuffers(framebuffer);
		
		colorBuffer.destroy();
		depthBuffer.destroy();
	}
}
