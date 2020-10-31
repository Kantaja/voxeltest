#version 450 core

layout (location = 0) in vec3 posIn;
layout (location = 1) in vec2 uvIn;
layout (location = 2) in float lightIn;

layout (location = 0) out vec2 uv;
layout (location = 1) out float light;

layout (location = 0) uniform mat4 pv;
//layout (location = 1) uniform mat4 model;

void main() {
	gl_Position = pv * vec4(posIn, 1.0);

	uv = uvIn;
	light = lightIn;
}
