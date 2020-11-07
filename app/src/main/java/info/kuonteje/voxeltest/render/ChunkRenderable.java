package info.kuonteje.voxeltest.render;

import java.util.function.IntSupplier;

import org.joml.FrustumIntersection;
import org.joml.Vector3dc;

import info.kuonteje.voxeltest.world.Chunk;

public class ChunkRenderable implements IRenderable
{
	private final Chunk chunk;
	private final boolean solid;
	
	private Runnable renderFunc = null;
	private IntSupplier triangleCountFunc = null;
	
	private double distanceSqToCamera = 0.0;
	
	public ChunkRenderable(Chunk chunk, boolean solid)
	{
		this.chunk = chunk;
		this.solid = solid;
	}
	
	public void setRenderer(ChunkRenderer renderer)
	{
		if(this.renderFunc == null)
		{
			if(solid)
			{
				renderFunc = renderer::renderSolid;
				triangleCountFunc = renderer::getSolidTriangles;
			}
			else
			{
				renderFunc = renderer::renderTranslucent;
				triangleCountFunc = renderer::getTranslucentTriangles;
			}
		}
	}
	
	@Override
	public boolean shouldRender(FrustumIntersection frustum)
	{
		return !chunk.empty() && renderFunc != null && triangleCountFunc.getAsInt() > 0 && chunk.testFrustum(frustum);
	}
	
	@Override
	public void render()
	{
		renderFunc.run();
	}
	
	@Override
	public void setCameraPosition(Vector3dc position)
	{
		distanceSqToCamera = position.distanceSquared(chunk.getCenter());
	}
	
	@Override
	public double distanceSqToCamera()
	{
		return distanceSqToCamera;
	}
}
