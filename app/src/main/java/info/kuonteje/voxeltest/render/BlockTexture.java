package info.kuonteje.voxeltest.render;

import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.RegistryEntry;

public class BlockTexture extends RegistryEntry<BlockTexture>
{
	private final EntryId textureId;
	
	public BlockTexture(EntryId id)
	{
		super(BlockTexture.class, id);
		this.textureId = EntryId.createPrefixed(id.getDomain(), id.getId(), "blocks");
	}
	
	public BlockTexture(String id)
	{
		this(EntryId.create(id));
	}
	
	public EntryId getTextureId()
	{
		return textureId;
	}
}
