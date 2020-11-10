#version 450 core

const uint TEXTURE_ARRAY_COUNT = 16;

layout ( binding = 2) uniform usamplerBuffer texLayerSampler;
layout ( binding = 3) uniform samplerBuffer tintSampler;
layout (location = 4) uniform int baseTriangleId;
layout ( binding = 5) uniform sampler2DArray texSamplers[TEXTURE_ARRAY_COUNT];

layout (location = 0) in vec2 uv;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec3 position;

layout (location = 0) out vec4 albedoOut;
layout (location = 1) out vec4 positionOut;
layout (location = 2) out vec4 normalOut;

void main() {
	int triangleId = (baseTriangleId + gl_PrimitiveID) >> 1;
	uint layer = texelFetch(texLayerSampler, triangleId).x;

	vec4 texel = texture(texSamplers[layer >> 11], vec3(uv, layer & 0x7FF));

	if(texel.w < 0.5) {
		discard;
	} else {
		texel.w = 1.0;
	}

	albedoOut = texel * vec4(texelFetch(tintSampler, triangleId).xyz, 1.0);
	positionOut = vec4(position, 1.0);
	normalOut = vec4(normal, 0.0);
}
