package info.kuonteje.voxeltest.world.worldgen.feature;

import java.util.Random;

import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.World;

public class TreeFeatureGenerator implements IFeatureGenerator
{
	private static final int TREE_CHANCE = 768;
	
	@Override
	public void tryGenerateIn(World world, Chunk chunk)
	{
		if(chunk.empty() || chunk.getPos().y() < 0) return;
		
		Random random = new Random(chunk.getPos().chunkSeed(world.getSeed()));
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 31; y >= 0; y--)
				{
					int idx = chunk.getBlockIdx(x, y, z);
					
					if(idx != 0)
					{
						if(idx == Blocks.GRASS.getIdx() && random.nextInt(TREE_CHANCE) == 0)
							generate(world, random, chunk.getPos().worldX() + x, chunk.getPos().worldY() + y + 1, chunk.getPos().worldZ() + z);
						
						continue;
					}
				}
			}
		}
	}
	
	private void generate(World world, Random random, int x, int y, int z)
	{
		world.setBlock(x, y - 1, z, Blocks.DIRT);
		
		for(int i = 0; i < 2; i++)
		{
			world.setBlock(x, y + i, z, Blocks.LOG);
		}
		
		for(int i = 2; i < 4; i++)
		{
			if(random.nextInt(i == 2 ? 5 : 10) == 0) world.setBlock(x - 2, y + i, z - 2, Blocks.LEAVES);
			world.setBlock(x - 1, y + i, z - 2, Blocks.LEAVES);
			world.setBlock(x, y + i, z - 2, Blocks.LEAVES);
			world.setBlock(x + 1, y + i, z - 2, Blocks.LEAVES);
			if(random.nextInt(i == 2 ? 5 : 10) == 0) world.setBlock(x + 2, y + i, z - 2, Blocks.LEAVES);
			
			world.setBlock(x - 2, y + i, z - 1, Blocks.LEAVES);
			world.setBlock(x - 1, y + i, z - 1, Blocks.LEAVES);
			world.setBlock(x, y + i, z - 1, Blocks.LEAVES);
			world.setBlock(x + 1, y + i, z - 1, Blocks.LEAVES);
			world.setBlock(x + 2, y + i, z - 1, Blocks.LEAVES);
			
			world.setBlock(x - 2, y + i, z, Blocks.LEAVES);
			world.setBlock(x - 1, y + i, z, Blocks.LEAVES);
			world.setBlock(x, y + i, z, Blocks.LOG);
			world.setBlock(x + 1, y + i, z, Blocks.LEAVES);
			world.setBlock(x + 2, y + i, z, Blocks.LEAVES);
			
			world.setBlock(x - 2, y + i, z + 1, Blocks.LEAVES);
			world.setBlock(x - 1, y + i, z + 1, Blocks.LEAVES);
			world.setBlock(x, y + i, z + 1, Blocks.LEAVES);
			world.setBlock(x + 1, y + i, z + 1, Blocks.LEAVES);
			world.setBlock(x + 2, y + i, z + 1, Blocks.LEAVES);
			
			if(random.nextInt(i == 2 ? 5 : 10) == 0) world.setBlock(x - 2, y + i, z + 2, Blocks.LEAVES);
			world.setBlock(x - 1, y + i, z + 2, Blocks.LEAVES);
			world.setBlock(x, y + i, z + 2, Blocks.LEAVES);
			world.setBlock(x + 1, y + i, z + 2, Blocks.LEAVES);
			if(random.nextInt(i == 2 ? 5 : 10) == 0) world.setBlock(x + 2, y + i, z + 2, Blocks.LEAVES);
		}
		
		for(int i = 4; i < 6; i++)
		{
			if(random.nextInt(i == 4 ? 5 : 10) == 0) world.setBlock(x - 1, y + i, z - 1, Blocks.LEAVES);
			world.setBlock(x, y + i, z - 1, Blocks.LEAVES);
			if(random.nextInt(i == 4 ? 5 : 10) == 0) world.setBlock(x + 1, y + i, z - 1, Blocks.LEAVES);
			
			world.setBlock(x - 1, y + i, z, Blocks.LEAVES);
			world.setBlock(x, y + i, z, i == 4 ? Blocks.LOG : Blocks.LEAVES);
			world.setBlock(x + 1, y + i, z, Blocks.LEAVES);
			
			if(random.nextInt(i == 4 ? 5 : 10) == 0) world.setBlock(x - 1, y + i, z + 1, Blocks.LEAVES);
			world.setBlock(x, y + i, z + 1, Blocks.LEAVES);
			if(random.nextInt(i == 4 ? 5 : 10) == 0) world.setBlock(x + 1, y + i, z + 1, Blocks.LEAVES);
		}
	}
}
