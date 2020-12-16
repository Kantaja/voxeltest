package info.kuonteje.voxeltest.render;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

import org.joml.FrustumIntersection;
import org.joml.Vector3dc;

import info.kuonteje.voxeltest.world.Chunk;

public class ChunkRenderable extends Renderable
{
	private final Chunk chunk;
	private final boolean solid;
	
	private Consumer<ShaderProgram> renderGeomFunc = null;
	private Consumer<ShaderProgram> renderShadowFunc = null;
	private Consumer<ShaderProgram> renderFullFunc = null;
	
	private IntSupplier triangleCountFunc = null;
	
	public ChunkRenderable(Chunk chunk, boolean solid)
	{
		this.chunk = chunk;
		this.solid = solid;
	}
	
	public void setRenderer(ChunkRenderer renderer)
	{
		if(this.renderFullFunc == null)
		{
			if(solid)
			{
				renderGeomFunc = renderer::renderSolidGeometry;
				renderShadowFunc = renderer::renderSolidShadow;
				renderFullFunc = renderer::renderSolidFull;
				
				triangleCountFunc = renderer::getSolidTriangles;
			}
			else
			{
				renderGeomFunc = renderer::renderTranslucent;
				renderFullFunc = renderer::renderTranslucent;
				
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
		return !chunk.empty() && renderFullFunc != null && triangleCountFunc.getAsInt() > 0 && (frustum == null || chunk.testFrustum(frustum));
	}
	
	@Override
	public void renderGeometry(ShaderProgram shader)
	{
		renderGeomFunc.accept(shader);
	}
	
	@Override
	public void renderShadow(ShaderProgram shader)
	{
		renderShadowFunc.accept(shader);
	}
	
	@Override
	public void renderFull(ShaderProgram shader)
	{
		renderFullFunc.accept(shader);
	}
}
