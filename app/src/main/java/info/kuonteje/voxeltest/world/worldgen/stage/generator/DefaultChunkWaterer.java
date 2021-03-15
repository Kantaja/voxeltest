package info.kuonteje.voxeltest.world.worldgen.stage.generator;

import info.kuonteje.voxeltest.world.BlockPredicate;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.worldgen.config.data.GeneratorConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IGenerationStageConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.IWorldGenerator;

public class DefaultChunkWaterer implements IWorldGenerator
{
	private final GeneratorConfig rootConfig;
	
	private final BlockPredicate topPredicate;
	
	public DefaultChunkWaterer(GeneratorConfig rootConfig)
	{
		this.rootConfig = rootConfig;
		
		this.topPredicate = BlockPredicate.forBlock(rootConfig.terrain().top());
	}
	
	public static DefaultChunkWaterer factory(GeneratorConfig rootConfig, IGenerationStageConfig config, long seed)
	{
		return new DefaultChunkWaterer(rootConfig);
	}
	
	@Override
	public void processChunk(Chunk chunk)
	{
		int baseY = chunk.pos().worldY();
		
		if(baseY > rootConfig.water().seaLevel()) return;
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					int realY = baseY + y;
					
					if(realY < rootConfig.water().seaLevel())
					{
						setBlock(chunk, x, y, z, rootConfig.water().liquidTop(), topPredicate);
						setBlock(chunk, x, y, z, rootConfig.water().liquid(), BlockPredicate.EMPTY);
					}
				}
			}
		}
	}
}
