const uint textureArrayCount = 16;

layout ( binding = 0) uniform usamplerBuffer texLayerSampler;
layout ( binding = 1) uniform samplerBuffer tintSampler;

layout (bindless_sampler) uniform sampler2DArray texSamplers[textureArrayCount];

layout (location = 0) in vec2 uv;
layout (location = 1) in vec3 vertexPos;
