package info.kuonteje.voxeltest.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL42C.*;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.opengl.GL45C.*;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import org.lwjgl.system.MemoryUtil;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarEnum;
import info.kuonteje.voxeltest.console.CvarF64;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.console.CvarRegistry;
import info.kuonteje.voxeltest.render.light.DirectionalLight;
import info.kuonteje.voxeltest.render.light.PointLight;
import info.kuonteje.voxeltest.render.light.SunlightAndAmbient;
import info.kuonteje.voxeltest.util.DoubleFrustum;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

// TODO this is a *mess*
public class Renderer
{
	public static final double WHITE_POINT = 11.2;
	
	private static class RenderableComparator implements Comparator<Renderable>
	{
		@Override
		public int compare(Renderable a, Renderable b)
		{
			int cmp = Double.compare(a.distanceSqToCamera(), b.distanceSqToCamera());
			return cmp == 0 ? Long.compare(a.objectId(), b.objectId()) : cmp;
		}
	}
	
	private static final Comparator<Renderable> solidComparator = new RenderableComparator();
	private static final Comparator<Renderable> translucentComparator = solidComparator.reversed();
	
	private static GLCapabilities caps;
	
	public static final CvarI64 rDebug;
	
	public static final CvarF64 rFov;
	
	public static final CvarI64 rCustomAspect;
	public static final CvarF64 rAspectHor, rAspectVer;
	
	public static final CvarF64 rGamma;
	
	public static final CvarI64 rShadows;
	
	public static final CvarI64 rShadowmapSize;
	
	public static final CvarI64 rCutoutShadows;
	
	public static final CvarF64 rShadowmapSlope, rShadowmapOffset;
	
	private static DepthBuffer depthBuffer = null;
	
	private static GBuffer solidGBuffer = null;
	private static ForwardFramebuffer hdrFramebuffer = null;
	private static ForwardFramebuffer ldrFramebuffer = null;
	
	private static final ShaderProgram lumHistShader, lumCalcShader;
	private static final ShaderBuffer luminanceData;
	private static final SingleTexture finalLuminance;
	private static ForwardFramebuffer quarterFramebuffer = null;
	
	private static final ShaderProgram skyShader;
	
	private static final ShaderProgram tonemapShader;
	
	public static final CvarEnum<TonemapOperator> rTmo;
	public static final CvarF64 rExposure;
	
	private static final PostProcessor postProcessor;
	private static final ShaderProgram finalShader;
	
	private static Vector2i size = new Vector2i(0, 0);
	
	private static DebugCamera previousCamera = null;
	private static volatile DebugCamera currentCamera = null;
	
	private static final Vector3d cameraPosition = new Vector3d();
	
	private static final Matrix4d perspective = new Matrix4d();
	private static AtomicBoolean updatePerspective;
	
	private static final Matrix4d pv = new Matrix4d();
	
	private static final DoubleFrustum cullingFrustum = new DoubleFrustum();
	
	private static final ShaderProgram solidShader, translucentShader;
	private static final ShaderProgram simpleDepthShader, cutoutDepthShader;
	
	public static final CvarI64 rSsao;
	
	private static final ObjectList<Renderable> solidObjects = new ObjectArrayList<>();
	private static final ObjectList<Renderable> translucentObjects = new ObjectArrayList<>();
	
	private static final ObjectList<Renderable> allSolidObjects = new ObjectArrayList<>();
	
	private static double sunAzimuth = 115.0;
	private static double sunElevation = 16.0;
	
	private static final SunlightAndAmbient sun;
	
	private static final List<DirectionalLight> directionalLights = new ObjectArrayList<>();
	private static final List<PointLight> pointLights = new ObjectArrayList<>();
	//private static final List<Spotlight> spotights = new ObjectArrayList<>();
	
