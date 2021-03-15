const uint textureArrayCount = 16;

layout (std430, binding = 0) readonly buffer texLayers {
	uint texLayerData[];
};

layout (std430, binding = 1) readonly buffer tints {
	uint tintData[];
};

layout (bindless_sampler) uniform sampler2DArray texSamplers[textureArrayCount];

layout (location = 0) in vec2 uv;
layout (location = 1) in vec3 vertexPos;
