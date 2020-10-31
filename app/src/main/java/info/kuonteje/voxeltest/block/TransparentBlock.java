package info.kuonteje.voxeltest.block;

import info.kuonteje.voxeltest.block.tag.ITransparentBlock;
import info.kuonteje.voxeltest.data.EntryId;

public class TransparentBlock extends Block implements ITransparentBlock
{
	public TransparentBlock(EntryId id)
	{
		super(id);
	}
	
	public TransparentBlock(String id)
	{
		super(id);
	}
}
