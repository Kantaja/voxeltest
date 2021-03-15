package info.kuonteje.voxeltest.render.light;

import java.util.Collection;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarF64;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.render.GBuffer;
import info.kuonteje.voxeltest.render.Renderable;
import info.kuonteje.voxeltest.render.Renderer;
import info.kuonteje.voxeltest.render.ShaderProgram;
import info.kuonteje.voxeltest.util.MathUtil;

public class SunlightAndAmbient extends DirectionalLight
{
	public final CvarF64 rSunIntensity = VoxelTest.CONSOLE.cvars().cvarF64("r_sun_intensity", 5.6, Cvar.Flags.CHEAT, v -> Math.max(v, 0.0), (n, o) -> setIntensity((float)n));
	public final CvarF64 rAmbientStrength = VoxelTest.CONSOLE.cvars().cvarF64("r_ambient_strength", 0.3, Cvar.Flags.CHEAT, v -> MathUtil.clamp(v, 0.0, Renderer.WHITE_POINT), (n, o) -> setAmbientStrength((float)n));
	
	public final CvarI64 rEnableSun = VoxelTest.CONSOLE.cvars().cvarBool("r_enable_sun", true, Cvar.Flags.CHEAT, null);
	
	public final ShaderProgram deferredSunShader, deferredSunlessShader;
	//public final ShaderProgram forwardSunShader, forwardSunlessShader;
	
	public SunlightAndAmbient()
	{
		super(true);
		
		deferredSunShader = GBuffer.createShader("lighting/sun_ambient");
		deferredSunlessShader = GBuffer.createShader("lighting/sunless_ambient");
		
		//forwardSunShader = ShaderProgram.builder().vertex("lighting/forward/sun_ambient");
		//forwardSunlessShader = GBuffer.createShader("lighting/forward/sunless_ambient");
		
		setIntensity(rSunIntensity.asFloat());
		setAmbientStrength(rAmbientStrength.asFloat());
		
		deferredSunShader.upload("shadowmapSampler", shadowmap().bindlessHandle());
		//forwardSunShader.upload("shadowmapSampler", getShadowmap().getBindlessHandle());
	}
	
	@Override
	@Deprecated
	public DirectionalLight setIntensity(float intensity)
	{
		VoxelTest.addRenderHook(() ->
		{
			deferredSunShader.upload("sunIntensity", intensity);
			//forwardSunShader.upload("sunIntensity", intensity);
		});
		
		return super.setIntensity(intensity);
	}
	
	private void setAmbientStrength(float strength)
	{
		VoxelTest.addRenderHook(() ->
		{
			deferredSunShader.upload("ambientStrength", strength);
			deferredSunlessShader.upload("ambientStrength", strength);
			
			//forwardSunShader.upload("ambientStrength", strength);
			//forwardSunlessShader.upload("ambientStrength", strength);
		});
	}
	
	@Override
	public void generateShadowmap(Collection<Renderable> objects, ShaderProgram depthShader)
	{
		if(rEnableSun.asBool()) super.generateShadowmap(objects, depthShader);
	}
	
	public ShaderProgram deferredShader()
	{
		return rEnableSun.asBool() ? deferredSunShader : deferredSunlessShader;
	}
	
	/*
	public ShaderProgram forwardShader()
	{
		return rEnableSun.getAsBool() ? forwardSunShader : forwardSunlessShader;
	}
	 */
}
