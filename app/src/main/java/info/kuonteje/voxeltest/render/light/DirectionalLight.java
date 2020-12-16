package info.kuonteje.voxeltest.render.light;

import static org.lwjgl.opengl.GL11C.*;

import java.util.Set;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import info.kuonteje.voxeltest.render.DepthBuffer;
import info.kuonteje.voxeltest.render.Renderable;
import info.kuonteje.voxeltest.render.ShaderProgram;
import info.kuonteje.voxeltest.render.SingleTexture;

public class DirectionalLight implements ILight
{
	private static final Matrix4f projection, lightSpaceScale;
	private static final ThreadLocal<Matrix4f> tmp = ThreadLocal.withInitial(Matrix4f::new);
	
	static
	{
		//float size = Renderer.getShadowmapSize() / 2;
		float size = 384.0F;
		projection = new Matrix4f().setOrtho(-size, size, -size, size, 1000.0F, 1.0F, true);
		
		lightSpaceScale = new Matrix4f(
				0.5F, 0.0F, 0.0F, 0.0F,
				0.0F, 0.5F, 0.0F, 0.0F,
				0.0F, 0.0F, 1.0F, 0.0F,
				0.5F, 0.5F, 0.0F, 1.0F
				);
	}
	
	private final DepthBuffer shadowmap;
	
	private final Vector3f position = new Vector3f();
	private final Vector3f direction = new Vector3f();
	
	private final Vector3f color = new Vector3f(1.0F);
	
	private float intensity = 1.0F;
	
	private final Matrix4f pv = new Matrix4f();
	private final FrustumIntersection frustum = new FrustumIntersection();
	
	public DirectionalLight(boolean castsShadows)
	{
		shadowmap = castsShadows ? DepthBuffer.createShadowmap() : null;
	}
	
	public void generateShadowmap(Set<Renderable> objects, ShaderProgram depthShader)
	{
		shadowmap.bind();
		glClear(GL_DEPTH_BUFFER_BIT);
		
		depthShader.upload("pv", pv);
		
		objects.forEach(r ->
		{
			if(r.shouldRender(frustum)) r.renderShadow(depthShader);
		});
	}
	
	public DirectionalLight setAngles(double x, double y, double z)
	{
		direction.set(x, y, z).normalize();
		
		projection.mul(tmp.get().identity().rotateX(-direction.x).rotateY(-direction.y).rotateZ(-direction.z)
				.translate(direction.x * 100.0F, direction.y * 100.0F, direction.z * 100.0F), pv);
		
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
	public Vector3fc getPosition()
	{
		return position;
	}
	
	@Override
	public Vector3fc getColor()
	{
		return color;
	}
	
	@Override
	public float getIntensity()
	{
		return intensity;
	}
	
	public Vector3fc getDirection()
	{
		return direction;
	}
	
	public SingleTexture getShadowmap()
	{
		return shadowmap.getTexture();
	}
	
	public Matrix4f getLightSpaceTransform()
	{
		return lightSpaceScale.mul(pv, tmp.get());
	}
}
