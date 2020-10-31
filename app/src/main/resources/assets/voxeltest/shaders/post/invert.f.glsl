#version 450 core

layout (binding = 0) uniform sampler2D sampler;

layout (location = 0) in vec2 uv;
layout (location = 0) out vec4 color;

void main() {
	color = vec4(vec3(1.0) - texture(sampler, uv).xyz, 1.0);
}
