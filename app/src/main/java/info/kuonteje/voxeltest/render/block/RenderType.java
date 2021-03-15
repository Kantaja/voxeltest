package info.kuonteje.voxeltest.render.block;

public enum RenderType
{
	NONE(false),
	SOLID(true),
	// Considered solid for meshing, but transparent for face culling
	// Fragments discarded if alpha < 0.5, otherwise alpha clamped to 1.0
	CUTOUT(false),
	TRANSLUCENT(false);
	
	private final boolean isOpaque;
	
	private RenderType(boolean isOpaque)
	{
		this.isOpaque = isOpaque;
	}
	
	public boolean isOpaque()
	{
		return isOpaque;
	}
}
