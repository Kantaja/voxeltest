package info.kuonteje.voxeltest.repack.fastnoise;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RotationType3d
{
	@JsonProperty("none")
	NONE,
	@JsonProperty("improve_xy_planes")
	IMPROVE_XY_PLANES,
	@JsonProperty("improve_xz_planes")
	IMPROVE_XZ_PLANES
}
