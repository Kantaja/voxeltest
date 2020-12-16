layout (bindless_sampler) uniform sampler2D lumSampler;
layout (bindless_sampler) uniform sampler2D aoSampler;

uniform bool ssao;

uniform vec2 size;
uniform int halfNoiseSize;

uniform float exposure;

const mat3 acesIn = mat3(
		0.59719, 0.07600, 0.02840,
		0.35458, 0.90834, 0.13383,
		0.04823, 0.01566, 0.83777
);

const mat3 acesOut = mat3(
		 1.60475, -0.10208, -0.00327,
		-0.53108,  1.10813, -0.07276,
		-0.07367, -0.00605,  1.07602
);

vec3 acesRrtOdt(vec3 texel) {
	vec3 a = texel * (texel + 0.0245786) - 0.000090537;
	vec3 b = texel * (0.983729 * texel + 0.432951) + 0.238081;
	return a / b;
}

// Stephen Hill's ACES fit
vec3 tonemapAces(vec3 texel) {
	return acesOut * acesRrtOdt(acesIn * (texel * exposure));
}

vec3 uc2Curve(vec3 texel) {
	const float A = 0.15;
	const float B = 0.50;
	const float C = 0.10;
	const float D = 0.20;
	const float E = 0.02;
	const float F = 0.30;

	return ((texel * (A * texel + C * B) + D * E) / (texel * (A * texel + B) + D * F)) - E / F;
}

vec3 tonemapUc2(vec3 texel) {
	const float whitePoint = 11.2;
	const vec3 whiteScale = vec3(1.0) / uc2Curve(vec3(whitePoint));

	return uc2Curve(texel * exposure * 2.0) * whiteScale;
}

float blurAo() {
	float blurred = 0.0;

	for(int x = -halfNoiseSize; x < halfNoiseSize; x++) {
		for(int y = -halfNoiseSize; y < halfNoiseSize; y++) {
			blurred += texture(aoSampler, uv + vec2(float(x), float(y)) * size).x;
		}
	}

	return blurred / (float(halfNoiseSize * halfNoiseSize) * 4.0);
}

void main() {
	// TODO eye adaptation

	color = vec4(texture(colorSampler, uv).xyz, 1.0);

	float luminance = texelFetch(lumSampler, ivec2(0, 0), 0).x;
	luminance = sqrt(luminance);

	float ao = ssao ? blurAo() : 1.0;
	color.xyz *= ao;

	// config?
	color.xyz = tonemapUc2(color.xyz);
	//color.xyz = tonemapAces(color.xyz);
}
