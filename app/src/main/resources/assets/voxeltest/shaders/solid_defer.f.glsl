layout (location = 0) out vec4 albedoOut;
layout (location = 1) out vec4 positionOut;
layout (location = 2) out vec4 normalOut;

void main() {
	int triangleId = gl_PrimitiveID >> 1;
	uint layer = texLayerData[triangleId];

	vec4 texel = texture(texSamplers[layer >> 11], vec3(uv, layer & 0x7FF));

	if(texel.w < 0.5) {
		discard;
	} else {
		texel.w = 1.0;
	}

	albedoOut = texel * vec4(unpackUnorm4x8(tintData[triangleId]).xyz, 1.0);
	positionOut = vec4(vertexPos, 1.0);
	normalOut = vec4(normalize(cross(dFdxFine(vertexPos), dFdyFine(vertexPos))), 1.0);
}
