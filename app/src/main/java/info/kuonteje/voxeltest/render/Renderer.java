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
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import org.lwjgl.system.MemoryUtil;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarF64;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.console.CvarRegistry;
import info.kuonteje.voxeltest.render.light.DirectionalLight;
import info.kuonteje.voxeltest.render.light.PointLight;
import info.kuonteje.voxeltest.render.light.Sunlight;
import info.kuonteje.voxeltest.util.MathUtil;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

// TODO this is a *mess*
public class Renderer
{
	private static class RenderableComparator implements Comparator<Renderable>
	{
		@Override
		public int compare(Renderable a, Renderable b)
		{
			int cmp = Double.compare(a.distanceSqToCamera(), b.distanceSqToCamera());
			return cmp == 0 ? Long.compare(a.getObjectId(), b.getObjectId()) : cmp;
		}
	}
	
	private static final Comparator<Renderable> solidComparator = new RenderableComparator();
	private static final Comparator<Renderable> translucentComparator = solidComparator.reversed();
	
	private static GLCapabilities caps;
	
	public static final CvarF64 rFov;
	
	public static final CvarI64 rCustomAspect;
	public static final CvarF64 rAspectHor, rAspectVer;
	
	public static final CvarF64 rGamma;
	
	public static final CvarI64 rShadowmapSize;
	private static int shadowmapSize;
	
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
	
	public static final CvarF64 rExposure;
	
	private static final PostProcessor postProcessor;
	private static final ShaderProgram finalShader;
	
	private static int width = 0;
	private static int height = 0;
	
	private static DebugCamera previousCamera = null;
	private static volatile DebugCamera currentCamera = null;
	
	private static final Vector3d cameraPosition = new Vector3d();
	
	private static final Matrix4f perspective = new Matrix4f();
	private static AtomicBoolean updatePerspective = new AtomicBoolean(false);
	
	private static final Matrix4f pv = new Matrix4f();
	
	private static final FrustumIntersection cullingFrustum = new FrustumIntersection();
	
	public static final CvarF64 rAmbientStrength, rSunIntensity;
	
	private static final ShaderProgram solidShader, translucentShader;
	private static final ShaderProgram depthShader;
	private static final ShaderProgram sunAmbientShader, directionalShader, pointShader/*, spotShader*/;
	
	public static final CvarI64 rSsao;
	
	// this has become faster to do synchronously by a decent (~10%) margin since last week and I don't know why
	private static final SortedSet<Renderable> solidObjects = new ObjectRBTreeSet<>(solidComparator);
	private static final SortedSet<Renderable> translucentObjects = new ObjectRBTreeSet<>(translucentComparator);
	
	private static final Set<Renderable> allSolidObjects = new ObjectAVLTreeSet<>(Comparator.comparingLong(Renderable::getObjectId));
	
	private static double sunAzimuth = 115.0;
	private static double sunElevation = 16.0;
	
	private static final Sunlight sun;
	
	private static final List<DirectionalLight> directionalLights = new ObjectArrayList<>();
	private static final List<PointLight> pointLights = new ObjectArrayList<>();
	//private static final List<Spotlight> spotights = new ObjectArrayList<>();
	
