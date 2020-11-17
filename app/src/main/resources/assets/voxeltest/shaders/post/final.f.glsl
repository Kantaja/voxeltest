uniform float gamma = 2.2;

void main() {
	color = vec4(pow(texture(colorSampler, uv).xyz, vec3(1.0 / gamma)), 1.0);
}
