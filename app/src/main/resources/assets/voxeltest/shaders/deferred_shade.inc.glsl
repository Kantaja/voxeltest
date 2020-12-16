layout (bindless_sampler) uniform sampler2D albedo;
layout (bindless_sampler) uniform sampler2D position;
layout (bindless_sampler) uniform sampler2D normal;
layout (bindless_sampler) uniform sampler2D depth;

layout (location = 0) in vec2 uv;
layout (location = 0) out vec4 color;