	static
	{
		caps = GL.createCapabilities(true);
		
		setupOpenGl();
		
		CvarRegistry cvars = VoxelTest.CONSOLE.cvars();
		
		rFov = cvars.getCvarF64C("r_fov", 85.0, Cvar.Flags.CONFIG, null, (n, o) -> dirtyPerspective());
		
		rCustomAspect = cvars.getCvarBoolC("r_custom_aspect", false, Cvar.Flags.CONFIG, (n, o) -> dirtyPerspective());
		
		rAspectHor = cvars.getCvarF64C("r_aspect_hor", 16.0, Cvar.Flags.CONFIG, null, (n, o) -> dirtyPerspective());
		rAspectVer = cvars.getCvarF64C("r_aspect_ver", 9.0, Cvar.Flags.CONFIG, null, (n, o) -> dirtyPerspective());
		
		rShadowmapSize = cvars.getCvarI64C("r_shadowmap_size", 2048L, Cvar.Flags.CONFIG, v -> Long.highestOneBit(Math.min(8192L, v)), (n, o) -> shadowmapSize = (int)n);
		shadowmapSize = rShadowmapSize.getAsInt();
		
		lumHistShader = ShaderProgram.builder().compute("luminance_histogram").create();
		lumCalcShader = ShaderProgram.builder().compute("luminance").create();
		
		finalLuminance = SingleTexture.alloc2D(2, 1, GL_R32F, 1);
		
		luminanceData = ShaderBuffer.allocEmpty(256 * Integer.BYTES, 0);
		
		skyShader = ShaderProgram.builder().vertex("sky").fragment("sky").create();
		
		tonemapShader = ForwardFramebuffer.createFbShader("hdr_final");
		tonemapShader.upload("lumSampler", finalLuminance.getBindlessHandle());
		
		rExposure = cvars.getCvarF64C("r_exposure", 1.0, Cvar.Flags.CONFIG, v -> Math.max(v, 0.0), (n, o) -> VoxelTest.addRenderHook(() -> tonemapShader.upload("exposure", (float)n)));
		tonemapShader.upload("exposure", rExposure.getAsFloat());
		
		postProcessor = new PostProcessor(ldrFramebuffer, width, height);
		
		finalShader = ForwardFramebuffer.createFbShader("final");
		
		rGamma = cvars.getCvarF64C("r_gamma", 2.2, Cvar.Flags.CONFIG, v -> Math.max(v, 0.0), (n, o) -> VoxelTest.addRenderHook(() -> finalShader.upload("gamma", (float)n)));
		finalShader.upload("gamma", rGamma.getAsFloat());
		
		solidShader = ShaderProgram.builder().vertex("block").fragment("solid_defer", "chunk_frag_uniforms").create();
		translucentShader = ShaderProgram.builder().vertex("block").fragment("translucent", "chunk_frag_uniforms").create();
		
		//depthShader = ShaderProgram.builder().vertex("depth").create();
		depthShader = ShaderProgram.builder().vertex("depth_cutout").fragment("depth_cutout", "chunk_frag_uniforms").create();
		
		sunAmbientShader = GBuffer.createShader("lighting/sun_ambient");
		
		sun = new Sunlight();
		
		rAmbientStrength = cvars.getCvarF64C("r_ambient_strength", 0.3, Cvar.Flags.CHEAT, v -> MathUtil.clamp(v, 0.0, 1.0), (n, o) -> VoxelTest.addRenderHook(() -> sunAmbientShader.upload("ambientStrength", (float)n)));
		sunAmbientShader.upload("ambientStrength", rAmbientStrength.getAsFloat());
		
		rSunIntensity = cvars.getCvarF64C("r_sun_intensity", 6.0, Cvar.Flags.CHEAT, v -> MathUtil.clamp(v, 0.0, 1.0), (n, o) -> VoxelTest.addRenderHook(() -> sunAmbientShader.upload("sunIntensity", (float)n)));
		sunAmbientShader.upload("sunIntensity", rSunIntensity.getAsFloat());
		
		directionalShader = GBuffer.createShader("lighting/directional");
		pointShader = GBuffer.createShader("lighting/point");
		
		rSsao = cvars.getCvarBoolC("r_ssao", true, Cvar.Flags.CONFIG, (n, o) -> VoxelTest.addRenderHook(() -> tonemapShader.upload("ssao", n)));
		tonemapShader.upload("ssao", rSsao.getAsBool());
		
		sunAmbientShader.upload("shadowmapSampler", sun.getShadowmap().getBindlessHandle());
		
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
	}
	
