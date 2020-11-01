package info.kuonteje.voxeltest.render.block;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.RegistryEntry;
import info.kuonteje.voxeltest.render.IRenderable;

public abstract class BlockModel extends RegistryEntry<BlockModel>
{
	public BlockModel(EntryId id)
	{
		super(BlockModel.class,  id);
	}
	
	public BlockModel(String id)
	{
		this(EntryId.create(id));
	}
	
	public abstract void getVertices(FloatBuffer buf, int x, int y, int z, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible);
	public abstract void getTextureCoords(FloatBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible);
	public abstract void getLight(FloatBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible);
	public abstract void getTextureLayers(IntBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible);
	public abstract void getTints(ByteBuffer buf, boolean northVisible, boolean southVisible, boolean eastVisible, boolean westVisible, boolean topVisible, boolean bottomVisible);
	
	public abstract IRenderable createDebugRenderable();
}
