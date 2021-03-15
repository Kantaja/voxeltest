const vec3 lumaK = vec3(0.2126, 0.7152, 0.0722);

void main() {
	color = vec4(vec3(dot(texture(colorSampler, uv).xyz, lumaK)), 1.0);
}
