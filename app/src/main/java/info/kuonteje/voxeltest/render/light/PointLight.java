package info.kuonteje.voxeltest.render.light;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import info.kuonteje.voxeltest.render.GBuffer;
import info.kuonteje.voxeltest.render.ShaderProgram;

public class PointLight implements ILight
{
	public static final ShaderProgram deferredShader/*, forwardShader*/;
	
	static
	{
		deferredShader = GBuffer.createShader("lighting/point");
		//forwardShader = GBuffer.createShader("lighting/forward/point");
	}
	
	private static final float DEFAULT_RANGE = 64.0F;
	
	private final Vector3d position = new Vector3d();
	private final Vector3f color = new Vector3f(1.0F);
	private final Vector4f attenuation = new Vector4f(1.0F, 1.0F, 0.0F, 0.0F);
	
	public PointLight()
	{
		setRange(DEFAULT_RANGE);
	}
	
	public PointLight setPosition(double x, double y, double z)
	{
		position.set(x, y, z);
		return this;
	}
	
	public PointLight setPosition(Vector3dc position)
	{
		this.position.set(position);
		return this;
	}
	
	public PointLight setIntensity(float intensity)
	{
		attenuation.x = intensity;
		return this;
	}
	
	public PointLight setColor(float r, float g, float b)
	{
		color.set(r, g, b);
		return this;
	}
	
	public PointLight setColor(Vector3fc color)
	{
		this.color.set(color);
		return this;
	}
	
	public PointLight setRange(float range)
	{
		attenuation.z = 4.5F / range;
		attenuation.w = 75.0F / (range * range);
		
		return this;
	}
	
	public PointLight setAttenuationConstant(float k)
	{
		attenuation.y = k;
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
		return attenuation.x;
	}
	
	public Vector4fc attenuationData()
	{
		return attenuation;
	}
}
