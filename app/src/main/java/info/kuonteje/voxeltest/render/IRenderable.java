package info.kuonteje.voxeltest.render;

import org.joml.FrustumIntersection;
import org.joml.Vector3dc;

public interface IRenderable
{
	void render();
	boolean shouldRender(FrustumIntersection frustum);
	
	void setCameraPosition(Vector3dc position);
	double distanceSqToCamera();
}
