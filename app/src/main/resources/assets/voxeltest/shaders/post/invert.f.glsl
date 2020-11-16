void main() {
	color = vec4(vec3(1.0) - texture(colorSampler, uv).xyz, 1.0);
}
