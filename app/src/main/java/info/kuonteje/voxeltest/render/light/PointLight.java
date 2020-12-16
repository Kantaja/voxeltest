package info.kuonteje.voxeltest.render.light;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class PointLight implements ILight
{
	private static final float DEFAULT_RANGE = 64.0F;
	
	private final Vector3f position = new Vector3f();
	private final Vector3f color = new Vector3f(1.0F);
	private final Vector4f attenuation = new Vector4f(1.0F, 1.0F, 0.0F, 0.0F);
	
	public PointLight()
	{
		setRange(DEFAULT_RANGE);
	}
	
	public PointLight setPosition(float x, float y, float z)
	{
		position.set(x, y, z);
		return this;
	}
	
	public PointLight setPosition(Vector3fc position)
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
		return attenuation.x;
	}
	
	public Vector4fc getAttenuationData()
	{
		return attenuation;
	}
}
