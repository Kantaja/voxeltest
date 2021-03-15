package info.kuonteje.voxeltest.world.worldgen.feature;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = JsonSerializer.None.class)
public final class NoFeatureConfig implements IFeatureConfig
{
	private NoFeatureConfig()
	{
		//
	}
}
