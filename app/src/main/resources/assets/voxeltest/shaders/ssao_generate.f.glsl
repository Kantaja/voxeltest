layout (bindless_sampler) uniform sampler2D noiseSampler;
layout (bindless_sampler) uniform samplerBuffer samples;

uniform mat4 projection;
uniform mat4 view;

uniform vec2 noiseScale;

uniform uint aoSamples;
uniform float aoRadius;
uniform float aoStrength;

const float zFar = 0.0;

void main() {
	if(getDepth() > zFar) {
		vec3 noise = texture(noiseSampler, uv * noiseScale).xyz;

		vec3 pos = (view * vec4(getPosition(), 1.0)).xyz;
		vec3 norm = (view * vec4(getNormal(), 0.0)).xyz;

		vec3 tangent = normalize(noise - norm * dot(noise, norm));

		mat3 tangentToView = mat3(
			tangent,
			cross(norm, tangent),
			norm
		);

		float occlusion = 0.0;

		for(int i = 0; i < aoSamples; i++) {
			vec3 samplePos = pos + (tangentToView * texelFetch(samples, i).xyz) * aoRadius;

			vec4 offset = projection * vec4(samplePos, 1.0);
			float d = (view * vec4(texture(position, (offset.xy / offset.w) * 0.5 + 0.5).xyz, 1.0)).z;

			occlusion += ((d >= samplePos.z + 0.025) ? 1.0 : 0.0) * smoothstep(0.0, 1.0, aoRadius / abs(samplePos.z - d));
		}

		color = vec4(1.0 - min((occlusion * aoStrength) / float(aoSamples), 1.0), 0.0, 0.0, 1.0);
	} else {
		color = vec4(1.0);
	}
}
