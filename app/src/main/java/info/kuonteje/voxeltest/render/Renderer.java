package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengl.GL45.*;

import java.nio.FloatBuffer;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.console.Console;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarF64;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.console.CvarRegistry;
import info.kuonteje.voxeltest.data.objects.BlockTextures;

public class Renderer
{
	public final CvarF64 rClearRed, rClearGreen, rClearBlue, rClearAlpha;
	private float clearRed, clearGreen, clearBlue, clearAlpha;
	
	public final CvarF64 rFov;
	private volatile float fov;
	
	public final CvarI64 rCustomAspect;
	public final CvarF64 rAspectHor, rAspectVer;
	
	private volatile boolean customAspect;
	private volatile float aspectHor, aspectVer;
	
	private Framebuffer framebuffer = null;
	
	private final PostProcessor postProcessor;
	
	private int width = 0;
	private int height = 0;
	
	private Camera previousCamera = null;
	private volatile Camera currentCamera = null;
	
	private final Matrix4f perspective = new Matrix4f();
	private volatile boolean updatePerspective = true;
	
	private final Matrix4f pv = new Matrix4f();
	
	private final FrustumIntersection cullingFrustum = new FrustumIntersection();
	
	private final ShaderProgram blockShader;
	
	//private final int boxVao;
	
	public Renderer(Console console, int width, int height)
	{
		setupOpenGl();
		
		CvarRegistry cvars = console.cvars();
		
		rClearRed = cvars.getCvarF64C("r_clear_red", 0.0, 0, CvarF64.ZERO_TO_ONE_TRANSFORMER, (n, o) -> VoxelTest.addRenderHook(() -> glClearColor(clearRed = (float)n, clearGreen, clearBlue, clearAlpha)));
		rClearGreen = cvars.getCvarF64C("r_clear_green", 0.0, 0, CvarF64.ZERO_TO_ONE_TRANSFORMER, (n, o) -> VoxelTest.addRenderHook(() -> glClearColor(clearRed, clearGreen = (float)n, clearBlue, clearAlpha)));
		rClearBlue = cvars.getCvarF64C("r_clear_blue", 0.0, 0, CvarF64.ZERO_TO_ONE_TRANSFORMER, (n, o) -> VoxelTest.addRenderHook(() -> glClearColor(clearRed, clearGreen, clearBlue = (float)n, clearAlpha)));
		rClearAlpha = cvars.getCvarF64C("r_clear_alpha", 1.0, 0, CvarF64.ZERO_TO_ONE_TRANSFORMER, (n, o) -> VoxelTest.addRenderHook(() -> glClearColor(clearRed, clearGreen, clearBlue, clearAlpha = (float)n)));
		
		glClearColor(clearRed = (float)rClearRed.get(), clearGreen = (float)rClearGreen.get(), clearBlue = (float)rClearBlue.get(), clearAlpha = (float)rClearAlpha.get());
		
		rFov = cvars.getCvarF64C("r_fov", 85.0, Cvar.Flags.CONFIG, null, (n, o) -> { fov = (float)n; dirtyPerspective(); });
		
		rCustomAspect = cvars.getCvarI64C("r_custom_aspect", 0, Cvar.Flags.CONFIG, CvarI64.BOOL_TRANSFORMER, (n, o) -> { customAspect = n != 0L; dirtyPerspective(); });
		
		rAspectHor = cvars.getCvarF64C("r_aspect_hor", 16.0, Cvar.Flags.CONFIG, null, (n, o) -> { aspectHor = (float)n; dirtyPerspective(); });
		rAspectVer = cvars.getCvarF64C("r_aspect_ver", 9.0, Cvar.Flags.CONFIG, null, (n, o) -> { aspectVer = (float)n; dirtyPerspective(); });
		
		fov = (float)rFov.get();
		
		customAspect = rCustomAspect.getAsBool();
		
		aspectHor = (float)rAspectHor.get();
		aspectVer = (float)rAspectVer.get();
		
		resize(width, height);
		
		postProcessor = new PostProcessor(console, framebuffer, width, height);
		
		blockShader = new ShaderProgram("block");
	}
	
