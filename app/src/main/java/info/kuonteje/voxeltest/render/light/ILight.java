package info.kuonteje.voxeltest.render.light;

import org.joml.Vector3fc;

public interface ILight
{
	Vector3fc getPosition();
	Vector3fc getColor();
	float getIntensity();
}
