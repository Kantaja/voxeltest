package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;

import java.util.EnumMap;
import java.util.Set;

import info.kuonteje.voxeltest.assets.AssetLoader;
import info.kuonteje.voxeltest.assets.AssetType;
import info.kuonteje.voxeltest.util.Either;
import info.kuonteje.voxeltest.util.Lazy;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;

public class ShaderProgram
{
	private static int lastBound = 0;
	
	private final int program;
	
	private final Lazy<Object2IntMap<String>> uniformCache = Lazy.of(Object2IntOpenHashMap::new);
	
	private ShaderProgram(EnumMap<ShaderType, Either<String, Integer>> builderMap)
	{
		final Lazy<Set<Integer>> providedShaders = Lazy.of(ObjectAVLTreeSet::new);
		final Set<Integer> shaders = new ObjectAVLTreeSet<>();
		
		builderMap.forEach((type, s) -> s.match(id -> shaders.add(loadShaderObject(id, type)), shader ->
		{
			providedShaders.get().add(shader);
			shaders.add(shader);
		}));
		
		program = glCreateProgram();
		
		shaders.forEach(s -> glAttachShader(program, s));
		
		glLinkProgram(program);
		
		shaders.forEach(s -> glDetachShader(program, s));
		
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
			shaders.forEach(s ->
			{
				if(!providedShaders.got() || !providedShaders.get().contains(s)) glDeleteShader(s);
			});
		}
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
		id += type.getSuffix();
		
		String src = AssetLoader.loadTextAsset(AssetType.SHADER, id);
		
		int shader = glCreateShader(type.getGlType());
		
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
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	public static class Builder
	{
		private final EnumMap<ShaderType, Either<String, Integer>> shaders = new EnumMap<>(ShaderType.class);
		
		private Builder() {}
		
		public Builder vertex(String id)
		{
			shaders.put(ShaderType.VERTEX, Either.left(id));
			return this;
		}
		
		public Builder tessControl(String id)
		{
			shaders.put(ShaderType.TESSELATION_CONTROL, Either.left(id));
			return this;
		}
		
		public Builder tessEval(String id)
		{
			shaders.put(ShaderType.TESSELATION_EVALUATION, Either.left(id));
			return this;
		}
		
		public Builder geometry(String id)
		{
			shaders.put(ShaderType.GEOMETRY, Either.left(id));
			return this;
		}
		
		public Builder fragment(String id)
		{
			shaders.put(ShaderType.FRAGMENT, Either.left(id));
			return this;
		}
		
		public Builder compute(String id)
		{
			shaders.put(ShaderType.COMPUTE, Either.left(id));
			return this;
		}
		
		public Builder vertexFragment(String id)
		{
			shaders.put(ShaderType.VERTEX, Either.left(id));
			shaders.put(ShaderType.FRAGMENT, Either.left(id));
			return this;
		}
		
		public Builder vertex(int id)
		{
			shaders.put(ShaderType.VERTEX, Either.right(id));
			return this;
		}
		
		public Builder tessControl(int id)
		{
			shaders.put(ShaderType.TESSELATION_CONTROL, Either.right(id));
			return this;
		}
		
		public Builder tessEval(int id)
		{
			shaders.put(ShaderType.TESSELATION_EVALUATION, Either.right(id));
			return this;
		}
		
		public Builder geometry(int id)
		{
			shaders.put(ShaderType.GEOMETRY, Either.right(id));
			return this;
		}
		
		public Builder fragment(int id)
		{
			shaders.put(ShaderType.FRAGMENT, Either.right(id));
			return this;
		}
		
		public Builder compute(int id)
		{
			shaders.put(ShaderType.COMPUTE, Either.right(id));
			return this;
		}
		
		public ShaderProgram create()
		{
			return new ShaderProgram(shaders);
		}
	}
}
