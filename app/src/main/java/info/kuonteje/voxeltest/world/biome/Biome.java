package info.kuonteje.voxeltest.world.biome;

import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.RegistryEntry;

public class Biome extends RegistryEntry<Biome>
{
	public Biome(EntryId id)
	{
		super(Biome.class, id);
	}
	
	public Biome(String id)
	{
		this(EntryId.create(id));
	}
}
