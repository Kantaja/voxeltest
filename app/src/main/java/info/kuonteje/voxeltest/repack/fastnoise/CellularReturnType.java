package info.kuonteje.voxeltest.repack.fastnoise;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CellularReturnType
{
	@JsonProperty("cell_value")
	CELL_VALUE,
	@JsonProperty("distance")
	DISTANCE,
	@JsonProperty("distance2")
	DISTANCE2,
	@JsonProperty("distance2_add")
	DISTANCE2_ADD,
	@JsonProperty("distance2_sub")
	DISTANCE2_SUB,
	@JsonProperty("distance2_mul")
	DISTANCE2_MUL,
	@JsonProperty("distance2_div")
	DISTANCE2_DIV,
	@JsonProperty("distance3")
	DISTANCE3,
	@JsonProperty("distance3_add")
	DISTANCE3_ADD,
	@JsonProperty("distance3_sub")
	DISTANCE3_SUB,
	@JsonProperty("distance3_mul")
	DISTANCE3_MUL,
	@JsonProperty("distance3_div")
	DISTANCE3_DIV
}
