package info.kuonteje.voxeltest.render.light;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarF64;

public class Sunlight extends DirectionalLight
{
	public final CvarF64 rSunIntensity = VoxelTest.CONSOLE.cvars().getCvarF64C("r_sun_intensity", 8.0, Cvar.Flags.CHEAT, v -> Math.max(v, 0.0), (n, o) -> setIntensity((float)n));
	
	public Sunlight()
	{
		super(true);
		setIntensity(rSunIntensity.getAsFloat());
	}
}
