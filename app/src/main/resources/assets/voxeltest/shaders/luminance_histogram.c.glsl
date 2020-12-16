layout (local_size_x = 64) in;

const vec3 lumaK = vec3(0.2126, 0.7152, 0.0722);
const float zFar = 0.0;

layout ( binding = 0) uniform sampler2D color;
layout ( binding = 1) uniform sampler2D depth;

layout (std430, binding = 0) buffer luminanceHistogram {
	uint lumData[256];
};

uniform int colorWidth;
uniform vec2 skyContribution = vec2(0.5, 0.0);

shared uint data[256];

void main() {
	const uint thread = gl_LocalInvocationID.x;

	for(int i = 0; i < 4; i++) {
		data[thread + i * 64] = 0;
	}

	groupMemoryBarrier();
	barrier();

	const int pixelY = int(gl_WorkGroupID.x);

	for(int pos = 0; pos < colorWidth; pos += 64) {
		int pixelX = pos + int(thread);

		if(pixelX < colorWidth) {
			ivec2 pixelPos = ivec2(pixelX, pixelY);

			vec3 color = texelFetch(color, pixelPos, 0).xyz;
			float depth = texelFetch(depth, pixelPos * 4, 0).x;

			float luminance = log(mix(dot(color, lumaK), skyContribution.y, depth == zFar ? skyContribution.x : 0.0) + 1.0) * 128.0;

			atomicAdd(data[min(255, uint(luminance))], 1);
		}
	}

	groupMemoryBarrier();
	barrier();

	for(int i = 0; i < 4; i++) {
		const uint idx = thread + i * 64;
		atomicExchange(lumData[idx], data[idx]);
	}
}
