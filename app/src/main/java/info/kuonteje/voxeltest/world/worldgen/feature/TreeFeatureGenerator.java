package info.kuonteje.voxeltest.world.worldgen.feature;

import java.util.Random;

import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.World;

public class TreeFeatureGenerator implements IFeatureGenerator
{
	private static final int TREE_CHANCE = 768;
	
	private static final int GRASS = DefaultRegistries.BLOCKS.getIdx(Blocks.GRASS);
	
	private static final int DIRT = DefaultRegistries.BLOCKS.getIdx(Blocks.DIRT);
	private static final int LOG = DefaultRegistries.BLOCKS.getIdx(Blocks.LOG);
	private static final int LEAVES = DefaultRegistries.BLOCKS.getIdx(Blocks.LEAVES);
	
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
						if(idx == GRASS && random.nextInt(TREE_CHANCE) == 0)
							generate(world, random, chunk.getPos().worldX() + x, chunk.getPos().worldY() + y + 1, chunk.getPos().worldZ() + z);
						
						continue;
					}
				}
			}
		}
	}
	
	private void generate(World world, Random random, int x, int y, int z)
	{
		world.setBlockIdx(x, y - 1, z, DIRT);
		
		for(int i = 0; i < 2; i++)
		{
			world.setBlockIdx(x, y + i, z, LOG);
		}
		
		for(int i = 2; i < 4; i++)
		{
			if(random.nextInt(i == 2 ? 5 : 10) == 0) world.setBlockIdx(x - 2, y + i, z - 2, LEAVES);
			world.setBlockIdx(x - 1, y + i, z - 2, LEAVES);
			world.setBlockIdx(x, y + i, z - 2, LEAVES);
			world.setBlockIdx(x + 1, y + i, z - 2, LEAVES);
			if(random.nextInt(i == 2 ? 5 : 10) == 0) world.setBlockIdx(x + 2, y + i, z - 2, LEAVES);
			
			world.setBlockIdx(x - 2, y + i, z - 1, LEAVES);
			world.setBlockIdx(x - 1, y + i, z - 1, LEAVES);
			world.setBlockIdx(x, y + i, z - 1, LEAVES);
			world.setBlockIdx(x + 1, y + i, z - 1, LEAVES);
			world.setBlockIdx(x + 2, y + i, z - 1, LEAVES);
			
			world.setBlockIdx(x - 2, y + i, z, LEAVES);
			world.setBlockIdx(x - 1, y + i, z, LEAVES);
			world.setBlockIdx(x, y + i, z, LOG);
			world.setBlockIdx(x + 1, y + i, z, LEAVES);
			world.setBlockIdx(x + 2, y + i, z, LEAVES);
			
			world.setBlockIdx(x - 2, y + i, z + 1, LEAVES);
			world.setBlockIdx(x - 1, y + i, z + 1, LEAVES);
			world.setBlockIdx(x, y + i, z + 1, LEAVES);
			world.setBlockIdx(x + 1, y + i, z + 1, LEAVES);
			world.setBlockIdx(x + 2, y + i, z + 1, LEAVES);
			
			if(random.nextInt(i == 2 ? 5 : 10) == 0) world.setBlockIdx(x - 2, y + i, z + 2, LEAVES);
			world.setBlockIdx(x - 1, y + i, z + 2, LEAVES);
			world.setBlockIdx(x, y + i, z + 2, LEAVES);
			world.setBlockIdx(x + 1, y + i, z + 2, LEAVES);
			if(random.nextInt(i == 2 ? 5 : 10) == 0) world.setBlockIdx(x + 2, y + i, z + 2, LEAVES);
		}
		
		for(int i = 4; i < 6; i++)
		{
			if(random.nextInt(i == 4 ? 5 : 10) == 0) world.setBlockIdx(x - 1, y + i, z - 1, LEAVES);
			world.setBlockIdx(x, y + i, z - 1, LEAVES);
			if(random.nextInt(i == 4 ? 5 : 10) == 0) world.setBlockIdx(x + 1, y + i, z - 1, LEAVES);
			
			world.setBlockIdx(x - 1, y + i, z, LEAVES);
			world.setBlockIdx(x, y + i, z, i == 4 ? LOG : LEAVES);
			world.setBlockIdx(x + 1, y + i, z, LEAVES);
			
			if(random.nextInt(i == 4 ? 5 : 10) == 0) world.setBlockIdx(x - 1, y + i, z + 1, LEAVES);
			world.setBlockIdx(x, y + i, z + 1, LEAVES);
			if(random.nextInt(i == 4 ? 5 : 10) == 0) world.setBlockIdx(x + 1, y + i, z + 1, LEAVES);
		}
	}
}
