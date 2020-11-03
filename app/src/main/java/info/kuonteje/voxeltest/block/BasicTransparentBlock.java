package info.kuonteje.voxeltest.block;

import info.kuonteje.voxeltest.block.tag.ITransparentBlock;
import info.kuonteje.voxeltest.data.EntryId;

public class BasicTransparentBlock extends Block implements ITransparentBlock
{
	public BasicTransparentBlock(EntryId id)
	{
		super(id);
	}
	
	public BasicTransparentBlock(String id)
	{
		super(id);
	}
}
