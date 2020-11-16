uniform int baseTriangleId;

layout (location = 0) out vec4 color;

void main() {
	int triangleId = (baseTriangleId + gl_PrimitiveID) >> 1;
	uint layer = texelFetch(texLayerSampler, triangleId).x;

	color = texture(texSamplers[layer >> 11], vec3(uv, layer & 0x7FF)) * vec4(texelFetch(tintSampler, triangleId).xyz, 1.0);
}
