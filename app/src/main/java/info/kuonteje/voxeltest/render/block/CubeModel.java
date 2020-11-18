package info.kuonteje.voxeltest.render.block;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL45.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.FrustumIntersection;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.lwjgl.system.MemoryUtil;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.RegistryEntry;
import info.kuonteje.voxeltest.render.BlockTexture;
import info.kuonteje.voxeltest.render.ModelUtil;
import info.kuonteje.voxeltest.render.Renderable;
import info.kuonteje.voxeltest.render.Texture;

public class CubeModel extends BlockModel
{
	private static final int MISSING_LAYER = 0;
	
	private static final float[] northVertices = { 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F };
	private static final float[] southVertices = { 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F };
	private static final float[] eastVertices = { 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F };
	private static final float[] westVertices = { 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F };
	private static final float[] topVertices = { 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F };
	private static final float[] bottomVertices = { 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F };
	
	private static final float[] northTc = { 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F };
	private static final float[] southTc = { 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F };
	private static final float[] eastTc = { 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F };
	private static final float[] westTc = { 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F };
	private static final float[] topTc = { 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F };
	private static final float[] bottomTc = { 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F };
	
	private int tint = 0xFFFFFF;
	
	private int north = MISSING_LAYER;
	private int south = MISSING_LAYER;
	private int east = MISSING_LAYER;
	private int west = MISSING_LAYER;
	private int top = MISSING_LAYER;
	private int bottom = MISSING_LAYER;
	
	public CubeModel(EntryId id)
	{
		super(id);
	}
	
	public CubeModel(String id)
	{
		super(id);
	}
	
	public CubeModel(RegistryEntry<Block> block)
	{
		super(block.getId());
	}
	
	public CubeModel setTint(int tint)
	{
		this.tint = tint & 0xFFFFFF;
		return this;
	}
	
	public CubeModel setAll(BlockTexture texture)
	{
		int idx = texture.getIdx();
		
		north = idx;
		south = idx;
		east = idx;
		west = idx;
		top = idx;
		bottom = idx;
		
		return this;
	}
	
	public CubeModel setSide(BlockTexture texture)
	{
		int idx = texture.getIdx();
		
		north = idx;
		south = idx;
		east = idx;
		west = idx;
		
		return this;
	}
	
	public CubeModel setTopBottom(BlockTexture texture)
	{
		int idx = texture.getIdx();
		
		top = idx;
		bottom = idx;
		
		return this;
	}
	
	public CubeModel setNorth(BlockTexture texture)
	{
		north = texture.getIdx();
		return this;
	}
	
	public CubeModel setSouth(BlockTexture texture)
	{
		south = texture.getIdx();
		return this;
	}
	
	public CubeModel setEast(BlockTexture texture)
	{
		east = texture.getIdx();
		return this;
	}
	
	public CubeModel setWest(BlockTexture texture)
	{
		west = texture.getIdx();
		return this;
	}
	
	public CubeModel setTop(BlockTexture texture)
	{
		top = texture.getIdx();
		return this;
	}
	
	public CubeModel setBottom(BlockTexture texture)
	{
		bottom = texture.getIdx();
		return this;
	}
	
	@Override
	public void getVertices(FloatBuffer buf, int x, int y, int z, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible)
	{
		if(northVisible) ModelUtil.addVertices(buf, northVertices, x, y, z);
		if(southVisible) ModelUtil.addVertices(buf, southVertices, x, y, z);
		if(eastVisible) ModelUtil.addVertices(buf, eastVertices, x, y, z);
		if(westVisible) ModelUtil.addVertices(buf, westVertices, x, y, z);
		if(topVisible) ModelUtil.addVertices(buf, topVertices, x, y, z);
		if(bottomVisible) ModelUtil.addVertices(buf, bottomVertices, x, y, z);
	}
	
	@Override
	public void getTextureCoords(FloatBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible)
	{
		if(northVisible) buf.put(northTc);
		if(southVisible) buf.put(southTc);
		if(eastVisible) buf.put(eastTc);
		if(westVisible) buf.put(westTc);
		if(topVisible) buf.put(topTc);
		if(bottomVisible) buf.put(bottomTc);
	}
	
