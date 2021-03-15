package info.kuonteje.voxeltest.repack.fastnoise;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NoiseType
{
	@JsonProperty("opensimplex2f")
	OPENSIMPLEX2,
	@JsonProperty("opensimplex2s")
	OPENSIMPLEX2S,
	@JsonProperty("cellular")
	CELLULAR,
	@JsonProperty("perlin")
	PERLIN,
	@JsonProperty("value_cubic")
	VALUE_CUBIC,
	@JsonProperty("value")
	VALUE
}
