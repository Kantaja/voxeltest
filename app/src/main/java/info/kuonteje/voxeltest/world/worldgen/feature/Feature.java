package info.kuonteje.voxeltest.world.worldgen.feature;

import java.util.function.LongFunction;

import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.RegistryEntry;

public class Feature extends RegistryEntry<Feature>
{
	private static final IFeatureGenerator IDENTITY = (world, chunk) -> {};
	
	protected final LongFunction<IFeatureGenerator> constructor;
	
	public Feature(EntryId id, LongFunction<IFeatureGenerator> constructor)
	{
		super(Feature.class, id);
		this.constructor = constructor;
	}
	
	public Feature(String id, LongFunction<IFeatureGenerator> constructor)
	{
		this(EntryId.create(id), constructor);
	}
	
	public IFeatureGenerator createGenerator(long seed)
	{
		return constructor == null ? IDENTITY : constructor.apply(seed);
	}
}
