package info.kuonteje.voxeltest.world.worldgen;

import java.util.List;

import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.objects.Features;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.worldgen.feature.Feature;
import info.kuonteje.voxeltest.world.worldgen.feature.IFeatureGenerator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ChunkDecorator implements IChunkProcessor
{
	private List<IFeatureGenerator> features = new ObjectArrayList<>();
	
	public ChunkDecorator(long seed)
	{
		for(Feature feature : DefaultRegistries.FEATURES)
		{
			if(feature != Features.IDENTITY) features.add(feature.createGenerator(seed));
		}
	}
	
	@Override
	public void processChunk(Chunk chunk)
	{
		features.forEach(f -> f.tryGenerateIn(chunk.getWorld(), chunk));
	}
}
