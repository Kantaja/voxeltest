package info.kuonteje.voxeltest.world.worldgen;

import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.ChunkPosition;
import info.kuonteje.voxeltest.world.IChunkProvider;
import info.kuonteje.voxeltest.world.World;

public class ChunkProviderGenerate implements IChunkProvider
{
	private final World world;
	private final IWorldGenerator generator;
	
	public ChunkProviderGenerate(World world)
	{
		this.world = world;
		generator = new WorldGeneratorDefault(world.getSeed());
	}
	
	@Override
	public Chunk getChunk(ChunkPosition pos)
	{
		Chunk chunk = new Chunk(world, pos);
		
		try
		{
			generator.fillChunk(chunk);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return chunk;
	}
}
