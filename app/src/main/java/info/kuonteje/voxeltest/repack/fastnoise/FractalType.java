package info.kuonteje.voxeltest.repack.fastnoise;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FractalType
{
	@JsonProperty("none")
	NONE,
	@JsonProperty("fbm")
	FBM,
	@JsonProperty("ridged")
	RIDGED,
	@JsonProperty("ping_pong")
	PING_PONG,
	// Kantaja
	@JsonProperty("billow")
	BILLOW,
	@JsonProperty("domain_warp_progressive")
	DOMAIN_WARP_PROGRESSIVE,
	@JsonProperty("domain_warp_independent")
	DOMAIN_WARP_INDEPENDENT
}
