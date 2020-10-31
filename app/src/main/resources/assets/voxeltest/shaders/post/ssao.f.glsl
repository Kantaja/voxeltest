#version 450 core

layout (binding = 0) uniform sampler2D sampler;

layout (location = 0) in vec2 uv;
layout (location = 0) out vec4 color;

void main() {
	// TODO
	color = texture(sampler, uv);
}
