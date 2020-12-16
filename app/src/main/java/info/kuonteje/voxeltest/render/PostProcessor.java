package info.kuonteje.voxeltest.render;

import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PostProcessor
{
	private int width, height;
	
	private ForwardFramebuffer front, back = null, a, b;
	
	private List<ShaderProgram> shaders;
	
	public PostProcessor(ForwardFramebuffer front, int width, int height)
	{
		resize(front, width, height);
		
		shaders = Collections.synchronizedList(new ObjectArrayList<>());
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
