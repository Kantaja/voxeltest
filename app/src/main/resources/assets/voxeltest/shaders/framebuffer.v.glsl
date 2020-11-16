layout (location = 0) out vec2 uv;

void main() {
	uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
	gl_Position = vec4(uv * 2.0F + -1.0F, 0.0F, 1.0F);
}
