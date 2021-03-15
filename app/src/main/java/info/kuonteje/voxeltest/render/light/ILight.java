package info.kuonteje.voxeltest.render.light;

import org.joml.Vector3dc;
import org.joml.Vector3fc;

public interface ILight
{
	Vector3dc position();
	Vector3fc color();
	float intensity();
}
