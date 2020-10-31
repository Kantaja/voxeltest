package info.kuonteje.voxeltest.render;

import org.joml.FrustumIntersection;

public interface IRenderable
{
	void render();
	boolean shouldRender(FrustumIntersection frustum);
}
