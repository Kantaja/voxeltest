package info.kuonteje.voxeltest.repack.fastnoise;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CellularDistanceFunction
{
	@JsonProperty("euclidean")
	EUCLIDEAN,
	@JsonProperty("euclidean_sq")
	EUCLIDEAN_SQ,
	@JsonProperty("manhattan")
	MANHATTAN,
	@JsonProperty("hybrid")
	HYBRID
}
