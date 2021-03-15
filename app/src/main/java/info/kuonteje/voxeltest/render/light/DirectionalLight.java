package info.kuonteje.voxeltest.render.light;

import static org.lwjgl.opengl.GL11C.*;

import java.util.Collection;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import info.kuonteje.voxeltest.render.DepthBuffer;
import info.kuonteje.voxeltest.render.GBuffer;
import info.kuonteje.voxeltest.render.Renderable;
import info.kuonteje.voxeltest.render.Renderer;
import info.kuonteje.voxeltest.render.ShaderProgram;
import info.kuonteje.voxeltest.render.SingleTexture;
import info.kuonteje.voxeltest.util.DoubleFrustum;

public class DirectionalLight implements ILight
{
	private static final Matrix4d projection, lightSpaceScale;
	private static final ThreadLocal<Matrix4d> tmp = ThreadLocal.withInitial(Matrix4d::new);
	
	public static final ShaderProgram deferredShader/*, forwardShader*/;
	
	static
	{
		float size = Renderer.rShadowmapSize.get() / 2;
		//float size = 32.0F;
		projection = new Matrix4d().setOrtho(-size, size, -size, size, 1000.0, 1.0, true);
		
		lightSpaceScale = new Matrix4d(
				0.5, 0.0, 0.0, 0.0,
				0.0, 0.5, 0.0, 0.0,
				0.0, 0.0, 1.0, 0.0,
				0.5, 0.5, 0.0, 1.0
				);
		
		deferredShader = GBuffer.createShader("lighting/directional");
		//forwardShader = GBuffer.createShader("lighting/forward/directional");
	}
	
	private final DepthBuffer shadowmap;
	
	private final Vector3d position = new Vector3d();
	private final Vector3d direction = new Vector3d();
	
	private final Vector3f color = new Vector3f(1.0F);
	
	private float intensity = 1.0F;
	
	private final Matrix4d pv = new Matrix4d();
	private final DoubleFrustum frustum = new DoubleFrustum();
	
	public DirectionalLight(boolean castsShadows)
	{
		shadowmap = castsShadows ? DepthBuffer.createShadowmap() : null;
	}
	
	public void generateShadowmap(Collection<Renderable> objects, ShaderProgram depthShader)
	{
		if(shadowmap != null)
		{
			shadowmap.bind();
			glClear(GL_DEPTH_BUFFER_BIT);
			
			depthShader.upload("pv", pv);
			
			objects.forEach(r ->
			{
				if(r.shouldRender(frustum)) r.renderShadow(depthShader);
			});
		}
	}
	
	public DirectionalLight setAngles(double x, double y, double z)
	{
		direction.set(x, y, z).normalize();
		
		projection.mul(tmp.get().identity().rotateX(-direction.x).rotateY(-direction.y).rotateZ(-direction.z)
				.translate(direction.x * 100.0, direction.y * 100.0, direction.z * 100.0), pv);
		
		frustum.set(pv, false);
		
		return this;
	}
	
	public DirectionalLight setAzEl(double azimuth, double elevation)
	{
		double sinAz = Math.sin(azimuth);
		double cosAz = Math.cos(azimuth);
		
		double sinEl = Math.sin(elevation);
		double cosEl = Math.cos(elevation);
		
		return setAngles(-sinAz * cosEl, -sinEl, cosAz * cosEl);
	}
	
	public DirectionalLight setIntensity(float intensity)
	{
		this.intensity = intensity;
		return this;
	}
	
	public DirectionalLight setColor(float r, float g, float b)
	{
		color.set(r, g, b);
		return this;
	}
	
	public DirectionalLight setColor(Vector3fc color)
	{
		this.color.set(color);
		return this;
	}
	
	@Override
	public Vector3dc position()
	{
		return position;
	}
	
	@Override
	public Vector3fc color()
	{
		return color;
	}
	
	@Override
	public float intensity()
	{
		return intensity;
	}
	
	public Vector3dc direction()
	{
		return direction;
	}
	
	public SingleTexture shadowmap()
	{
		return shadowmap == null ? null : shadowmap.texture();
	}
	
	public Matrix4d lightSpaceTransform()
	{
		return lightSpaceScale.mul(pv, tmp.get());
	}
}
