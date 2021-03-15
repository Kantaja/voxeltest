void main() {
	uint layer = texLayerData[gl_PrimitiveID >> 1];
	vec4 texel = texture(texSamplers[layer >> 11], vec3(uv, layer & 0x7FF));

	if(texel.w < 0.1) {
		discard;
	}
}
