#version 450 core

layout ( binding = 0) uniform sampler2D albedo;
layout ( binding = 1) uniform sampler2D position;
layout ( binding = 2) uniform sampler2D normal;
layout ( binding = 3) uniform sampler2D depth;

layout (location = 0) in vec2 uv;
layout (location = 0) out vec4 color;

void main() {
	color = texture(albedo, uv);
}
