package info.kuonteje.voxeltest.render.block;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.RegistryEntry;
import info.kuonteje.voxeltest.render.BlockTexture;
import info.kuonteje.voxeltest.render.ModelUtil;

@JsonSerialize(using = RegistryEntry.Serializer.class)
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
		super(block.id());
	}
	
	public CubeModel setTint(int tint)
	{
		this.tint = tint & 0xFFFFFF;
		return this;
	}
	
	public CubeModel setAll(BlockTexture texture)
	{
		north = south = east = west = top = bottom = texture.idx();
		return this;
	}
	
	public CubeModel setSide(BlockTexture texture)
	{
		north = south = east = west = texture.idx();
		return this;
	}
	
	public CubeModel setTopBottom(BlockTexture texture)
	{
		int idx = texture.idx();
		
		top = idx;
		bottom = idx;
		
		return this;
	}
	
	public CubeModel setNorth(BlockTexture texture)
	{
		north = texture.idx();
		return this;
	}
	
	public CubeModel setSouth(BlockTexture texture)
	{
		south = texture.idx();
		return this;
	}
	
	public CubeModel setEast(BlockTexture texture)
	{
		east = texture.idx();
		return this;
	}
	
	public CubeModel setWest(BlockTexture texture)
	{
		west = texture.idx();
		return this;
	}
	
	public CubeModel setTop(BlockTexture texture)
	{
		top = texture.idx();
		return this;
	}
	
	public CubeModel setBottom(BlockTexture texture)
	{
		bottom = texture.idx();
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
}
