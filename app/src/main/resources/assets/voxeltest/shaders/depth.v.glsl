uniform mat4 pv;

layout (location = 0) in vec3 posIn;

void main() {
	gl_Position = pv * vec4(posIn, 1.0);
}
