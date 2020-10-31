package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import info.kuonteje.voxeltest.assets.AssetLoader;
import info.kuonteje.voxeltest.assets.AssetType;
import info.kuonteje.voxeltest.util.Lazy;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ShaderProgram
{
	public static enum ShaderType
	{
		VERTEX(".v", GL_VERTEX_SHADER),
		FRAGMENT(".f", GL_FRAGMENT_SHADER);
		
		private final String suffix;
		private final int glType;
		
		private ShaderType(String suffix, int glType)
		{
			this.suffix = suffix;
			this.glType = glType;
		}
	}
	
	private static int lastBound = 0;
	
	private final int program;
	
	private final Lazy<Object2IntMap<String>> uniformCache = Lazy.of(Object2IntOpenHashMap::new);
	
	private void init(int vertex, int fragment, boolean destroyVertex, boolean destroyFragment)
	{
		glAttachShader(program, vertex);
		glAttachShader(program, fragment);
		
		glLinkProgram(program);
		
		try
		{
			if(glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE)
			{
				String log = glGetProgramInfoLog(program);
				
				glDeleteProgram(program);
				
				throw new RuntimeException("Failed to link shader program", new RuntimeException(log));
			}
		}
		finally
		{
			glDetachShader(program, vertex);
			glDetachShader(program, fragment);
			
			if(destroyVertex) glDeleteShader(vertex);
			if(destroyFragment) glDeleteShader(fragment);
		}
	}
	
	public ShaderProgram(int vertex, int fragment)
	{
		program = glCreateProgram();
		init(vertex, fragment, false, false);
	}
	
	public ShaderProgram(int vertex, String fragmentId)
	{
		int fragment = loadShaderObject(fragmentId, ShaderType.FRAGMENT);
		program = glCreateProgram();
		init(vertex, fragment, false, true);
	}
	
	public ShaderProgram(String vertexId, int fragment)
	{
		int vertex = loadShaderObject(vertexId, ShaderType.VERTEX);
		program = glCreateProgram();
		init(vertex, fragment, true, false);
	}
	
	public ShaderProgram(String vertexId, String fragmentId)
	{
		vertexId += ShaderType.VERTEX.suffix;
		fragmentId += ShaderType.FRAGMENT.suffix;
		
		String vertexSrc = AssetLoader.loadTextAsset(AssetType.SHADER, vertexId);
		String fragmentSrc = AssetLoader.loadTextAsset(AssetType.SHADER, fragmentId);
		
		int vertex = createShader(vertexId, ShaderType.VERTEX, vertexSrc);
		int fragment = createShader(fragmentId, ShaderType.FRAGMENT, fragmentSrc);
		
		program = glCreateProgram();
		init(vertex, fragment, true, true);
	}
	
	public ShaderProgram(String id)
	{
		this(id, id);
	}
	
	public int handle()
	{
		return program;
	}
	
	public int uniformHandle(String name)
	{
		return uniformCache.get().computeIntIfAbsent(name, s -> glGetUniformLocation(program, s));
	}
	
	public void bind()
	{
		bind(program);
	}
	
	public void unbind()
	{
		bind(0);
	}
	
	private static void bind(int handle)
	{
		if(handle != lastBound)
		{
			glUseProgram(handle);
			lastBound = handle;
		}
	}
	
	public void destroy()
	{
		glDeleteProgram(program);
	}
	
	public static int loadShaderObject(String id, ShaderType type)
	{
		id += type.suffix;
		return createShader(id, type, AssetLoader.loadTextAsset(AssetType.SHADER, id));
	}
	
	private static int createShader(String id, ShaderType type, String src)
	{
		int shader = glCreateShader(type.glType);
		
		glShaderSource(shader, src);
		glCompileShader(shader);
		
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
		{
			String log = glGetShaderInfoLog(shader);
			
			glDeleteShader(shader);
			
			throw new RuntimeException("Failed to load " + type.toString() + " shader \"" + id + "\"", new RuntimeException(log));
		}
		
		return shader;
	}
}
