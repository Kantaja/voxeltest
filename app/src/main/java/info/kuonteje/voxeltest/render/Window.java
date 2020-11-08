package info.kuonteje.voxeltest.render;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joml.Vector2d;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class Window
{
	public static interface IWindowResizeCallback
	{
		void resize(int width, int height);
	}
	
	public static interface IKeyCallback
	{
		void key(int key, boolean pressed, boolean ctrl, boolean alt, boolean shift);
	}
	
	public static interface IMouseButtonCallback
	{
		void mouseButton(int button, boolean pressed, boolean ctrl, boolean alt, boolean shift);
	}
	
	private IWindowResizeCallback resizeCallback = null;
	
	private CopyOnWriteArrayList<IKeyCallback> keyCallbacks = new CopyOnWriteArrayList<>();
	private CopyOnWriteArrayList<IMouseButtonCallback> mouseButtonCallbacks = new CopyOnWriteArrayList<>();
	
	private final Object mouseLock = new Object();
	
	// avoid overhead of MemoryStack every frame
	private final DoubleBuffer bufMouseX = MemoryUtil.memAllocDouble(1);
	private final DoubleBuffer bufMouseY = MemoryUtil.memAllocDouble(1);
	
	private int nwidth, nheight;
	private int xpos, ypos, width, height;
	
	private boolean requiresResize = true;
	
	private int swapInterval = 0;
	private boolean requiresSwapIntervalUpdate = true;
	
	private boolean fullscreen = false;
	private boolean mouseCaptured = false;
	
	private final AtomicBoolean shouldClose = new AtomicBoolean(false);
	
	private final long window;
	
	public Window(String title, int width, int height)
	{
		glfwDefaultWindowHints();
		
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
		
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);
		
		if((window = glfwCreateWindow(width, height, title, NULL, NULL)) == 0) throw new RuntimeException("failed to create window");
		
		IntBuffer bwidth = BufferUtils.createIntBuffer(1);
		IntBuffer bheight = BufferUtils.createIntBuffer(1);
		
		glfwGetWindowSize(window, bwidth, bheight);
		
		nwidth = bwidth.get(0);
		nheight = bheight.get(0);
		
		glfwGetCursorPos(window, bufMouseX, bufMouseY);
		
		glfwSetWindowSizeCallback(window, (_window, _width, _height) ->
		{
			nwidth = _width;
			nheight = _height;
			requiresResize = true;
		});
		
		glfwSetKeyCallback(window, (_window, _key, _scancode, _action, _mods) ->
		{
			if(_action != GLFW_REPEAT)
			{
				boolean pressed = _action == GLFW_PRESS;
				boolean ctrl = (_mods & GLFW_MOD_CONTROL) != 0;
				boolean alt = (_mods & GLFW_MOD_ALT) != 0;
				boolean shift = (_mods & GLFW_MOD_SHIFT) != 0;
				
				if(pressed && _key == GLFW_KEY_F8) System.gc();
				
				for(int i = 0; i < keyCallbacks.size(); i++)
				{
					keyCallbacks.get(i).key(_key, pressed, ctrl, alt, shift);
				}
			}
		});
		
		GLFW.glfwSetMouseButtonCallback(window, (_window, _button, _action, _mods) ->
		{
			if(_action != GLFW_REPEAT)
			{
				boolean pressed = _action == GLFW_PRESS;
				boolean ctrl = (_mods & GLFW_MOD_CONTROL) != 0;
				boolean alt = (_mods & GLFW_MOD_ALT) != 0;
				boolean shift = (_mods & GLFW_MOD_SHIFT) != 0;
				
				for(int i = 0; i < mouseButtonCallbacks.size(); i++)
				{
					mouseButtonCallbacks.get(i).mouseButton(_button, pressed, ctrl, alt, shift);
				}
			}
		});
		
		glfwMakeContextCurrent(window);
	}
	
	public void requestClose()
	{
		shouldClose.setRelease(true);
	}
	
	public boolean shouldClose()
	{
		return shouldClose.getAcquire();
	}
	
	public void update()
	{
		glfwSwapBuffers(window);
		glfwPollEvents();
		
		if(requiresResize)
		{
			requiresResize = false;
			if(resizeCallback != null) resizeCallback.resize(nwidth, nheight);
		}
		
		if(requiresSwapIntervalUpdate)
		{
			requiresSwapIntervalUpdate = false;
			glfwSwapInterval(swapInterval);
		}
		
		if(glfwWindowShouldClose(window)) shouldClose.setRelease(true);
		
		if(mouseCaptured)
		{
			synchronized(mouseLock)
			{
				glfwGetCursorPos(window, bufMouseX, bufMouseY);
			}
		}
	}
	
	public boolean isKeyDown(int key)
	{
		return glfwGetKey(window, key) == GLFW_PRESS;
	}
	
	public void setResizeCallback(IWindowResizeCallback callback)
	{
		resizeCallback = callback;
	}
	
	public void addKeyCallback(IKeyCallback callback)
	{
		keyCallbacks.add(callback);
	}
	
	public void addMouseButtonCallback(IMouseButtonCallback callback)
	{
		mouseButtonCallbacks.add(callback);
	}
	
	public void setTitle(String title)
	{
		glfwSetWindowTitle(window, title);
	}
	
	public void toggleFullscreen()
	{
		if(fullscreen)
		{
			glfwSetWindowMonitor(window, 0, xpos, ypos, width, height, GLFW_DONT_CARE);
			fullscreen = false;
		}
		else
		{
			long monitor = glfwGetPrimaryMonitor();
			
			if(monitor != 0)
			{
				GLFWVidMode mode = glfwGetVideoMode(monitor);
				
				try(MemoryStack stack = MemoryStack.stackPush())
				{
					IntBuffer bufWindowXPos = stack.mallocInt(1);
					IntBuffer bufWindowYPos = stack.mallocInt(1);
					
					glfwGetWindowPos(window, bufWindowXPos, bufWindowYPos);
					
					xpos = bufWindowXPos.get(0);
					ypos = bufWindowYPos.get(0);
				}
				
				width = nwidth;
				height = nheight;
				
				glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
				
				fullscreen = true;
			}
		}
		
		requiresSwapIntervalUpdate = true;
	}
	
	public void setSwapInterval(int swapInterval)
	{
		this.swapInterval = swapInterval;
		requiresSwapIntervalUpdate = true;
	}
	
	public void captureMouse()
	{
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		mouseCaptured = true;
	}
	
	public void releaseMouse()
	{
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		mouseCaptured = false;
	}
	
	public boolean getKey(int key)
	{
		return glfwGetKey(window, key) == GLFW_PRESS;
	}
	
	public Vector2d getMouse(Vector2d dest)
	{
		synchronized(mouseLock)
		{
			return dest.set(bufMouseX.get(0), bufMouseY.get(0));
		}
	}
	
	public long handle()
	{
		return window;
	}
	
	public void destroy()
	{
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		memFree(bufMouseX);
		memFree(bufMouseY);
	}
}
