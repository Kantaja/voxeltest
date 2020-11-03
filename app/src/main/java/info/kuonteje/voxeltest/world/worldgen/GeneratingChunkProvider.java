package info.kuonteje.voxeltest.world.worldgen;

import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.ChunkPosition;
import info.kuonteje.voxeltest.world.IChunkProvider;
import info.kuonteje.voxeltest.world.World;
import info.kuonteje.voxeltest.world.worldgen.defaultGenerator.DefaultCaveGenerator;
import info.kuonteje.voxeltest.world.worldgen.defaultGenerator.DefaultChunkGenerator;
import info.kuonteje.voxeltest.world.worldgen.defaultGenerator.DefaultChunkWaterer;

public class GeneratingChunkProvider implements IChunkProvider
{
	private final World world;
	
	private final IChunkProcessor generator;
	private final IChunkProcessor waterer;
	private final IChunkProcessor caveGenerator;
	
	private final ChunkDecorator decorator;
	
	public GeneratingChunkProvider(World world)
	{
		this.world = world;
		
		long seed = world.getSeed();
		
		System.out.println("Generator seed: " + seed);
		
		generator = new DefaultChunkGenerator(seed);
		waterer = new DefaultChunkWaterer();
		caveGenerator = new DefaultCaveGenerator(seed);
		
		decorator = new ChunkDecorator(seed);
	}
	
	@Override
	public Chunk getChunk(ChunkPosition pos)
	{
		Chunk chunk = new Chunk(world, pos);
		
		try
		{
			generator.processChunk(chunk);
			waterer.processChunk(chunk);
			caveGenerator.processChunk(chunk);
			
			decorator.processChunk(chunk);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return chunk;
	}
}
