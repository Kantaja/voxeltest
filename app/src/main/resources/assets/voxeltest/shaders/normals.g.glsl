#version 450 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

layout (location = 0) in vec2 uv0[];
layout (location = 1) in float light0[];
layout (location = 2) in vec3 vertexPos[];

layout (location = 0) out vec2 uv;
layout (location = 1) out vec3 normal;
layout (location = 2) out float light;
layout (location = 3) out vec3 position;

void main() {
	vec3 a = (vertexPos[1] - vertexPos[0]).xyz;
	vec3 b = (vertexPos[2] - vertexPos[0]).xyz;

	vec3 n = normalize(cross(a, b));

	for(int i = 0; i < gl_in.length(); i++) {
		gl_Position = gl_in[i].gl_Position;
		gl_PrimitiveID = gl_PrimitiveIDIn;

		uv = uv0[i];
		normal = n;
		light = light0[i];
		position = vertexPos[i];

		EmitVertex();
	}

	EndPrimitive();
}
