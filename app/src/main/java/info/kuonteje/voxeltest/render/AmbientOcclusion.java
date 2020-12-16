package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.*;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.opengl.GL45C.*;

import java.nio.FloatBuffer;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarF64;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.console.CvarRegistry;
import info.kuonteje.voxeltest.util.MathUtil;

public class AmbientOcclusion
{
	public static final ShaderProgram generateShader;
	
	public static final CvarI64 rSsaoSamples, rSsaoNoiseSize;
	public static final CvarF64 rSsaoRadius, rSsaoStrength;
	
	private static final SingleTexture aoNoise, aoSamples;
	
	private static ForwardFramebuffer generate = null;
	
	static
	{
		generateShader = GBuffer.createShader("ssao_generate");
		
		CvarRegistry cvars = VoxelTest.CONSOLE.cvars();
		
		rSsaoNoiseSize = cvars.getCvarI64("r_ssao_noise_size", 4L, Cvar.Flags.CONFIG | Cvar.Flags.LATCH, v -> Long.highestOneBit(MathUtil.clamp(v, 2L, 32L)));
		
		int noiseSize = rSsaoNoiseSize.getAsInt();
		Renderer.getTonemapShader().upload("halfNoiseSize", noiseSize / 2);
		
		aoNoise = SingleTexture.alloc2D(noiseSize, noiseSize, GL_RGBA16F, 1);
		
		glTextureParameteri(aoNoise.handle(), GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTextureParameteri(aoNoise.handle(), GL_TEXTURE_WRAP_T, GL_REPEAT);
		
		// hangs if GL_DEBUG_OUTPUT is enabled
		glDisable(GL_DEBUG_OUTPUT);
		aoSamples = generateAoData();
		glEnable(GL_DEBUG_OUTPUT);
		
		generateShader.upload("noiseSampler", aoNoise.getBindlessHandle());
		generateShader.upload("samples", aoSamples.getBindlessHandle());
		
		rSsaoSamples = cvars.getCvarI64C("r_ssao_samples", 32L, Cvar.Flags.CONFIG, v -> MathUtil.clamp(v, 8L, 64L), (n, o) -> VoxelTest.addRenderHook(() -> generateShader.uploadU("aoSamples", (int)n)));
		generateShader.uploadU("aoSamples", rSsaoSamples.getAsInt());
		
		rSsaoRadius = cvars.getCvarF64C("r_ssao_radius", 1.5, Cvar.Flags.CONFIG, v -> Math.max(v, 0.01), (n, o) -> VoxelTest.addRenderHook(() -> generateShader.upload("aoRadius", (float)n)));
		generateShader.upload("aoRadius", rSsaoRadius.getAsFloat());
		
		rSsaoStrength = cvars.getCvarF64C("r_ssao_strength", 1.2, Cvar.Flags.CONFIG, v -> MathUtil.clamp(v, 0.01, 4.0), (n, o) -> VoxelTest.addRenderHook(() -> generateShader.upload("aoStrength", (float)n)));
		generateShader.upload("aoStrength", rSsaoStrength.getAsFloat());
	}
	
	public static void init()
	{
		// static init
	}
	
	private static SingleTexture generateAoData()
	{
		Random random = new Random();
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			int noiseSize = rSsaoNoiseSize.getAsInt();
			
			FloatBuffer noise = stack.callocFloat(noiseSize * noiseSize * 3);
			
			for(int i = 0; i < noiseSize * noiseSize; i++)
			{
				new Vector2f(random.nextFloat() * 2.0F - 1.0F, random.nextFloat() * 2.0F - 1.0F).get(i * 3, noise);
			}
			
			glTextureSubImage2D(aoNoise.handle(), 0, 0, 0, noiseSize, noiseSize, GL_RGB, GL_FLOAT, noise);
		}
		
		int sampleBuffer = glCreateBuffers();
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			FloatBuffer samples = stack.mallocFloat(64 * 4);
			
			for(int i = 0; i < 64; i++)
			{
				Vector3f sample = new Vector3f(random.nextFloat() * 2.0F - 1.0F, random.nextFloat() * 2.0F - 1.0F, random.nextFloat()).normalize();
				sample.mul(random.nextFloat() * MathUtil.lerp(0.01F, 1.0F, i / 64.0F));
				
				sample.get(i * 4, samples);
				samples.put(i * 4 + 3, 0.0F);
			}
			
			glNamedBufferStorage(sampleBuffer, samples, 0);
		}
		
		SingleTexture sampleTexture = SingleTexture.wrap(0, 0, 0, glCreateTextures(GL_TEXTURE_BUFFER));
		glTextureBuffer(sampleTexture.handle(), GL_RGBA32F, sampleBuffer);
		
		VoxelTest.addDestroyable(() ->
		{
			sampleTexture.destroy();
			glDeleteBuffers(sampleBuffer);
		});
		
		return sampleTexture;
	}
	
	static void resize(GBuffer gBuffer)
	{
		int width = gBuffer.width();
		int height = gBuffer.height();
		
		float noiseSize = rSsaoNoiseSize.get();
		
		generateShader.upload("noiseScale", width / noiseSize, height / noiseSize);
		
		gBuffer.uploadTextureHandles(generateShader);
		
		if(generate != null) generate.destroy();
		generate = new ForwardFramebuffer(null, width, height, true);
	}
	
	static void uploadMatrices(Matrix4f projection, Matrix4f view)
	{
		generateShader.upload("projection", projection);
		generateShader.upload("view", view);
	}
	
	public static TextureHandle<SingleTexture> getGeneratedTexture()
	{
		return generate.getColorTexture().getBindlessHandle();
	}
	
	static void generate()
	{
		if(Renderer.rSsao.getAsBool())
		{
			generate.bind();
			generateShader.bind();
			
			ForwardFramebuffer.drawFullscreenQuad();
		}
	}
}
