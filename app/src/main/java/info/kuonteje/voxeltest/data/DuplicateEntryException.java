package info.kuonteje.voxeltest.data;

public class DuplicateEntryException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	
	public DuplicateEntryException(String message)
	{
		super(message);
	}
}