	public static void init(Window window)
	{
		resize(window.getWidth(), window.getHeight());
		window.setResizeCallback(Renderer::resize);
		
		solidGBuffer.uploadTextureHandles(sunAmbientShader);
		solidGBuffer.uploadTextureHandles(directionalShader);
		solidGBuffer.uploadTextureHandles(pointShader);
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
		
		glEnable(GL_DEBUG_OUTPUT);
		
		glClipControl(GL_LOWER_LEFT, GL_ZERO_TO_ONE);
		
		glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		glClearDepth(0.0);
		
		glDepthMask(false);
		
		glCullFace(GL_BACK);
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
			perspective.setPerspective((float)Math.toRadians(rFov.get()), rCustomAspect.getAsBool() ? (float)(rAspectHor.get() / rAspectVer.get()) : (float)width / (float)height, Float.POSITIVE_INFINITY, 0.01F, true);
		
		synchronized(camera)
		{
			if(up || camera != previousCamera || camera.requiresViewUpdate())
			{
				Matrix4f view = camera.getView();
				
				perspective.mul(view, pv);
				cullingFrustum.set(pv, false);
				
				solidShader.upload("pv", pv);
				solidShader.upload("view", view);
				
				translucentShader.upload("pv", pv);
				translucentShader.upload("view", view);
				
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
		updateSunAngle();
		
		glDepthMask(true);
		
		if(!allSolidObjects.isEmpty()) generateShadowmaps();
		
		solidGBuffer.bind();
		
		if(!solidObjects.isEmpty())
		{
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			renderSolids();
		}
		else glClear(GL_DEPTH_BUFFER_BIT);
		
		glDepthMask(false);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		// TODO light clustering
		
		if(!solidObjects.isEmpty())
		{
			AmbientOcclusion.generate();
			
			hdrFramebuffer.bind();
			glClear(GL_COLOR_BUFFER_BIT);
			
			sunAmbientShader.upload("lightPv", sun.getLightSpaceTransform());
			
			solidGBuffer.draw(sunAmbientShader);
			
			glBlendFunc(GL_ONE, GL_ONE);
			
			if(!directionalLights.isEmpty())
			{
				for(DirectionalLight light : directionalLights)
				{
					directionalShader.upload("lightData.direction", light.getDirection());
					directionalShader.upload("lightData.color", light.getColor());
					directionalShader.upload("lightData.intensity", light.getIntensity());
					
					solidGBuffer.draw(directionalShader);
				}
			}
			
			if(!pointLights.isEmpty())
			{
				for(PointLight light : pointLights)
				{
					pointShader.upload("lightData.position", light.getPosition());
					pointShader.upload("lightData.color", light.getColor());
					pointShader.upload("lightData.attenuation", light.getAttenuationData());
					
					solidGBuffer.draw(pointShader);
				}
			}
		}
		else
		{
			hdrFramebuffer.bind();
			glClear(GL_COLOR_BUFFER_BIT);
		}
		
		renderSky();
		
		if(!translucentObjects.isEmpty())
		{
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			renderTranslucents();
		}
		
		glDisable(GL_BLEND);
		
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
		glViewport(0, 0, shadowmapSize, shadowmapSize);
		glEnable(GL_DEPTH_TEST);
		
		glCullFace(GL_FRONT);
		
		depthShader.bind();
		
		sun.generateShadowmap(allSolidObjects, depthShader);
		
		glCullFace(GL_BACK);
		
		glDisable(GL_DEPTH_TEST);
		glViewport(0, 0, width, height);
	}
	
	private static void renderSky()
	{
		glDisable(GL_BLEND);
		
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_GEQUAL);
		
		skyShader.bind();
		ForwardFramebuffer.drawFullscreenQuad();
		
		glDepthFunc(GL_GREATER);
		glDisable(GL_DEPTH_TEST);
		
		glEnable(GL_BLEND);
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
		
		quarterFramebuffer.getColorTexture().bind(0);
		depthBuffer.getTexture().bind(1);
		
		luminanceData.bind(0);
		
		lumHistShader.bind();
		glDispatchCompute(quarterFramebuffer.height(), 1, 1);
		
		glBindImageTexture(0, finalLuminance.handle(), 0, false, 0, GL_READ_WRITE, GL_R32F);
		
		lumCalcShader.bind();
		glDispatchCompute(1, 1, 1);
	}
	
	public static void resize(int width, int height)
	{
		if(width != Renderer.width || height != Renderer.height)
		{
			if(depthBuffer != null) depthBuffer.destroy();
			depthBuffer = new DepthBuffer(width, height, false);
			
			if(solidGBuffer != null) solidGBuffer.destroy();
			solidGBuffer = new GBuffer(depthBuffer, width, height);
			
			if(hdrFramebuffer != null) hdrFramebuffer.destroy();
			hdrFramebuffer = new ForwardFramebuffer(depthBuffer, width, height, true);
			
			// it's not important that the size is a multiple of 4, this isn't displayed anywhere
			if(quarterFramebuffer != null) quarterFramebuffer.destroy();
			quarterFramebuffer = new ForwardFramebuffer(null, width / 4, height / 4, true);
			
			lumHistShader.upload("colorWidth", quarterFramebuffer.width());
			lumCalcShader.uploadU("downscaledSize", quarterFramebuffer.width(), quarterFramebuffer.height());
			
			if(ldrFramebuffer != null) ldrFramebuffer.destroy();
			ldrFramebuffer = new ForwardFramebuffer(null, width, height, false);
			
			AmbientOcclusion.resize(solidGBuffer);
			
			tonemapShader.upload("size", 1.0F / width, 1.0F / height);
			tonemapShader.upload("aoSampler", AmbientOcclusion.getGeneratedTexture());
			
			//glViewport(0, 0, width, height);
			
			Renderer.width = width;
			Renderer.height = height;
			
			dirtyPerspective();
			
			if(postProcessor != null) postProcessor.resize(ldrFramebuffer, width, height);
			
			System.out.println("Resized to " + width + "x" + height);
		}
	}
	
	private static void updateSunAngle()
	{
		sun.setAzEl(Math.toRadians(sunAzimuth), Math.toRadians(sunElevation));
		
		sunAmbientShader.upload("sunDir", sun.getDirection());
		//translucentShader.upload("sunDir", sun.getDirection());
	}
	
	public static DebugCamera getCurrentCamera()
	{
		return currentCamera;
	}
	
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
		return depthShader;
	}
	
	public static int getShadowmapSize()
	{
		return shadowmapSize;
	}
}
