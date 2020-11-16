package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.opengl.GL45C.*;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Phaser;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
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
	// THIS INTENTIONALLY VIOLATES THE CONTRACT OF COMPARATOR
	// If compare(a, b) returned 0 for a != b, only one object with the same distance from the camera would be rendered
	// in the case that two equal-comparing but different objects are compared again there is no issue with switching the order, this is only used for maintaining a skip list
	private static class RenderableComparator implements Comparator<IRenderable>
	{
		@Override
		public int compare(IRenderable a, IRenderable b)
		{
			int cmp = Double.compare(a.distanceSqToCamera(), b.distanceSqToCamera());
			
			if(cmp == 0) return a == b ? 0 : -1;
			else return cmp;
		}
	}
	
	private static final Comparator<IRenderable> solidComparator = new RenderableComparator();
	private static final Comparator<IRenderable> translucentComparator = solidComparator.reversed();
	
	private final GLCapabilities caps;
	
	public final CvarF64 rClearRed, rClearGreen, rClearBlue, rClearAlpha;
	private float clearRed, clearGreen, clearBlue, clearAlpha;
	
	public final CvarF64 rFov;
	private volatile float fov;
	
	public final CvarI64 rCustomAspect;
	public final CvarF64 rAspectHor, rAspectVer;
	
	private volatile boolean customAspect;
	private volatile float aspectHor, aspectVer;
	
	private Texture depthTexture = null;
	
	private GBuffer solidGBuffer = null;
	private ForwardFramebuffer framebuffer = null;
	
	private final PostProcessor postProcessor;
	
	private int width = 0;
	private int height = 0;
	
	private Camera previousCamera = null;
	private volatile Camera currentCamera = null;
	
	private final Vector3d cameraPosition = new Vector3d();
	
	private final Matrix4f perspective = new Matrix4f();
	private volatile boolean updatePerspective = true;
	
	private final Matrix4f pv = new Matrix4f();
	
	private final FrustumIntersection cullingFrustum = new FrustumIntersection();
	
	private final ShaderProgram solidShader, translucentShader;
	
	private final Phaser solidPhaser = new Phaser(1);
	private final Phaser translucentPhaser = new Phaser(1);
	
	private final NavigableSet<IRenderable> solidObjects = new ConcurrentSkipListSet<>(solidComparator);
	private final NavigableSet<IRenderable> translucentObjects = new ConcurrentSkipListSet<>(translucentComparator);
	
	private double sunAzimuth = 115.0;
	private double sunElevation = 16.0;
	
	private final Vector3f sunDir = new Vector3f();
	
	public Renderer(Console console, Window window, int width, int height)
	{
		caps = GL.createCapabilities();
		
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
		
		window.setResizeCallback(this::resize);
		
		postProcessor = new PostProcessor(console, framebuffer, width, height);
		
		solidShader = ShaderProgram.builder().vertex("block").geometry("normals").fragment("solid_defer", "chunk_frag_uniforms").create();
		translucentShader = ShaderProgram.builder().vertex("block").geometry("normals").fragment("translucent", "chunk_frag_uniforms").create();
		
		// TODO AMD/intel alternatives
		// do intel gpus even support 4.5?
		// are debug features even useful to port?
		if(caps.GL_NVX_gpu_memory_info) console.addCommand("r_vram", (c, a) -> VoxelTest.addRenderHook(() ->
		{
			int total = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX) / 1024;
			int available = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX) / 1024;
			int current = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX) / 1024;
			
			int evictions = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTION_COUNT_NVX) / 1024;
			int totalEvicted = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX) / 1024;
			
			System.out.println((total - current) + " / " + total + " MB dedicated vram used (" + current + " MB free, " + available + " MB available)");
			System.out.println(totalEvicted + " MB evicted in " + evictions + " evictions");
		}), 0);
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
		glEnable(GL_BLEND);
	}
	
	private void dirtyPerspective()
	{
		synchronized(perspective)
		{
			updatePerspective = true;
		}
	}
	
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
				
				solidShader.upload("pv", pv);
				translucentShader.upload("pv", pv);
			}
		}
		
		updatePerspective = false;
		
		camera.getInterpPosition(cameraPosition);
		
		sunElevation += 0.01;
		updateSunAngle();
		
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		
		solidGBuffer.bind();
		solidShader.bind();
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		BlockTextures.getArray().bind(BlockTextures.ARRAY_TEXTURE_UNIT);
	}
	
	// adding to the skip lists shows up as the biggest cpu time sink, but it's cheaper than adding to an array list then sorting it
	public void renderSolid(IRenderable renderable)
	{
		solidPhaser.register();
		
		VoxelTest.getThreadPool().execute(() ->
		{
			if(renderable.shouldRender(cullingFrustum))
			{
				renderable.setCameraPosition(cameraPosition);
				solidObjects.add(renderable);
			}
			
			solidPhaser.arriveAndDeregister();
		});
	}
	
	public void renderTranslucent(IRenderable renderable)
	{
		translucentPhaser.register();
		
		VoxelTest.getThreadPool().execute(() ->
		{
			if(renderable.shouldRender(cullingFrustum))
			{
				renderable.setCameraPosition(cameraPosition);
				translucentObjects.add(renderable);
			}
			
			translucentPhaser.arriveAndDeregister();
		});
	}
	
	public void completeFrame()
	{
		IRenderable renderable;
		
		solidPhaser.arriveAndAwaitAdvance();
		
		// Polling allocates a SimpleImmutableEntry for EVERY object
		// reimplement the entire container?
		while((renderable = solidObjects.pollFirst()) != null)
		{
			renderable.render();
		}
		
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		
		framebuffer.bind();
		
		glClear(GL_COLOR_BUFFER_BIT);
		
		solidGBuffer.draw();
		
		translucentShader.bind();
		
		BlockTextures.getArray().bind(BlockTextures.ARRAY_TEXTURE_UNIT);
		
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		
		translucentPhaser.arriveAndAwaitAdvance();
		
		while((renderable = translucentObjects.pollFirst()) != null)
		{
			renderable.render();
		}
		
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		
		ForwardFramebuffer front = postProcessor.run();
		
		front.unbind();
		front.draw();
		
		previousCamera = currentCamera;
		currentCamera = null;
	}
	
	public void resize(int width, int height)
	{
		if(width != this.width || height != this.height)
		{
			if(depthTexture != null) depthTexture.destroy();
			depthTexture = createDepthTexture(width, height);
			
			if(solidGBuffer != null) solidGBuffer.destroy();
			solidGBuffer = new GBuffer(depthTexture, width, height);
			
			if(framebuffer != null) framebuffer.destroy();
			framebuffer = new ForwardFramebuffer(depthTexture, width, height);
			
			glViewport(0, 0, width, height);
			
			this.width = width;
			this.height = height;
			
			dirtyPerspective();
			
			if(postProcessor != null) postProcessor.resize(framebuffer, width, height);
			
			System.out.println("Resized to " + width + "x" + height);
		}
	}
	
	private Texture createDepthTexture(int width, int height)
	{
		int texture = glCreateTextures(GL_TEXTURE_2D);
		glTextureStorage2D(texture, 1, GL_DEPTH_COMPONENT32F, width, height);
		
		return Texture.wrap(width, height, texture);
	}
	
	private void updateSunAngle()
	{
		double az = Math.toRadians(sunAzimuth);
		double el = Math.toRadians(sunElevation);
		
		double sinAz = Math.sin(az);
		double cosAz = Math.cos(az);
		
		double sinEl = Math.sin(el);
		double cosEl = Math.cos(el);
		
		sunDir.set(sinAz * cosEl, sinEl, -cosAz * cosEl).normalize();
		
		GBuffer.getDefaultShader().upload("sunDir", sunDir);
		//translucentShader.upload("sunDir", sunDir);
	}
	
	public Camera getCurrentCamera()
	{
		return currentCamera;
	}
	
	public PostProcessor getPostProcessor()
	{
		return postProcessor;
	}
	
	public ShaderProgram getSolidShader()
	{
		return solidShader;
	}
	
	public ShaderProgram getTranslucentShader()
	{
		return translucentShader;
	}
}
