package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL41C.*;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Matrix3dc;
import org.joml.Matrix3fc;
import org.joml.Matrix4dc;
import org.joml.Matrix4fc;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3dc;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4dc;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
import org.lwjgl.system.MemoryStack;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.assets.AssetLoader;
import info.kuonteje.voxeltest.data.objects.AssetTypes;
import info.kuonteje.voxeltest.util.Either;
import info.kuonteje.voxeltest.util.IDestroyable;
import info.kuonteje.voxeltest.util.Lazy;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ShaderProgram implements IDestroyable
{
	private static final String SHADER_PRE = "#version 450 core\n#extension GL_ARB_bindless_texture : enable\n";
	
	private static final Map<String, String> includes = new Object2ObjectOpenHashMap<>();
	
	private static int lastBound = 0;
	
	private final int program;
	
	private final Lazy<Object2IntMap<String>> uniformCache = Lazy.of(Object2IntOpenHashMap::new);
	
	private boolean destroyed = false;
	
	private ShaderProgram(EnumMap<ShaderType, Either<String, Integer>> builderMap, EnumMap<ShaderType, List<String>> includeIds)
	{
		VoxelTest.addDestroyable(this);
		
		final Lazy<Set<Integer>> providedShaders = Lazy.of(ObjectAVLTreeSet::new);
		final Set<Integer> shaders = new ObjectAVLTreeSet<>();
		
		builderMap.forEach((type, s) -> s.match(id -> shaders.add(loadShaderObject(id, type, includeIds.getOrDefault(type, Collections.emptyList()))), shader ->
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
		return uniformCache.get().computeIfAbsent(name, (String s) -> glGetUniformLocation(program, s));
	}
	
	public void upload(String uniform, float x)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform1f(program, handle, x);
	}
	
	public void upload(String uniform, double x)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform1f(program, handle, (float)x);
	}
	
	public void upload(String uniform, int x)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform1i(program, handle, x);
	}
	
	public void uploadU(String uniform, int x)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform1ui(program, handle, x);
	}
	
	public void upload(String uniform, boolean x)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform1i(program, handle, x ? 1 : 0);
	}
	
	public void upload(String uniform, float x, float y)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform2f(program, handle, x, y);
	}
	
	public void upload(String uniform, double x, double y)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform2f(program, handle, (float)x, (float)y);
	}
	
	public void upload(String uniform, int x, int y)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform2i(program, handle, x, y);
	}
	
	public void uploadU(String uniform, int x, int y)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform2ui(program, handle, x, y);
	}
	
	public void upload(String uniform, float x, float y, float z)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform3f(program, handle, x, y, z);
	}
	
	public void upload(String uniform, double x, double y, double z)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform3f(program, handle, (float)x, (float)y, (float)z);
	}
	
	public void upload(String uniform, int x, int y, int z)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform3i(program, handle, x, y, z);
	}
	
	public void uploadU(String uniform, int x, int y, int z)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform3ui(program, handle, x, y, z);
	}
	
	public void upload(String uniform, float x, float y, float z, float w)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform4f(program, handle, x, y, z, w);
	}
	
	public void upload(String uniform, double x, double y, double z, double w)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform4f(program, handle, (float)x, (float)y, (float)z, (float)w);
	}
	
	public void upload(String uniform, int x, int y, int z, int w)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform4i(program, handle, x, y, z, w);
	}
	
	public void uploadU(String uniform, int x, int y, int z, int w)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniform4ui(program, handle, x, y, z, w);
	}
	
	public void upload(String uniform, Vector2fc vector)
	{
		upload(uniform, vector.x(), vector.y());
	}
	
	public void upload(String uniform, Vector2dc vector)
	{
		upload(uniform, vector.x(), vector.y());
	}
	
	public void upload(String uniform, Vector2ic vector)
	{
		upload(uniform, vector.x(), vector.y());
	}
	
	public void uploadU(String uniform, Vector2ic vector)
	{
		uploadU(uniform, vector.x(), vector.y());
	}
	
	public void upload(String uniform, Vector3fc vector)
	{
		upload(uniform, vector.x(), vector.y(), vector.z());
	}
	
	public void upload(String uniform, Vector3dc vector)
	{
		upload(uniform, vector.x(), vector.y(), vector.z());
	}
	
	public void upload(String uniform, Vector3ic vector)
	{
		upload(uniform, vector.x(), vector.y(), vector.z());
	}
	
	public void uploadU(String uniform, Vector3ic vector)
	{
		uploadU(uniform, vector.x(), vector.y(), vector.z());
	}
	
	public void upload(String uniform, Vector4fc vector)
	{
		upload(uniform, vector.x(), vector.y(), vector.z(), vector.w());
	}
	
	public void upload(String uniform, Vector4dc vector)
	{
		upload(uniform, vector.x(), vector.y(), vector.z(), vector.w());
	}
	
	public void upload(String uniform, Vector4ic vector)
	{
		upload(uniform, vector.x(), vector.y(), vector.z(), vector.w());
	}
	
	public void uploadU(String uniform, Vector4ic vector)
	{
		uploadU(uniform, vector.x(), vector.y(), vector.z(), vector.w());
	}
	
	public void upload(String uniform, Matrix3fc matrix)
	{
		int handle = uniformHandle(uniform);
		
		if(handle >= 0)
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				FloatBuffer matrixBuf = stack.mallocFloat(9);
				matrix.get(matrixBuf);
				glProgramUniformMatrix3fv(program, handle, false, matrixBuf);
			}
		}
	}
	
	public void upload(String uniform, Matrix3dc matrix)
	{
		int handle = uniformHandle(uniform);
		
		if(handle >= 0)
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				FloatBuffer matrixBuf = stack.mallocFloat(9);
				matrix.get(matrixBuf);
				glProgramUniformMatrix3fv(program, handle, false, matrixBuf);
			}
		}
	}
	
	public void upload(String uniform, Matrix4fc matrix)
	{
		int handle = uniformHandle(uniform);
		
		if(handle >= 0)
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				FloatBuffer matrixBuf = stack.mallocFloat(16);
				matrix.get(matrixBuf);
				glProgramUniformMatrix4fv(program, handle, false, matrixBuf);
			}
		}
	}
	
	public void upload(String uniform, Matrix4dc matrix)
	{
		int handle = uniformHandle(uniform);
		
		if(handle >= 0)
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				FloatBuffer matrixBuf = stack.mallocFloat(16);
				matrix.get(matrixBuf);
				glProgramUniformMatrix4fv(program, handle, false, matrixBuf);
			}
		}
	}
	
	public void upload(String uniform, TextureHandle<?> texture)
	{
		int handle = uniformHandle(uniform);
		if(handle >= 0) glProgramUniformHandleui64ARB(program, handle, texture.handle());
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
	
	@Override
	public void destroy()
	{
		if(!destroyed)
		{
			glDeleteProgram(program);
			
			destroyed = true;
			VoxelTest.removeDestroyable(this);
		}
	}
	
	public static int loadShaderObject(String id, ShaderType type, List<String> includeIds)
	{
		id += type.suffix();
		
		String src = AssetLoader.loadTextAsset(AssetTypes.SHADER, id);
		
		int shader = glCreateShader(type.glType());
		
		if(includeIds.isEmpty()) glShaderSource(shader, SHADER_PRE, src);
		else
		{
			StringBuilder sourceBuilder = new StringBuilder();
			includeIds.stream().map(String::toLowerCase).map(ShaderProgram::getInclude).forEach(s -> sourceBuilder.append(s + "\n"));
			
			glShaderSource(shader, SHADER_PRE, sourceBuilder.toString(), src);
		}
		
		glCompileShader(shader);
		
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
		{
			String log = glGetShaderInfoLog(shader);
			
			glDeleteShader(shader);
			
			throw new RuntimeException("Failed to load " + type.toString() + " shader \"" + id + "\"", new RuntimeException(log));
		}
		
		return shader;
	}
	
	public static int loadShaderObject(String id, ShaderType type, String... includeIds)
	{
		return loadShaderObject(id, type, List.of(includeIds));
	}
	
	private static String getInclude(String id)
	{
		return includes.computeIfAbsent(id, s -> AssetLoader.loadTextAsset(AssetTypes.SHADER, s + ".inc"));
	}
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	public static class Builder
	{
		private final EnumMap<ShaderType, Either<String, Integer>> shaders = new EnumMap<>(ShaderType.class);
		private final EnumMap<ShaderType, List<String>> includeIds = new EnumMap<>(ShaderType.class);
		
		private Builder() {}
		
		public Builder vertex(String id, String... includes)
		{
			shaders.put(ShaderType.VERTEX, Either.left(id));
			if(includes.length > 0)
				Collections.addAll(includeIds.computeIfAbsent(ShaderType.VERTEX, s -> new ObjectArrayList<>()), includes);
			return this;
		}
		
		public Builder tessControl(String id, String... includes)
		{
			shaders.put(ShaderType.TESSELATION_CONTROL, Either.left(id));
			if(includes.length > 0)
				Collections.addAll(includeIds.computeIfAbsent(ShaderType.TESSELATION_CONTROL, s -> new ObjectArrayList<>()), includes);
			return this;
		}
		
		public Builder tessEval(String id, String... includes)
		{
			shaders.put(ShaderType.TESSELATION_EVALUATION, Either.left(id));
			if(includes.length > 0)
				Collections.addAll(includeIds.computeIfAbsent(ShaderType.TESSELATION_EVALUATION, s -> new ObjectArrayList<>()), includes);
			return this;
		}
		
		public Builder geometry(String id, String... includes)
		{
			shaders.put(ShaderType.GEOMETRY, Either.left(id));
			if(includes.length > 0)
				Collections.addAll(includeIds.computeIfAbsent(ShaderType.GEOMETRY, s -> new ObjectArrayList<>()), includes);
			return this;
		}
		
		public Builder fragment(String id, String... includes)
		{
			shaders.put(ShaderType.FRAGMENT, Either.left(id));
			if(includes.length > 0)
				Collections.addAll(includeIds.computeIfAbsent(ShaderType.FRAGMENT, s -> new ObjectArrayList<>()), includes);
			return this;
		}
		
		public Builder compute(String id, String... includes)
		{
			shaders.put(ShaderType.COMPUTE, Either.left(id));
			if(includes.length > 0)
				Collections.addAll(includeIds.computeIfAbsent(ShaderType.COMPUTE, s -> new ObjectArrayList<>()), includes);
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
			return new ShaderProgram(shaders, includeIds);
		}
	}
}