	@Override
	public void getTextureLayers(IntBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible)
	{
		if(northVisible) buf.put(north);
		if(southVisible) buf.put(south);
		if(eastVisible) buf.put(east);
		if(westVisible) buf.put(west);
		if(topVisible) buf.put(top);
		if(bottomVisible) buf.put(bottom);
	}
	
	@Override
	public void getTints(ByteBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible)
	{
		if(northVisible) ModelUtil.addTint(buf, tint);
		if(southVisible) ModelUtil.addTint(buf, tint);
		if(eastVisible)  ModelUtil.addTint(buf, tint);
		if(westVisible) ModelUtil.addTint(buf, tint);
		if(topVisible) ModelUtil.addTint(buf, tint);
		if(bottomVisible) ModelUtil.addTint(buf, tint);
	}
	
	@Override
	public Renderable createDebugRenderable()
	{
		int vao = glCreateVertexArrays();
		int vertexVbo = glCreateBuffers();
		
		FloatBuffer vertexData = MemoryUtil.memAllocFloat(36 * 3);
		
		try
		{
			getVertices(vertexData, 0, 0, 0, true, true, true, true, true, true);
			glNamedBufferStorage(vertexVbo, vertexData.flip(), 0);
		}
		finally
		{
			MemoryUtil.memFree(vertexData);
		}
		
		int texCoordVbo = glCreateBuffers();
		FloatBuffer texCoordData = MemoryUtil.memAllocFloat(36 * 2);
		
		try
		{
			getTextureCoords(texCoordData, true, true, true, true, true, true);
			glNamedBufferStorage(texCoordVbo, texCoordData.flip(), 0);
		}
		finally
		{
			MemoryUtil.memFree(texCoordData);
		}
		
		int texLayerVbo = glCreateBuffers();
		Texture texLayerTbo = Texture.wrap(0, 0, glCreateTextures(GL_TEXTURE_BUFFER));
		
		glTextureBuffer(texLayerTbo.handle(), GL_R32UI, texLayerVbo);
		
		IntBuffer layerData = MemoryUtil.memAllocInt(6);
		
		try
		{
			getTextureLayers(layerData, true, true, true, true, true, true);
			glNamedBufferStorage(texLayerVbo, layerData.flip(), 0);
		}
		finally
		{
			MemoryUtil.memFree(layerData);
		}
		
		int tintVbo = glCreateBuffers();
		Texture tintTbo = Texture.wrap(0, 0, glCreateTextures(GL_TEXTURE_BUFFER));
		
		glTextureBuffer(tintTbo.handle(), GL_RGBA8, tintVbo);
		
		ByteBuffer tintData = MemoryUtil.memAlloc(6);
		
		try
		{
			getTints(tintData, true, true, true, true, true, true);
			glNamedBufferStorage(tintVbo, tintData.flip(), 0);
		}
		finally
		{
			MemoryUtil.memFree(tintData);
		}
		
		bindVbo(vao, vertexVbo, 0, 3);
		bindVbo(vao, texCoordVbo, 1, 2);
		
		VoxelTest.addShutdownHook(() ->
		{
			glDeleteVertexArrays(vao);
			
			texLayerTbo.destroy();
			tintTbo.destroy();
			
			glDeleteBuffers(vertexVbo);
			glDeleteBuffers(texCoordVbo);
			glDeleteBuffers(texLayerVbo);
		});
		
		return new Renderable()
		{
			private final Vector3d center = new Vector3d(0.5, 0.5, 0.5);
			
			@Override
			public Vector3dc getCenter()
			{
				return center;
			}
			
			@Override
			public boolean shouldRender(FrustumIntersection frustum)
			{
				return frustum.testAab(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			}
			
			@Override
			public void render()
			{
				texLayerTbo.bind(0);
				tintTbo.bind(1);
				
				glBindVertexArray(vao);
				glDrawArrays(GL_TRIANGLES, 0, 36);
			}
		};
	}
	
	private void bindVbo(int vao, int vbo, int index, int size)
	{
		glEnableVertexArrayAttrib(vao, index);
		glVertexArrayAttribFormat(vao, index, size, GL_FLOAT, false, 0);
		glVertexArrayVertexBuffer(vao, index, vbo, 0L, size * 4);
		glVertexArrayAttribBinding(vao, index, index);
	}
}
