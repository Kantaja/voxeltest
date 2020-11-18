package info.kuonteje.voxeltest.render;

import java.util.function.IntSupplier;

import org.joml.FrustumIntersection;
import org.joml.Vector3dc;

import info.kuonteje.voxeltest.world.Chunk;

public class ChunkRenderable extends Renderable
{
	private final Chunk chunk;
	private final boolean solid;
	
	private Runnable renderFunc = null;
	private IntSupplier triangleCountFunc = null;
	
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
	public Vector3dc getCenter()
	{
		return chunk.getCenter();
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
}
