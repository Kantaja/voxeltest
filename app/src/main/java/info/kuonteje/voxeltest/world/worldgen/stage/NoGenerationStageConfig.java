package info.kuonteje.voxeltest.world.worldgen.stage;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = JsonSerializer.None.class)
public final class NoGenerationStageConfig implements IGenerationStageConfig
{
	private NoGenerationStageConfig()
	{
		//
	}
}
