package info.kuonteje.voxeltest.block;

import info.kuonteje.voxeltest.block.tag.ICutoutBlock;
import info.kuonteje.voxeltest.data.EntryId;

public class LeavesBlock extends Block implements ICutoutBlock
{
	public LeavesBlock(EntryId id)
	{
		super(id);
	}
	
	public LeavesBlock(String id)
	{
		super(id);
	}
	
	/*
	@Override
	public boolean blocksAdjacentFaces(World world, int x, int y, int z)
	{
		return false;
	}
	 */
}
