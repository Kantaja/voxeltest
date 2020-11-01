package info.kuonteje.voxeltest.world.worldgen;

import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.ChunkPosition;
import info.kuonteje.voxeltest.world.IChunkProvider;
import info.kuonteje.voxeltest.world.World;
import info.kuonteje.voxeltest.world.worldgen.caves.DefaultCaveGenerator;
import info.kuonteje.voxeltest.world.worldgen.caves.ICaveGenerator;

public class ChunkProviderGenerate implements IChunkProvider
{
	private final World world;
	
	private final IWorldGenerator generator;
	private final IChunkWaterer waterer;
	private final ICaveGenerator caveGenerator;
	
	public ChunkProviderGenerate(World world)
	{
		this.world = world;
		
		long seed = world.getSeed();
		
		System.out.println("Generator seed: " + seed);
		
		generator = new DefaultWorldGenerator(seed);
		waterer = new DefaultChunkWaterer();
		caveGenerator = new DefaultCaveGenerator(seed);
	}
	
	@Override
	public Chunk getChunk(ChunkPosition pos)
	{
		Chunk chunk = new Chunk(world, pos);
		
		try
		{
			generator.fillChunk(chunk);
			waterer.addWater(chunk);
			caveGenerator.generateCaves(chunk);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return chunk;
	}
}
