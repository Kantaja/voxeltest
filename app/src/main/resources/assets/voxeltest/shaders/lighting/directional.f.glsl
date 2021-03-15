layout (bindless_sampler) uniform sampler2D shadowmap;

uniform mat4 lightMatrix;

uniform struct {
	vec3 direction;
	vec3 color;
	float intensity;
} lightData;

void main() {
	vec4 albedoTexel = getAlbedo();
	color.xyz = albedoTexel.xyz * max(dot(getNormal(), -lightData.direction), 0.0) * lightData.color * lightData.intensity;
}
