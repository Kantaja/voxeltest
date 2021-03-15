layout (bindless_sampler) uniform sampler2D shadowmapSampler;

uniform mat4 lightPv;

uniform float ambientStrength;

uniform vec3 sunDir;
uniform float sunIntensity;

void main() {
	color = getAlbedo();

	float sunlight = clamp(dot(getNormal(), -sunDir), 0.0, 1.0) * sunIntensity;

	vec4 lightSpacePos = lightPv * vec4(getPosition(), 1.0);
	float shadow = (lightSpacePos.z < texture(shadowmapSampler, lightSpacePos.xy).x) ? 0.0 : 1.0;

	color.xyz *= ambientStrength + sunlight * shadow;
}
