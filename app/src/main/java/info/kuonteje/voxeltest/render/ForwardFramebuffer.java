package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL45C.*;

import java.util.Comparator;
import java.util.Set;

import info.kuonteje.voxeltest.VoxelTest;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;

public class ForwardFramebuffer
{
	// These are also texture units
	private static final int COLOR_INDEX = 0;
	private static final int DEPTH_INDEX = 1;
	
	public static final int vertexShaderObject;
	public static final ShaderProgram identityShader;
	
	private static final int vao;
	
	private static final Set<ShaderProgram> fbShaders = new ObjectAVLTreeSet<>(Comparator.comparingInt(ShaderProgram::handle));
	
	static
	{
		vertexShaderObject = ShaderProgram.loadShaderObject("framebuffer", ShaderType.VERTEX);
		
		identityShader = createFbShader("identity");
		vao = glCreateVertexArrays();
		
		VoxelTest.addShutdownHook(() ->
		{
			fbShaders.forEach(ShaderProgram::destroy);
			glDeleteShader(vertexShaderObject);
			glDeleteVertexArrays(vao);
		});
	}
	
	public static ShaderProgram createFbShader(ShaderProgram.Builder builder)
	{
		ShaderProgram shader = builder.vertex(vertexShaderObject).create();
		fbShaders.add(shader);
		return shader;
	}
	
	public static ShaderProgram createFbShader(String name)
	{
		return createFbShader(ShaderProgram.builder().fragment("post/" + name, "post/all"));
	}
	
	public static int getVao()
	{
		return vao;
	}
	
	private final int framebuffer;
	private final Texture color, depth;
	
	public ForwardFramebuffer(Texture depthTexture, int width, int height, boolean fp)
	{
		framebuffer = glCreateFramebuffers();
		
		int colorBuffer = glCreateTextures(GL_TEXTURE_2D);
		glTextureStorage2D(colorBuffer, 1, fp ? GL_RGBA16F : GL_RGBA16, width, height);
		glNamedFramebufferTexture(framebuffer, GL_COLOR_ATTACHMENT0, colorBuffer, 0);
		
		this.color = Texture.wrap(width, height, colorBuffer);
		
		if(depthTexture != null)
		{
			glNamedFramebufferTexture(framebuffer, GL_DEPTH_ATTACHMENT, depthTexture.handle(), 0);
			depth = depthTexture;
		}
		else depth = null;
	}
	
	public Texture getDepthTexture()
	{
		return depth;
	}
	
	public void draw(ShaderProgram shader, Texture depthTexture)
	{
		if(depthTexture == null) depthTexture = depth;
		
		shader.bind();
		
		color.bind(COLOR_INDEX);
		if(depthTexture != null) depthTexture.bind(DEPTH_INDEX);
		
		glBindVertexArray(vao);
		glDrawArrays(GL_TRIANGLES, 0, 3);
	}
	
	public void draw()
	{
		draw(identityShader, null);
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
		color.destroy();
	}
}
