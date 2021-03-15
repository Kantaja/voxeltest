package info.kuonteje.voxeltest.render;

public enum TonemapOperator
{
	CLAMP(0),
	UC2(1),
	ACES(2),
	REINHARD(3);
	
	private final int id;
	
	private TonemapOperator(int id)
	{
		this.id = id;
	}
	
	public int id()
	{
		return id;
	}
}
