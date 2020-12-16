package info.kuonteje.voxeltest.render;

import static info.kuonteje.voxeltest.ConstantConfig.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector3d;

import info.kuonteje.voxeltest.Ticks;
import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarF64;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.util.MathUtil;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;

public class DebugCamera implements Ticks.ITickHandler
{
	private static final double RAD90 = Math.PI / 2.0;
	
	private static CvarF64 moveSpeed = VoxelTest.CONSOLE.cvars().getCvarF64("move_speed", 12.0, Cvar.Flags.CHEAT, null);
	private static CvarF64 walkMult = VoxelTest.CONSOLE.cvars().getCvarF64("walk_mult", 0.3, Cvar.Flags.CHEAT, null);
	private static CvarF64 sprintMult = VoxelTest.CONSOLE.cvars().getCvarF64("sprint_mult", 3.0, Cvar.Flags.CHEAT, null);
	
	private static CvarI64 mInvertY = VoxelTest.CONSOLE.cvars().getCvarBool("m_invert_y", false, Cvar.Flags.CONFIG);
	
	private static CvarF64 mYaw, mPitch, sensitivity;
	private static double yaw, pitch;
	
	static
	{
		mYaw = VoxelTest.CONSOLE.cvars().getCvarF64C("m_yaw", 0.022, Cvar.Flags.CONFIG, null, (n, o) -> recalc());
		mPitch = VoxelTest.CONSOLE.cvars().getCvarF64C("m_pitch", 0.022, Cvar.Flags.CONFIG, null, (n, o) -> recalc());
		sensitivity = VoxelTest.CONSOLE.cvars().getCvarF64C("sensitivity", 2.25, Cvar.Flags.CONFIG, null, (n, o) -> recalc());
		
		recalc();
	}
	
	private static void recalc()
	{
		double s = sensitivity.get();
		
		yaw = Math.toRadians(s * mYaw.get());
		pitch = Math.toRadians(s * mPitch.get());
	}
	
	private final Int2BooleanFunction keyFunc;
	private final Function<Vector2d, Vector2d> mouseFunc;
	
	private final Vector2d prevMouse = new Vector2d();
	
	private final Vector3d position = new Vector3d(0.0, 96.0, 0.0);
	private final Vector3d rotation = new Vector3d();
	
	private final Matrix4f view = new Matrix4f();
	
	private boolean requiresViewUpdate = true;
	
	private Vector3d interpPosition = new Vector3d();
	
	private Vector3d prevPosition = new Vector3d();
	private Vector3d prevRotation = new Vector3d();
	
	private Vector3d prevTickPosition = new Vector3d();
	
	private AtomicInteger ticksSinceMove = new AtomicInteger(0);
	
	private final Object transformLock = new Object();
	
	public DebugCamera(Int2BooleanFunction keyFunc, Function<Vector2d, Vector2d> mouseFunc)
	{
		this.keyFunc = keyFunc;
		this.mouseFunc = mouseFunc;
		
		prevTickPosition.set(position);
		
		Ticks.addTickHandler(this);
	}
	
	public void frame(double delta)
	{
		Vector2d mouse = mouseFunc.apply(new Vector2d());
		
		double newRotX = yaw * (prevMouse.x - mouse.x);
		double newRotY = pitch * (prevMouse.y - mouse.y) * (mInvertY.getAsBool() ? -1.0 : 1.0);
		
		if(ticksSinceMove.get() <= 1 || newRotY != 0.0 || newRotX != 0.0)
		{
			synchronized(transformLock)
			{
				rotation.y += newRotX;
				rotation.x += newRotY;
				
				if(rotation.x > RAD90) rotation.x = RAD90;
				else if(rotation.x < -RAD90) rotation.x = -RAD90;
				
				prevMouse.set(mouse);
				
				MathUtil.lerp(prevTickPosition, position, VoxelTest.getPartialTick(), interpPosition);
				
				view.identity().rotateX((float)(-rotation.x)).rotateY((float)(-rotation.y)).translate((float)(-interpPosition.x), (float)(-interpPosition.y), (float)(-interpPosition.z));
				
				requiresViewUpdate = true;
				
				prevRotation.set(rotation);
				prevPosition.set(position);
			}
		}
		else requiresViewUpdate = false;
	}
	
	@Override
	public void tick(double delta)
	{
		double move = moveSpeed.get() * delta;
		
		double offx = 0.0, offy = 0.0, offz = 0.0;
		
		boolean walk = keyFunc.test(CFG_KEY_WALK);
		boolean sprint = keyFunc.test(CFG_KEY_SPRINT);
		
		if(walk && !sprint) move *= walkMult.get();
		else if(sprint && !walk) move *= sprintMult.get();;
		
		if(keyFunc.test(CFG_KEY_FORWARD)) offz -= move;
		if(keyFunc.test(CFG_KEY_BACK)) offz += move;
		if(keyFunc.test(CFG_KEY_LEFT)) offx -= move;
		if(keyFunc.test(CFG_KEY_RIGHT)) offx += move;
		
		if(keyFunc.test(CFG_KEY_UP)) offy += move;
		if(keyFunc.test(CFG_KEY_DOWN)) offy -= move;
		
		double dx = 0.0, dz = 0.0;
		
		synchronized(transformLock)
		{
			if(offz != 0.0)
			{
				dx += offz * Math.sin(rotation.y);
				dz += offz * Math.cos(rotation.y);
			}
			
			if(offx != 0.0)
			{
				dx += offx * Math.sin(rotation.y + RAD90);
				dz += offx * Math.cos(rotation.y + RAD90);
			}
			
			prevTickPosition.set(position);
			
			if(dx != 0.0 || offy != 0.0 || dz != 0.0)
			{
				position.add(dx, offy, dz);
				ticksSinceMove.set(0);
			}
			else ticksSinceMove.getAndIncrement();
		}
	}
	
	public boolean requiresViewUpdate()
	{
		return requiresViewUpdate;
	}
	
	public Matrix4f getView()
	{
		return view;
	}
	
	public Vector3d getPosition(Vector3d dest)
	{
		synchronized(transformLock)
		{
			return dest.set(position);
		}
	}
	
	public Vector3d getInterpPosition(Vector3d dest)
	{
		synchronized(transformLock)
		{
			return dest.set(interpPosition);
		}
	}
	
	public Vector3d getRotation(Vector3d dest)
	{
		synchronized(transformLock)
		{
			return dest.set(position);
		}
	}
	
	@Override
	public String name()
	{
		return "camera";
	}
}
