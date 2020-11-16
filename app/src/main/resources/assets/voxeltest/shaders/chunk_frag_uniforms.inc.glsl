const uint TEXTURE_ARRAY_COUNT = 16;

layout ( binding = 0) uniform usamplerBuffer texLayerSampler;
layout ( binding = 1) uniform samplerBuffer tintSampler;
layout ( binding = 2) uniform sampler2DArray texSamplers[TEXTURE_ARRAY_COUNT];

layout (location = 0) in vec2 uv;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec3 position;
