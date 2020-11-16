layout (location = 0) out vec4 albedoOut;
layout (location = 1) out vec4 positionOut;
layout (location = 2) out vec4 normalOut;

void main() {
	int triangleId = gl_PrimitiveID >> 1;
	uint layer = texelFetch(texLayerSampler, triangleId).x;

	vec4 texel = texture(texSamplers[layer >> 11], vec3(uv, layer & 0x7FF));

	if(texel.w < 0.5) {
		discard;
	} else {
		texel.w = 1.0;
	}

	albedoOut = texel * vec4(texelFetch(tintSampler, triangleId).xyz, 1.0);
	positionOut = vec4(position, 1.0);
	normalOut = vec4(normal, 1.0);
}
