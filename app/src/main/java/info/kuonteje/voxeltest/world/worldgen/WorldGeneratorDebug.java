package info.kuonteje.voxeltest.world.worldgen;

import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.world.Chunk;

public class WorldGeneratorDebug implements IWorldGenerator
{
	@Override
	public void fillChunk(Chunk chunk)
	{
		if(chunk.getPos().y() == 2) fillSurfaceChunk(chunk);
		else if(chunk.getPos().y() < 2) fillUndergroundChunk(chunk);
	}
	
	private void fillSurfaceChunk(Chunk chunk)
	{
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y <= 24; y++)
				{
					if(y == 24) chunk.setBlock(x, y, z, Blocks.GRASS);
					else if(y > 20) chunk.setBlock(x, y, z, Blocks.DIRT);
					else chunk.setBlock(x, y, z, Blocks.STONE);
				}
			}
		}
	}
	
	private void fillUndergroundChunk(Chunk chunk)
	{
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					chunk.setBlock(x, y, z, Blocks.STONE);
				}
			}
		}
	}
}
