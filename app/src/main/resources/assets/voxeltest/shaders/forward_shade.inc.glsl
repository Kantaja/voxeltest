vec4 getAlbedo() {
	int triangleId = (baseTriangleId + gl_PrimitiveID) >> 1;
	uint layer = texLayerData[triangleId];

	return texture(texSamplers[layer >> 11], vec3(uv, layer & 0x7FF)) * vec4(unpackUnorm4x8(tintData[triangleId]).xyz, 1.0);
}

vec4 getPosition() {
	return vertexPos;
}

vec4 getNormal() {
	return normalize(cross(dFdxFine(vertexPos), dFdyFine(vertexPos)));
}
