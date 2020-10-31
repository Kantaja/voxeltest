package info.kuonteje.voxeltest.render.block;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL45.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.joml.FrustumIntersection;
import org.lwjgl.system.MemoryUtil;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.RegistryEntry;
import info.kuonteje.voxeltest.render.BlockTexture;
import info.kuonteje.voxeltest.render.ChunkShaderBindings;
import info.kuonteje.voxeltest.render.IRenderable;
import info.kuonteje.voxeltest.render.ModelUtil;
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
	
	private static final float vTopTint = 1.0F;
	//	private static final float vSideTint = 0.7F;
	private static final float vNorthSouthTint = 0.8F;
	private static final float vEastWestTint = 0.7F;
	private static final float vBottomTint = 0.5F;
	
	private static final float[] topTint = new float[6];
	//	private static final float[] sideTint = new float[6];
	private static final float[] northSouthTint = new float[6];
	private static final float[] eastWestTint = new float[6];
	private static final float[] bottomTint = new float[6];
	
	static
	{
		Arrays.fill(topTint, vTopTint);
		//		Arrays.fill(sideTint, vSideTint);
		Arrays.fill(northSouthTint, vNorthSouthTint);
		Arrays.fill(eastWestTint, vEastWestTint);
		Arrays.fill(bottomTint, vBottomTint);
	}
	
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
		int idx = DefaultRegistries.BLOCK_TEXTURES.getIdx(texture);
		
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
		int idx = DefaultRegistries.BLOCK_TEXTURES.getIdx(texture);
		
		north = idx;
		south = idx;
		east = idx;
		west = idx;
		
		return this;
	}
	
	public CubeModel setTopBottom(BlockTexture texture)
	{
		int idx = DefaultRegistries.BLOCK_TEXTURES.getIdx(texture);
		
		top = idx;
		bottom = idx;
		
		return this;
	}
	
	public CubeModel setNorth(BlockTexture texture)
	{
		north = DefaultRegistries.BLOCK_TEXTURES.getIdx(texture);
		return this;
	}
	
	public CubeModel setSouth(BlockTexture texture)
	{
		south = DefaultRegistries.BLOCK_TEXTURES.getIdx(texture);
		return this;
	}
	
	public CubeModel setEast(BlockTexture texture)
	{
		east = DefaultRegistries.BLOCK_TEXTURES.getIdx(texture);
		return this;
	}
	
	public CubeModel setWest(BlockTexture texture)
	{
		west = DefaultRegistries.BLOCK_TEXTURES.getIdx(texture);
		return this;
	}
	
	public CubeModel setTop(BlockTexture texture)
	{
		top = DefaultRegistries.BLOCK_TEXTURES.getIdx(texture);
		return this;
	}
	
	public CubeModel setBottom(BlockTexture texture)
	{
		bottom = DefaultRegistries.BLOCK_TEXTURES.getIdx(texture);
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
	public void getLight(FloatBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible)
	{
		if(northVisible) buf.put(northSouthTint);//buf.put(sideTint);
		if(southVisible) buf.put(northSouthTint);//buf.put(sideTint);
		if(eastVisible) buf.put(eastWestTint);//buf.put(sideTint);
		if(westVisible) buf.put(eastWestTint);//buf.put(sideTint);
		if(topVisible) buf.put(topTint);
		if(bottomVisible) buf.put(bottomTint);
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
	public IRenderable createDebugRenderable()
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
		
		int lightVbo = glCreateBuffers();
		FloatBuffer lightData = MemoryUtil.memAllocFloat(36 * 1);
		
		try
		{
			getLight(lightData, true, true, true, true, true, true);
			glNamedBufferStorage(lightVbo, lightData.flip(), 0);
		}
		finally
		{
			MemoryUtil.memFree(lightData);
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
		bindVbo(vao, lightVbo, 2, 1);
		
		VoxelTest.addShutdownHook(() ->
		{
			glDeleteVertexArrays(vao);
			
			texLayerTbo.destroy();
			tintTbo.destroy();
			
			glDeleteBuffers(vertexVbo);
			glDeleteBuffers(texCoordVbo);
			glDeleteBuffers(lightVbo);
			glDeleteBuffers(texLayerVbo);
		});
		
		return new IRenderable()
		{
			@Override
			public boolean shouldRender(FrustumIntersection frustum)
			{
				return frustum.testAab(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			}
			
			@Override
			public void render()
			{
				texLayerTbo.bind(ChunkShaderBindings.TEX_LAYER_SAMPLER);
				tintTbo.bind(ChunkShaderBindings.TINT_SAMPLER);
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