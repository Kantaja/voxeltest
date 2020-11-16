const uint TEXTURE_ARRAY_COUNT = 16;

// Avoid context errors for invalid locations
layout ( binding = 2) uniform usamplerBuffer texLayerSampler;
layout ( binding = 3) uniform samplerBuffer tintSampler;
layout (location = 4) uniform int baseTriangleId;
layout ( binding = 5) uniform sampler2DArray texSamplers[16];

layout (location = 1) in vec3 normal;
layout (location = 0) out vec4 color;

void main() {
	color = vec4(normal, 1.0);

	if(color.x < 0.0) {
		color.x *= -0.25;
	}

	if(color.y < 0.0) {
		color.y *= -0.25;
	}

	if(color.z < 0.0) {
		color.z *= -0.25;
	}
}
