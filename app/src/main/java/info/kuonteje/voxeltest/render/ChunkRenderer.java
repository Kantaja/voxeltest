package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ChunkRenderer
{
	private static final int VERTICES_PER_TRIANGLE = 3;
	private static final int TEX_COORDS_PER_TRIANGLE = 3;
	private static final int LIGHTS_PER_TRIANGLE = 3;
	
	private static final int VERTEX_FLOATS_PER_VERTEX = 3;
	private static final int TEX_COORD_FLOATS_PER_VERTEX = 2;
	private static final int LIGHT_FLOATS_PER_VERTEX = 1;
	
	private static final int TRIANGLES_PER_TEX_LAYER = 2;
	private static final int TEX_LAYER_INTS_PER_FACE = 1;
	
	private static final int TRIANGLES_PER_TINT = 2;
	private static final int TINT_BYTES_PER_FACE = 4;
	
	private static final int resizeSpace = 512;
	
	private int bufferSizeTriangles = 1024;
	
	private final int vao, vertexVbo, texCoordVbo, lightVbo;
	private final int texLayerVbo, tintVbo;
	
	private final Texture texLayerTbo, tintTbo;
	
	private FloatBuffer vertexBuf, texCoordBuf, lightBuf;
	private IntBuffer texLayerBuf;
	private ByteBuffer tintBuf;
	
	private boolean resize = false;
	
	private int nOpaqueTriangles, opaqueTriangles = 0;
	private int nTransparentTriangles, transparentTriangles = 0;
	
	public ChunkRenderer()
	{
		vao = glCreateVertexArrays();
		
		vertexVbo = glCreateBuffers();
		texCoordVbo = glCreateBuffers();
		lightVbo = glCreateBuffers();
		
		texLayerVbo = glCreateBuffers();
		tintVbo = glCreateBuffers();
		
		texLayerTbo = Texture.wrap(0, 0, glCreateTextures(GL_TEXTURE_BUFFER));
		tintTbo = Texture.wrap(0, 0, glCreateTextures(GL_TEXTURE_BUFFER));
		
		glTextureBuffer(texLayerTbo.handle(), GL_R32UI, texLayerVbo);
		glTextureBuffer(tintTbo.handle(), GL_RGBA8, tintVbo);
		
		reallocBuffers();
		
		bindVbo(vao, vertexVbo, 0, 3);
		bindVbo(vao, texCoordVbo, 1, 2);
		bindVbo(vao, lightVbo, 2, 1);
		
		vertexBuf = memAllocFloat(bufferSizeTriangles * VERTICES_PER_TRIANGLE * VERTEX_FLOATS_PER_VERTEX);
		texCoordBuf = memAllocFloat(bufferSizeTriangles * TEX_COORDS_PER_TRIANGLE * TEX_COORD_FLOATS_PER_VERTEX);
		lightBuf = memAllocFloat(bufferSizeTriangles * LIGHTS_PER_TRIANGLE * LIGHT_FLOATS_PER_VERTEX);
		texLayerBuf = memAllocInt(bufferSizeTriangles / TRIANGLES_PER_TEX_LAYER * TEX_LAYER_INTS_PER_FACE);
		tintBuf = memAlloc(bufferSizeTriangles / TRIANGLES_PER_TINT * TINT_BYTES_PER_FACE);
	}
	
	private void reallocBuffers()
	{
		nglNamedBufferData(vertexVbo, bufferSizeTriangles * VERTICES_PER_TRIANGLE * VERTEX_FLOATS_PER_VERTEX * Float.BYTES, NULL, GL_STREAM_DRAW);
		nglNamedBufferData(texCoordVbo, bufferSizeTriangles * TEX_COORDS_PER_TRIANGLE * TEX_COORD_FLOATS_PER_VERTEX * Float.BYTES, NULL, GL_STREAM_DRAW);
		nglNamedBufferData(lightVbo, bufferSizeTriangles * LIGHTS_PER_TRIANGLE * LIGHT_FLOATS_PER_VERTEX * Float.BYTES, NULL, GL_STREAM_DRAW);
		nglNamedBufferData(texLayerVbo, bufferSizeTriangles / TRIANGLES_PER_TEX_LAYER * Integer.BYTES * TEX_LAYER_INTS_PER_FACE, NULL, GL_STREAM_DRAW);
		nglNamedBufferData(tintVbo, bufferSizeTriangles / TRIANGLES_PER_TINT * Byte.BYTES * TINT_BYTES_PER_FACE, NULL, GL_STREAM_DRAW);
	}
	
	private void bindVbo(int vao, int vbo, int index, int size)
	{
		glEnableVertexArrayAttrib(vao, index);
		glVertexArrayAttribFormat(vao, index, size, GL_FLOAT, false, 0);
		glVertexArrayVertexBuffer(vao, index, vbo, 0L, size * Float.BYTES);
		glVertexArrayAttribBinding(vao, index, index);
	}
	
	public void setTriangles(int opaque, int transparent)
	{
		synchronized(this)
		{
			nOpaqueTriangles = opaque;
			nTransparentTriangles = transparent;
			
			if((opaque + transparent) > bufferSizeTriangles)
			{
				bufferSizeTriangles = opaque + transparent + resizeSpace;
				resize = true;
			}
		}
	}
	
	public FloatBuffer getVertexBuffer()
	{
		return resize ? recreateVertexBuf() : vertexBuf.clear();
	}
	
	public FloatBuffer getTexCoordBuffer()
	{
		return resize ? recreateTexCoordBuf() : texCoordBuf.clear();
	}
	
	public FloatBuffer getLightBuffer()
	{
		return resize ? recreateLightBuf() : lightBuf.clear();
	}
	
	public IntBuffer getTextureLayerBuffer()
	{
		return resize ? recreateTexLayerBuf() : texLayerBuf.clear();
	}
	
	public ByteBuffer getTintBuffer()
	{
		return resize ? recreateTintBuf() : tintBuf.clear();
	}
	
	private FloatBuffer recreateVertexBuf()
	{
		if(vertexBuf != null) memFree(vertexBuf);
		return vertexBuf = memAllocFloat(bufferSizeTriangles * VERTICES_PER_TRIANGLE * VERTEX_FLOATS_PER_VERTEX);
	}
	
	private FloatBuffer recreateTexCoordBuf()
	{
		if(texCoordBuf != null) memFree(texCoordBuf);
		return texCoordBuf = memAllocFloat(bufferSizeTriangles * TEX_COORDS_PER_TRIANGLE * TEX_COORD_FLOATS_PER_VERTEX);
	}
	
	private FloatBuffer recreateLightBuf()
	{
		if(lightBuf != null) memFree(lightBuf);
		return lightBuf = memAllocFloat(bufferSizeTriangles * LIGHTS_PER_TRIANGLE * LIGHT_FLOATS_PER_VERTEX);
	}
	
	private IntBuffer recreateTexLayerBuf()
	{
		if(texLayerBuf != null) memFree(texLayerBuf);
		return texLayerBuf = memAllocInt(bufferSizeTriangles / TRIANGLES_PER_TEX_LAYER * TEX_LAYER_INTS_PER_FACE);
	}
	
	private ByteBuffer recreateTintBuf()
	{
		if(tintBuf != null) memFree(tintBuf);
		return tintBuf = memAlloc(bufferSizeTriangles / TRIANGLES_PER_TINT * TINT_BYTES_PER_FACE);
	}
	
	public void loadMesh()
	{
		synchronized(this)
		{
			if(resize) reallocBuffers();
			
			glNamedBufferSubData(vertexVbo, 0L, vertexBuf);
			glNamedBufferSubData(texCoordVbo, 0L, texCoordBuf);
			glNamedBufferSubData(lightVbo, 0L, lightBuf);
			glNamedBufferSubData(texLayerVbo, 0L, texLayerBuf);
			glNamedBufferSubData(tintVbo, 0L, tintBuf);
			
			opaqueTriangles = nOpaqueTriangles;
			transparentTriangles = nTransparentTriangles;
			
			resize = false;
		}
	}
	
	public void renderOpaque()
	{
		if(opaqueTriangles > 0)
		{
			texLayerTbo.bind(ChunkShaderBindings.TEX_LAYER_SAMPLER);
			tintTbo.bind(ChunkShaderBindings.TINT_SAMPLER);
			glBindVertexArray(vao);
			glDrawArrays(GL_TRIANGLES, 0, opaqueTriangles * VERTICES_PER_TRIANGLE);
		}
	}
	
	public void renderTransparent()
	{
		if(transparentTriangles > 0)
		{
			glUniform1i(ChunkShaderBindings.BASE_TRIANGLE_ID, opaqueTriangles);
			texLayerTbo.bind(ChunkShaderBindings.TEX_LAYER_SAMPLER);
			tintTbo.bind(ChunkShaderBindings.TINT_SAMPLER);
			glBindVertexArray(vao);
			glDrawArrays(GL_TRIANGLES, opaqueTriangles * VERTICES_PER_TRIANGLE, transparentTriangles * VERTICES_PER_TRIANGLE);
		}
	}
	
	public int getTotalTriangles()
	{
		return opaqueTriangles + transparentTriangles;
	}
	
	public void destroy()
	{
		memFree(vertexBuf);
		memFree(texCoordBuf);
		memFree(lightBuf);
		memFree(texLayerBuf);
		memFree(tintBuf);
		
		glDeleteBuffers(vertexVbo);
		glDeleteBuffers(texCoordVbo);
		glDeleteBuffers(lightVbo);
		
		glDeleteBuffers(texLayerVbo);
		glDeleteBuffers(tintVbo);
		
		texLayerTbo.destroy();
		tintTbo.destroy();
	}
}
