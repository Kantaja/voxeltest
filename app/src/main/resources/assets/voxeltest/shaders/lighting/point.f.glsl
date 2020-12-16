uniform struct {
	vec3 position;
	vec3 color;
	vec4 attenuation; // [intensity, k, linear factor, quadratic factor]
} lightData;

void main() {
	vec4 albedoTexel = texture(albedo, uv);
	vec3 diff = lightData.position - texture(position, uv).xyz;

	float distance = length(diff);
	float intensity = lightData.attenuation.x / (lightData.attenuation.y + distance * lightData.attenuation.z + distance * distance * lightData.attenuation.w);

	color.xyz = vec3(albedoTexel.xyz * max(dot(texture(normal, uv).xyz, normalize(diff)), 0.0) * lightData.color * intensity);
}
