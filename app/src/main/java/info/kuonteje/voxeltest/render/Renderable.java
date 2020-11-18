package info.kuonteje.voxeltest.render;

import java.util.concurrent.atomic.AtomicLong;

import org.joml.FrustumIntersection;
import org.joml.Vector3dc;

public abstract class Renderable
{
	private static final AtomicLong objectIds = new AtomicLong();
	
	private final long objectId;
	
	private double distanceSqToCamera = 0.0;
	
	public Renderable()
	{
		objectId = objectIds.getAndIncrement();
	}
	
	public abstract Vector3dc getCenter();
	
	public abstract void render();
	public abstract boolean shouldRender(FrustumIntersection frustum);
	
	public void setCameraPosition(Vector3dc position)
	{
		distanceSqToCamera = position.distanceSquared(getCenter());
	}
	
	public double distanceSqToCamera()
	{
		return distanceSqToCamera;
	}
	
	public final long getObjectId()
	{
		return objectId;
	}
}
