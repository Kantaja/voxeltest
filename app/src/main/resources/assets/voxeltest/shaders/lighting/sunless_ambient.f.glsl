uniform float ambientStrength;

void main() {
	color = getAlbedo() * ambientStrength;
}
