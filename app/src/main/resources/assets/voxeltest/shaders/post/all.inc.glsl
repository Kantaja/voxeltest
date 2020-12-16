layout (bindless_sampler) uniform sampler2D colorSampler;
layout (bindless_sampler) uniform sampler2D depthSampler;

layout (location = 0) in vec2 uv;
layout (location = 0) out vec4 color;
