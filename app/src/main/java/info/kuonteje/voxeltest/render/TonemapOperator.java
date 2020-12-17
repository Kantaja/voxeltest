package info.kuonteje.voxeltest.render;

public enum TonemapOperator
{
	UC2(0),
	ACES(1),
	REINHARD(2);
	
	private final int id;
	
	private TonemapOperator(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
}
