package info.kuonteje.voxeltest.block;

import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.RegistryEntry;

public class Block extends RegistryEntry<Block>
{
	public Block(EntryId id)
	{
		super(Block.class, id);
	}
	
	public Block(String id)
	{
		this(EntryId.create(id));
	}
}
