package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL45C.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ChunkRenderer
{
	private static final int TEX_LAYER_BUFFER_BINDING = 0;
	private static final int TINT_BUFFER_BINDING = 1;
	
	private static final int VERTICES_PER_TRIANGLE = 3;
	private static final int TEX_COORDS_PER_TRIANGLE = 3;
	
	private static final int VERTEX_FLOATS_PER_VERTEX = 3;
	private static final int TEX_COORD_FLOATS_PER_VERTEX = 2;
	
	private static final int TRIANGLES_PER_TEX_LAYER = 2;
	private static final int TEX_LAYER_INTS_PER_FACE = 1;
	
	private static final int TRIANGLES_PER_TINT = 2;
	private static final int TINT_BYTES_PER_FACE = 4;
	
	private static final int resizeSpace = 512;
	
	private int bufferSizeTriangles = 0;
	
	private final int vao, vertexVbo, texCoordVbo;
	
	private final ShaderBuffer texLayerSsbo, tintSsbo;
	
	private FloatBuffer vertexBuf, texCoordBuf;
	private IntBuffer texLayerBuf;
	private ByteBuffer tintBuf;
	
	private boolean resize = false;
	
	private int nSolidTriangles, solidTriangles = 0;
	private int nTranslucentTriangles, translucentTriangles = 0;
	
	public ChunkRenderer()
	{
		vao = glCreateVertexArrays();
		
		vertexVbo = glCreateBuffers();
		texCoordVbo = glCreateBuffers();
		
		texLayerSsbo = ShaderBuffer.createUninitialized();
		tintSsbo = ShaderBuffer.createUninitialized();
		
		reallocBuffers();
		
		bindVbo(vao, vertexVbo, 0, 3);
		bindVbo(vao, texCoordVbo, 1, 2);
		
		//vertexBuf = memAllocFloat(bufferSizeTriangles * VERTICES_PER_TRIANGLE * VERTEX_FLOATS_PER_VERTEX);
		//texCoordBuf = memAllocFloat(bufferSizeTriangles * TEX_COORDS_PER_TRIANGLE * TEX_COORD_FLOATS_PER_VERTEX);
		//texLayerBuf = memAllocInt(bufferSizeTriangles / TRIANGLES_PER_TEX_LAYER * TEX_LAYER_INTS_PER_FACE);
		//tintBuf = memAlloc(bufferSizeTriangles / TRIANGLES_PER_TINT * TINT_BYTES_PER_FACE);
		
		vertexBuf = null;
		texCoordBuf = null;
		texLayerBuf = null;
		tintBuf = null;
	}
	
	private void reallocBuffers()
	{
		glNamedBufferData(vertexVbo, bufferSizeTriangles * VERTICES_PER_TRIANGLE * VERTEX_FLOATS_PER_VERTEX * Float.BYTES, GL_STREAM_DRAW);
		glNamedBufferData(texCoordVbo, bufferSizeTriangles * TEX_COORDS_PER_TRIANGLE * TEX_COORD_FLOATS_PER_VERTEX * Float.BYTES, GL_STREAM_DRAW);
		texLayerSsbo.resize(bufferSizeTriangles / TRIANGLES_PER_TEX_LAYER * Integer.BYTES * TEX_LAYER_INTS_PER_FACE, GL_STREAM_DRAW);
		tintSsbo.resize(bufferSizeTriangles / TRIANGLES_PER_TINT * Byte.BYTES * TINT_BYTES_PER_FACE, GL_STREAM_DRAW);
	}
	
	private void bindVbo(int vao, int vbo, int index, int size)
	{
		glEnableVertexArrayAttrib(vao, index);
		glVertexArrayAttribFormat(vao, index, size, GL_FLOAT, false, 0);
		glVertexArrayVertexBuffer(vao, index, vbo, 0L, size * Float.BYTES);
		glVertexArrayAttribBinding(vao, index, index);
	}
	
	public void setTriangles(int solid, int translucent)
	{
		synchronized(this)
		{
			nSolidTriangles = solid;
			nTranslucentTriangles = translucent;
			
			if((solid + translucent) > bufferSizeTriangles)
			{
				bufferSizeTriangles = solid + translucent + resizeSpace;
				resize = true;
			}
		}
	}
	
	public FloatBuffer vertexBuffer()
	{
		return resize ? recreateVertexBuf() : vertexBuf.clear();
	}
	
	public FloatBuffer texCoordBuffer()
	{
		return resize ? recreateTexCoordBuf() : texCoordBuf.clear();
	}
	
	public IntBuffer textureLayerBuffer()
	{
		return resize ? recreateTexLayerBuf() : texLayerBuf.clear();
	}
	
	public ByteBuffer tintBuffer()
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
			glNamedBufferSubData(texLayerSsbo.handle(), 0L, texLayerBuf);
			glNamedBufferSubData(tintSsbo.handle(), 0L, tintBuf);
			
			solidTriangles = nSolidTriangles;
			translucentTriangles = nTranslucentTriangles;
			
			resize = false;
		}
	}
	
	public void renderSolidGeometry(ShaderProgram shader)
	{
		if(solidTriangles > 0) drawSolid();
	}
	
	public void renderSolidShadow(ShaderProgram shader)
	{
		if(solidTriangles > 0)
		{
			texLayerSsbo.bind(TEX_LAYER_BUFFER_BINDING);
			drawSolid();
		}
	}
	
	public void renderSolidFull(ShaderProgram shader)
	{
		if(solidTriangles > 0)
		{
			texLayerSsbo.bind(TEX_LAYER_BUFFER_BINDING);
			tintSsbo.bind(TINT_BUFFER_BINDING);
			
			drawSolid();
		}
	}
	
	private void drawSolid()
	{
		glBindVertexArray(vao);
		glDrawArrays(GL_TRIANGLES, 0, solidTriangles * VERTICES_PER_TRIANGLE);
	}
	
	public void renderTranslucent(ShaderProgram shader)
	{
		if(translucentTriangles > 0)
		{
			texLayerSsbo.bind(TEX_LAYER_BUFFER_BINDING);
			tintSsbo.bind(TINT_BUFFER_BINDING);
			shader.upload("baseTriangleId", solidTriangles);
			
			glBindVertexArray(vao);
			glDrawArrays(GL_TRIANGLES, solidTriangles * VERTICES_PER_TRIANGLE, translucentTriangles * VERTICES_PER_TRIANGLE);
		}
	}
	
	public int solidTriangles()
	{
		return solidTriangles;
	}
	
	public int translucentTriangles()
	{
		return translucentTriangles;
	}
	
	public void destroy()
	{
		memFree(vertexBuf);
		memFree(texCoordBuf);
		memFree(texLayerBuf);
		memFree(tintBuf);
		
		glDeleteBuffers(vertexVbo);
		glDeleteBuffers(texCoordVbo);
		
		texLayerSsbo.destroy();
		tintSsbo.destroy();
	}
}