	private void setupOpenGl()
	{
		final String[] types = { "ERROR", "DEPRECATED_BEHAVIOR", "UNDEFINED_BEHAVIOR", "PORTABILITY", "PERFORMANCE", "OTHER", "MARKER" };
		final String[] severities = { "HIGH", "MEDIUM", "LOW" };
		
		glDebugMessageCallback((source, type, id, severity, len, message, user) ->
		{
			if(severity == GL_DEBUG_SEVERITY_NOTIFICATION || type == GL_DEBUG_TYPE_PUSH_GROUP || type == GL_DEBUG_TYPE_POP_GROUP) return;
			(type == GL_DEBUG_TYPE_ERROR ? System.err : System.out).println("[OpenGL] type: "
					+ types[type - GL_DEBUG_TYPE_ERROR] + ", severity: " + severities[severity - GL_DEBUG_SEVERITY_HIGH] + ", message: " + MemoryUtil.memUTF8(message));
		}, 0L);
		
		glEnable(GL_DEBUG_OUTPUT);
		
		glClipControl(GL_LOWER_LEFT, GL_ZERO_TO_ONE);
		
		glClearDepth(0.0);
		glDepthFunc(GL_GREATER);
		
		glCullFace(GL_BACK);
		
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void dirtyPerspective()
	{
		synchronized(perspective)
		{
			updatePerspective = true;
		}
	}
	
	private void uploadMatrix(int handle, Matrix4f matrix)
	{
		if(handle >= 0)
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				FloatBuffer matrixBuf = stack.mallocFloat(16);
				matrix.get(matrixBuf);
				glUniformMatrix4fv(handle, false, matrixBuf);
			}
		}
	}
	
	//private static final Matrix4f model = new Matrix4f();
	
	public void beginFrame(Camera camera)
	{
		currentCamera = camera;
		
		if(updatePerspective)
		{
			synchronized(perspective)
			{
				// swap near and far for reversed depth
				if(updatePerspective)
					perspective.setPerspective((float)Math.toRadians(fov), customAspect ? aspectHor / aspectVer : (float)width / (float)height, Float.POSITIVE_INFINITY, 0.01F, true);
			}
		}
		
		synchronized(camera)
		{
			if(updatePerspective || camera != previousCamera || camera.requiresViewUpdate())
			{
				perspective.mul(camera.getView(), pv);
				cullingFrustum.set(pv, false);
			}
		}
		
		updatePerspective = false;
		
		framebuffer.bind();
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		
		BlockTextures.getArray().bind(ChunkShaderBindings.TEX_SAMPLERS);
		blockShader.bind();
		
		uploadMatrix(ChunkShaderBindings.PV_MATRIX, pv);
		//uploadMatrix(BlockShaderBindings.MODEL_MATRIX, model);
	}
	
	public void render(IRenderable renderable)
	{
		if(renderable.shouldRender(cullingFrustum)) renderable.render();
	}
	
	public void endFrame()
	{
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		
		Framebuffer front = postProcessor.run();
		
		front.unbind();
		front.draw();
		
		previousCamera = currentCamera;
		currentCamera = null;
	}
	
	public void resize(int width, int height)
	{
		if(width != this.width || height != this.height)
		{
			if(framebuffer != null) framebuffer.destroy();
			framebuffer = new Framebuffer(width, height);
			
			glViewport(0, 0, width, height);
			
			this.width = width;
			this.height = height;
			
			dirtyPerspective();
			
			if(postProcessor != null) postProcessor.resize(framebuffer, width, height);
			
			System.out.println("Resized to " + width + "x" + height);
		}
	}
	
	public Camera getCurrentCamera()
	{
		return currentCamera;
	}
	
	public PostProcessor getPostProcessor()
	{
		return postProcessor;
	}
}
