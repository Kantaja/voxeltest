package info.kuonteje.voxeltest.block;

import info.kuonteje.voxeltest.block.tag.ITranslucentBlock;
import info.kuonteje.voxeltest.data.EntryId;

public class BasicTranslucentBlock extends Block implements ITranslucentBlock
{
	public BasicTranslucentBlock(EntryId id)
	{
		super(id);
	}
	
	public BasicTranslucentBlock(String id)
	{
		super(id);
	}
}
