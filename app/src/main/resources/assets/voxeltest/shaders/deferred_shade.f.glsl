layout ( binding = 0) uniform sampler2D albedo;
layout ( binding = 1) uniform sampler2D position;
layout ( binding = 2) uniform sampler2D normal;
layout ( binding = 3) uniform sampler2D depth;

uniform float ambient = 0.3;
uniform vec3 sunDir;
uniform float sunIntensity = 6.0;

layout (location = 0) in vec2 uv;
layout (location = 0) out vec4 color;

void main() {
	float sunlight = clamp(dot(texture(normal, uv).xyz, sunDir) + 0.07, 0.0, 1.0) * sunIntensity;
	color = vec4(texture(albedo, uv).xyz * max(ambient, sunlight), 1.0);
}
