layout ( binding = 0) uniform sampler2D colorSampler;
layout ( binding = 1) uniform sampler2D depthSampler;

layout (location = 0) in vec2 uv;
layout (location = 0) out vec4 color;