	static
	{
		caps = GL.createCapabilities(true);
		
		setupOpenGl();
		
		CvarRegistry cvars = VoxelTest.CONSOLE.cvars();
		
		rDebug = cvars.cvarBool("r_debug", false, Cvar.Flags.CONFIG | Cvar.Flags.LATCH);
		
		if(rDebug.asBool()) glEnable(GL_DEBUG_OUTPUT);
		
		rFov = cvars.cvarF64("r_fov", 85.0, Cvar.Flags.CONFIG, null, (n, o) -> dirtyPerspective());
		
		rCustomAspect = cvars.cvarBool("r_custom_aspect", false, Cvar.Flags.CONFIG, null, (n, o) -> dirtyPerspective());
		updatePerspective = new AtomicBoolean(rCustomAspect.asBool());
		
		rAspectHor = cvars.cvarF64("r_aspect_hor", 16.0, Cvar.Flags.CONFIG, null, (n, o) -> dirtyPerspective());
		rAspectVer = cvars.cvarF64("r_aspect_ver", 9.0, Cvar.Flags.CONFIG, null, (n, o) -> dirtyPerspective());
		
		rShadows = cvars.cvarBool("r_shadows", false, Cvar.Flags.CHEAT);
		
		final long maxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
		rShadowmapSize = cvars.cvarI64("r_shadowmap_size", 2048L, Cvar.Flags.CONFIG | Cvar.Flags.LATCH, v -> Long.highestOneBit(Math.min(maxTextureSize, v)));
		
		rCutoutShadows = cvars.cvarBool("r_cutout_shadows", true, Cvar.Flags.CONFIG);
		
		rShadowmapSlope = cvars.cvarF64("r_shadowmap_slope", 1.75, Cvar.Flags.CHEAT, null, (n, o) -> VoxelTest.addRenderHook(Renderer::updateDepthBias));
		rShadowmapOffset = cvars.cvarF64("r_shadowmap_offset", 1.25, Cvar.Flags.CHEAT, null, (n, o) -> VoxelTest.addRenderHook(Renderer::updateDepthBias));
		
		updateDepthBias();
		
		lumHistShader = ShaderProgram.builder().compute("luminance_histogram").create();
		lumCalcShader = ShaderProgram.builder().compute("luminance").create();
		
		finalLuminance = SingleTexture.alloc2D(2, 1, GL_R32F, 1);
		
		luminanceData = ShaderBuffer.allocEmpty(256 * Integer.BYTES, 0);
		
		skyShader = ShaderProgram.builder().vertex("sky").fragment("sky").create();
		
		tonemapShader = ForwardFramebuffer.createFbShader("hdr_final");
		tonemapShader.upload("lumSampler", finalLuminance.bindlessHandle());
		
		rTmo = cvars.cvarEnum(TonemapOperator.class, "r_tmo", TonemapOperator.UC2, Cvar.Flags.CONFIG, null, (n, o) -> VoxelTest.addRenderHook(() -> tonemapShader.upload("tmo", n.id())));
		tonemapShader.upload("tmo", rTmo.get().id());
		
		rExposure = cvars.cvarF64("r_exposure", 1.0, Cvar.Flags.CONFIG, v -> Math.max(v, 0.0), (n, o) -> VoxelTest.addRenderHook(() -> tonemapShader.upload("exposure", (float)n)));
		tonemapShader.upload("exposure", rExposure.asFloat());
		
		postProcessor = new PostProcessor(ldrFramebuffer, size.x, size.y);
		
		finalShader = ForwardFramebuffer.createFbShader("final");
		
		rGamma = cvars.cvarF64("r_gamma", 2.2, Cvar.Flags.CONFIG, v -> Math.max(v, 0.0), (n, o) -> VoxelTest.addRenderHook(() -> finalShader.upload("gamma", (float)n)));
		finalShader.upload("gamma", rGamma.asFloat());
		
		solidShader = ShaderProgram.builder().vertex("block").fragment("solid_defer", "chunk_frag_uniforms").create();
		translucentShader = ShaderProgram.builder().vertex("block").fragment("translucent", "chunk_frag_uniforms").create();
		
		simpleDepthShader = ShaderProgram.builder().vertex("depth").create();
		cutoutDepthShader = ShaderProgram.builder().vertex("depth_cutout").fragment("depth_cutout", "chunk_frag_uniforms").create();
		
		sun = new SunlightAndAmbient();
		
		rSsao = cvars.cvarBool("r_ssao", true, Cvar.Flags.CONFIG, null, (n, o) -> VoxelTest.addRenderHook(() -> tonemapShader.upload("ssao", n)));
		tonemapShader.upload("ssao", rSsao.asBool());
		
		AmbientOcclusion.init();
		
		// TODO AMD/intel alternatives
		// do intel gpus even support 4.5?
		// are debug features even useful to port?
		if(caps.GL_NVX_gpu_memory_info) VoxelTest.CONSOLE.addCommand("r_vram", (c, a) -> VoxelTest.addRenderHook(() ->
		{
			int total = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX) / 1024;
			int available = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX) / 1024;
			int current = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX) / 1024;
			
			int evictions = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTION_COUNT_NVX) / 1024;
			int totalEvicted = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX) / 1024;
			
			System.out.println((total - current) + " / " + total + " MB dedicated vram used (" + current + " MB free, " + available + " MB available)");
			System.out.println(totalEvicted + " MB evicted in " + evictions + " evictions");
		}), 0);
		
		//postProcessor.addStep(ForwardFramebuffer.createFbShader("luma"));
	}
	
	private static void updateDepthBias()
	{
		glPolygonOffset(-rShadowmapSlope.asFloat(), -rShadowmapOffset.asFloat());
	}
	
	public static void init(Window window)
	{
		resize(window.width(), window.height());
		window.setResizeCallback(Renderer::resize);
	}
	
	private static void setupOpenGl()
	{
		final String[] types = { "ERROR", "DEPRECATED_BEHAVIOR", "UNDEFINED_BEHAVIOR", "PORTABILITY", "PERFORMANCE", "OTHER", "MARKER" };
		final String[] severities = { "HIGH", "MEDIUM", "LOW" };
		
		glDebugMessageCallback((source, type, id, severity, len, message, user) ->
		{
			if(severity == GL_DEBUG_SEVERITY_NOTIFICATION || type == GL_DEBUG_TYPE_PUSH_GROUP || type == GL_DEBUG_TYPE_POP_GROUP) return;
			(type == GL_DEBUG_TYPE_ERROR ? System.err : System.out).println("[OpenGL] type: "
					+ types[type - GL_DEBUG_TYPE_ERROR] + ", severity: " + severities[severity - GL_DEBUG_SEVERITY_HIGH] + ", message: " + MemoryUtil.memUTF8(message));
		}, 0L);
		
		glClipControl(GL_LOWER_LEFT, GL_ZERO_TO_ONE);
		
		glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		glClearDepth(0.0);
		
		glDepthMask(false);
		glDepthFunc(GL_GREATER);
		
		glCullFace(GL_BACK);
		
		glDisable(GL_BLEND);
	}
	
	private static void dirtyPerspective()
	{
		updatePerspective.setRelease(true);
	}
	
	public static void beginFrame(DebugCamera camera)
	{
		currentCamera = camera;
		
		boolean up = updatePerspective.compareAndExchangeAcquire(true, false);
		
		if(up) // swap near and far for reversed depth
			perspective.setPerspective(Math.toRadians(rFov.get()), rCustomAspect.asBool() ? (double)(rAspectHor.get() / rAspectVer.get()) : (double)size.x / (double)size.y, Double.POSITIVE_INFINITY, 0.01, true);
		
		synchronized(camera)
		{
			if(up || camera != previousCamera || camera.requiresViewUpdate())
			{
				Matrix4dc view = camera.view();
				
				perspective.mul(view, pv);
				cullingFrustum.set(pv, false);
				
				solidShader.upload("pv", pv);
				translucentShader.upload("pv", pv);
				
				AmbientOcclusion.uploadMatrices(perspective, view);
			}
		}
		
		camera.getInterpPosition(cameraPosition);
		
		solidObjects.clear();
		translucentObjects.clear();
		
		allSolidObjects.clear();
	}
	
	public static void renderSolid(Renderable renderable)
	{
		if(renderable.shouldRender(cullingFrustum))
		{
			renderable.setCameraPosition(cameraPosition);
			solidObjects.add(renderable);
			allSolidObjects.add(renderable);
		}
		else if(renderable.shouldRender(null)) allSolidObjects.add(renderable);
	}
	
	public static void renderTranslucent(Renderable renderable)
	{
		if(renderable.shouldRender(cullingFrustum))
		{
			renderable.setCameraPosition(cameraPosition);
			translucentObjects.add(renderable);
		}
	}
	
	public static void completeFrame(double delta)
	{
		//sunElevation += 3.0 * delta;
		sunElevation += 0.0042 * delta;
		//sunElevation += 60.0 * delta;
		updateSunAngle();
		
		glDepthMask(true);
		
		if(!allSolidObjects.isEmpty() && rShadows.asBool()) generateShadowmaps();
		
		solidGBuffer.bind();
		
		if(!solidObjects.isEmpty())
		{
			// No point sorting this, discard defeats early fragment tests
			// maybe split out non-cutout solid blocks to run a z prepass and use the existing system only for cutouts
			//solidObjects.unstableSort(solidComparator);
			
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			renderSolids();
		}
		else glClear(GL_DEPTH_BUFFER_BIT);
		
		glDepthMask(false);
		
		// TODO light clustering
		// also batching
		
		if(!solidObjects.isEmpty())
		{
			AmbientOcclusion.generate();
			
			hdrFramebuffer.bind();
			glClear(GL_COLOR_BUFFER_BIT);
			
			ShaderProgram sunShader = sun.deferredShader();
			
			sunShader.upload("lightPv", sun.lightSpaceTransform());
			
			solidGBuffer.draw(sunShader);
			
			glEnable(GL_BLEND);
			glBlendFunc(GL_ONE, GL_ONE);
			
			if(!directionalLights.isEmpty())
			{
				ShaderProgram shader = DirectionalLight.deferredShader;
				
				for(DirectionalLight light : directionalLights)
				{
					shader.upload("lightData.direction", light.direction());
					shader.upload("lightData.color", light.color());
					shader.upload("lightData.intensity", light.intensity());
					
					SingleTexture shadowmap = light.shadowmap();
					
					if(shadowmap != null && rShadows.asBool())
					{
						shader.upload("shadows", true);
						shader.upload("shadowmapSampler", shadowmap.bindlessHandle());
					}
					else shader.upload("shadows", false);
					
					solidGBuffer.draw(shader);
				}
			}
			
			if(!pointLights.isEmpty())
			{
				ShaderProgram shader = PointLight.deferredShader;
				
				for(PointLight light : pointLights)
				{
					shader.upload("lightData.position", light.position());
					shader.upload("lightData.color", light.color());
					shader.upload("lightData.attenuation", light.attenuationData());
					
					solidGBuffer.draw(shader);
				}
			}
			
			glDisable(GL_BLEND);
		}
		else
		{
			hdrFramebuffer.bind();
			glClear(GL_COLOR_BUFFER_BIT);
		}
		
		renderSky();
		
		if(!translucentObjects.isEmpty())
		{
			translucentObjects.unstableSort(translucentComparator);
			
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			
			renderTranslucents();
			
			glDisable(GL_BLEND);
		}
		
		generateLuminance();
		
		ldrFramebuffer.bind();
		hdrFramebuffer.draw(tonemapShader, null);
		
		ForwardFramebuffer front = postProcessor.run();
		
		front.unbind();
		front.draw(finalShader, null);
		
		previousCamera = currentCamera;
		currentCamera = null;
	}
	
	private static void generateShadowmaps()
	{
		int shadowmapSize = rShadowmapSize.asInt();
		glViewport(0, 0, shadowmapSize, shadowmapSize);
		
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_POLYGON_OFFSET_FILL);
		
		glCullFace(GL_FRONT);
		
		ShaderProgram shader = rCutoutShadows.asBool() ? cutoutDepthShader : simpleDepthShader;
		
		shader.bind();
		
		sun.generateShadowmap(allSolidObjects, shader);
		
		glCullFace(GL_BACK);
		
		glDisable(GL_POLYGON_OFFSET_FILL);
		glDisable(GL_DEPTH_TEST);
		
		glViewport(0, 0, size.x, size.y);
	}
	
	private static void renderSky()
	{
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_GEQUAL);
		
		skyShader.bind();
		ForwardFramebuffer.drawFullscreenQuad();
		
		glDepthFunc(GL_GREATER);
		glDisable(GL_DEPTH_TEST);
	}
	
	private static void renderSolids()
	{
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		
		solidShader.bind();
		solidObjects.forEach(r -> r.renderFull(solidShader));
		
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
	}
	
	private static void renderTranslucents()
	{
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		
		translucentShader.bind();
		translucentObjects.forEach(r -> r.renderFull(translucentShader));
		
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
	}
	
	private static void generateLuminance()
	{
		hdrFramebuffer.blitColorTo(quarterFramebuffer, true);
		
		luminanceData.bind(0);
		
		lumHistShader.bind();
		glDispatchCompute(quarterFramebuffer.height(), 1, 1);
		
		glBindImageTexture(0, finalLuminance.handle(), 0, false, 0, GL_READ_WRITE, GL_R32F);
		
		lumCalcShader.bind();
		glDispatchCompute(1, 1, 1);
	}
	
	public static void resize(int width, int height)
	{
		if(!size.equals(width, height))
		{
			if(depthBuffer != null) depthBuffer.destroy();
			depthBuffer = new DepthBuffer(width, height, false);
			
			if(solidGBuffer != null) solidGBuffer.destroy();
			solidGBuffer = new GBuffer(depthBuffer, width, height);
			
			solidGBuffer.uploadTextureHandles(sun.deferredSunShader);
			solidGBuffer.uploadTextureHandles(sun.deferredSunlessShader);
			solidGBuffer.uploadTextureHandles(DirectionalLight.deferredShader);
			solidGBuffer.uploadTextureHandles(PointLight.deferredShader);
			
			if(hdrFramebuffer != null) hdrFramebuffer.destroy();
			hdrFramebuffer = new ForwardFramebuffer(depthBuffer, width, height, true);
			
			// it's not important that the size is a multiple of 4, this isn't displayed anywhere
			if(quarterFramebuffer != null) quarterFramebuffer.destroy();
			quarterFramebuffer = new ForwardFramebuffer(null, width / 4, height / 4, true);
			
			lumHistShader.upload("colorWidth", quarterFramebuffer.width());
			lumCalcShader.uploadU("downscaledSize", quarterFramebuffer.width(), quarterFramebuffer.height());
			
			lumHistShader.upload("color", quarterFramebuffer.colorTexture().bindlessHandle());
			lumHistShader.upload("depth", depthBuffer.texture().bindlessHandle());
			
			if(ldrFramebuffer != null) ldrFramebuffer.destroy();
			ldrFramebuffer = new ForwardFramebuffer(null, width, height, false);
			
			AmbientOcclusion.resize(solidGBuffer);
			
			tonemapShader.upload("size", 1.0F / width, 1.0F / height);
			tonemapShader.upload("aoSampler", AmbientOcclusion.generatedTexture());
			
			if(!rShadows.asBool()) glViewport(0, 0, width, height);
			
			size.set(width, height);
			
			if(!rCustomAspect.asBool()) dirtyPerspective();
			
			if(postProcessor != null) postProcessor.resize(ldrFramebuffer, width, height);
			
			System.out.println("Resized to " + width + "x" + height);
		}
	}
	
	private static void updateSunAngle()
	{
		sun.setAzEl(Math.toRadians(sunAzimuth), Math.toRadians(sunElevation));
		
		sun.deferredSunShader.upload("sunDir", sun.direction());
		//sun.forwardSunShader.upload("sunDir", sun.getDirection());
	}
	
	public static DebugCamera getCurrentCamera()
	{
		return currentCamera;
	}
	
	// TODO refactor
	public static PostProcessor getPostProcessor()
	{
		return postProcessor;
	}
	
	public static ShaderProgram getSolidShader()
	{
		return solidShader;
	}
	
	public static ShaderProgram getTranslucentShader()
	{
		return translucentShader;
	}
	
	public static ShaderProgram getTonemapShader()
	{
		return tonemapShader;
	}
	
	public static ShaderProgram getDepthShader()
	{
		return cutoutDepthShader;
	}
}
