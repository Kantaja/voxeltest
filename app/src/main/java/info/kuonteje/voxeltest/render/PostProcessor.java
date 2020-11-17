package info.kuonteje.voxeltest.render;

import java.util.Collections;
import java.util.List;

import info.kuonteje.voxeltest.console.Console;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarI64;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PostProcessor
{
	public final CvarI64 rSsao;
	private volatile boolean ssao;
	
	private int width, height;
	
	private ForwardFramebuffer front, back = null, a, b;
	
	private List<ShaderProgram> shaders;
	
	private ShaderProgram ssaoShader;
	
	public PostProcessor(Console console, ForwardFramebuffer front, int width, int height)
	{
		rSsao = console.cvars().getCvarI64C("r_ssao", 1L, Cvar.Flags.CONFIG, CvarI64.BOOL_TRANSFORMER, (n, o) -> ssao = n != 0L);
		ssao = rSsao.getAsBool();
		
		resize(front, width, height);
		
		shaders = Collections.synchronizedList(new ObjectArrayList<>());
		
		ssaoShader = ForwardFramebuffer.createFbShader("ssao");
	}
	
	public void resize(ForwardFramebuffer front, int width, int height)
	{
		if(this.width != width || this.height != height)
		{
			this.front = front;
			
			if(back != null) back.destroy();
			back = new ForwardFramebuffer(null, width, height, false);
			
			this.width = width;
			this.height = height;
		}
	}
	
	public ForwardFramebuffer run()
	{
		a = front;
		b = back;
		
		if(ssao) runStep(ssaoShader);
		
		shaders.forEach(this::runStep);
		
		return a;
	}
	
	private void runStep(ShaderProgram shader)
	{
		b.bind();
		a.draw(shader, front.getDepthTexture());
		
		ForwardFramebuffer tmp = a;
		a = b;
		b = tmp;
	}
	
	public void addStep(ShaderProgram shader)
	{
		shaders.add(shader);
	}
}
