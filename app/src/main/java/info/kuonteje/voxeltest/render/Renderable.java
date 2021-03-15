package info.kuonteje.voxeltest.render;

import java.util.concurrent.atomic.AtomicLong;

import org.joml.Vector3dc;

import info.kuonteje.voxeltest.util.DoubleFrustum;

public abstract class Renderable
{
	private static final AtomicLong objectIds = new AtomicLong();
	
	private final long objectId;
	
	private double distanceSqToCamera = 0.0;
	
	public Renderable()
	{
		objectId = objectIds.getAndIncrement();
	}
	
	public abstract Vector3dc center();
	
	public abstract void renderGeometry(ShaderProgram shader);
	public abstract void renderShadow(ShaderProgram shader);
	public abstract void renderFull(ShaderProgram shader);
	
	public abstract boolean shouldRender(DoubleFrustum frustum);
	
	public void setCameraPosition(Vector3dc position)
	{
		distanceSqToCamera = position.distanceSquared(center());
	}
	
	public double distanceSqToCamera()
	{
		return distanceSqToCamera;
	}
	
	public final long objectId()
	{
		return objectId;
	}
}
