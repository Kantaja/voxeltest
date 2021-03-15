layout (local_size_x = 64) in;

layout (r32f, binding = 0) uniform image2D finalLuminance;

layout (std430, binding = 0) readonly buffer luminanceHistogram {
	uint lumData[256];
};

uniform uvec2 downscaledSize;

shared uint data[256];

uniform float time;

void main() {
	const uint thread = gl_LocalInvocationID.x;

	for(uint i = 0; i < 4; i++) {
		const uint idx = thread + i * 64;
		data[idx] = lumData[idx];
	}

	groupMemoryBarrier();
	barrier();

	if(thread == 0) {
		imageStore(finalLuminance, ivec2(1, 0), imageLoad(finalLuminance, ivec2(0, 0)));

		const uint totalPixels = downscaledSize.x * downscaledSize.y;
		uint processedPixels = 0;

		float finalLuminanceValue = 0.0;

		for(uint currentLuminance = 0; currentLuminance < 256; currentLuminance++) {
			uint currentPixels = data[currentLuminance];
			processedPixels += currentPixels;

			float decodedLuminance = exp((float(currentLuminance) + 0.5) / 128.0) - 1.0;

			finalLuminanceValue += decodedLuminance * float(currentPixels);
		}

		imageStore(finalLuminance, ivec2(0, 0), vec4(finalLuminanceValue / float(totalPixels), 0.0, 0.0, 0.0));
	}
}
