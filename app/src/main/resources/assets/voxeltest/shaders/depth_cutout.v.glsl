uniform mat4 pv;

layout (location = 0) in vec3 posIn;
layout (location = 1) in vec2 uvIn;

layout (location = 0) out vec2 uv;

void main() {
	gl_Position = pv * vec4(posIn, 1.0);

	uv = uvIn;
}
