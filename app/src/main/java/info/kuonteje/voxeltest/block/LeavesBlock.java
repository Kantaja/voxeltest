package info.kuonteje.voxeltest.block;

import info.kuonteje.voxeltest.block.tag.ITransparentBlock;
import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.world.World;

public class LeavesBlock extends Block implements ITransparentBlock
{
	public LeavesBlock(EntryId id)
	{
		super(id);
	}
	
	public LeavesBlock(String id)
	{
		super(id);
	}
	
	@Override
	public boolean blocksAdjacentFaces(World world, int x, int y, int z)
	{
		return false;
	}
}
