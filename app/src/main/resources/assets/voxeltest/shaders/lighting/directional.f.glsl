layout (bindless_sampler) uniform sampler2D shadowmap;

uniform mat4 lightMatrix;

uniform struct {
	vec3 direction;
	vec3 color;
	float intensity;
} lightData;

void main() {
	vec4 albedoTexel = texture(albedo, uv);
	color.xyz = vec3(albedoTexel.xyz * max(dot(texture(normal, uv).xyz, -lightData.direction), 0.0) * lightData.color * lightData.intensity);
}
