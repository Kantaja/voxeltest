uniform struct {
	vec3 position;
	vec3 color;
	vec4 attenuation; // [intensity, k, linear factor, quadratic factor]
} lightData;

void main() {
	vec4 albedoTexel = getAlbedo();
	vec3 diff = lightData.position - getPosition();

	float distance = length(diff);
	float intensity = lightData.attenuation.x / (lightData.attenuation.y + distance * lightData.attenuation.z + distance * distance * lightData.attenuation.w);

	color.xyz = albedoTexel.xyz * max(dot(getNormal(), normalize(diff)), 0.0) * lightData.color * intensity;
}
