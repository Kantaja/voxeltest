package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL32C.*;
import static org.lwjgl.opengl.GL40C.*;
import static org.lwjgl.opengl.GL43C.*;

public enum ShaderType
{
	VERTEX(".v", GL_VERTEX_SHADER),
	TESSELATION_CONTROL(".tc", GL_TESS_CONTROL_SHADER),
	TESSELATION_EVALUATION(".te", GL_TESS_EVALUATION_SHADER),
	GEOMETRY(".g", GL_GEOMETRY_SHADER),
	FRAGMENT(".f", GL_FRAGMENT_SHADER),
	COMPUTE(".c", GL_COMPUTE_SHADER);
	
	private final String suffix;
	private final int glType;
	
	private ShaderType(String suffix, int glType)
	{
		this.suffix = suffix;
		this.glType = glType;
	}
	
	public String suffix()
	{
		return suffix;
	}
	
	public int glType()
	{
		return glType;
	}
}
