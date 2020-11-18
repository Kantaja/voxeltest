const float A = 0.15;
const float B = 0.50;
const float C = 0.10;
const float D = 0.20;
const float E = 0.02;
const float F = 0.30;

vec3 uc2Partial(vec3 texel) {
	return ((texel * (A * texel + C * B) + D * E) / (texel * (A * texel + B) + D * F)) - E / F;
}

void main() {
	const float exposureBias = 2.0;
	const vec3 whiteScale = vec3(1.0) / uc2Partial(vec3(11.2));

	color = vec4(uc2Partial(texture(colorSampler, uv).xyz * exposureBias) * whiteScale, 1.0);
}
