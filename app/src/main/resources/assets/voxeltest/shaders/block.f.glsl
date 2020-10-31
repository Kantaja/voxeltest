#version 450 core

const uint TEXTURE_ARRAY_COUNT = 16;

layout (location = 0) in vec2 uv;
layout (location = 1) in float light;

layout (location = 0) out vec4 color;

layout (binding = 2) uniform usamplerBuffer texLayerSampler;
layout (binding = 3) uniform samplerBuffer tintSampler;
layout (location = 4) uniform int baseTriangleId;
layout (binding = 5) uniform sampler2DArray texSamplers[TEXTURE_ARRAY_COUNT];

void main() {
	int triangleId = (baseTriangleId + gl_PrimitiveID) >> 1;
	uint layer = texelFetch(texLayerSampler, triangleId).x;
	color = texture(texSamplers[layer >> 11], vec3(uv, layer & 0x7FF)) * vec4(texelFetch(tintSampler, triangleId).xyz, 1.0) * vec4(light, light, light, 1.0);
}