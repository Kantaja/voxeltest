package info.kuonteje.voxeltest.world.worldgen.defaultGenerator;

import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.worldgen.IChunkProcessor;

public class DefaultChunkWaterer implements IChunkProcessor
{
	private static final int GRASS = Blocks.GRASS.getIdx();
	
	@Override
	public void processChunk(Chunk chunk)
	{
		int chunkY = chunk.getPos().y();
		
		if(chunkY > 0) return;
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					int realY = chunkY * 32 + y;
					int existingBlock = chunk.getBlockIdx(x, y, z);
					
					if(realY < 2)
					{
						if(existingBlock == GRASS) chunk.setBlock(x, y, z, Blocks.SAND);
						else if(existingBlock == 0 && realY <= 0) chunk.setBlock(x, y, z, Blocks.WATER);
					}
				}
			}
		}
	}
}
