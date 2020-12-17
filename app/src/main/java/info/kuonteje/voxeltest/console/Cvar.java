package info.kuonteje.voxeltest.console;

import info.kuonteje.voxeltest.console.CvarRegistry.SetResult;

public abstract sealed class Cvar permits CvarI64, CvarF64, CvarString, SealedGenericWorkaround
{
	public static enum Type
	{
		NONE,
		I64,
		F64,
		STRING,
		ENUM
	}
	
	public static class Flags
	{
		public static final int CHEAT = 0x01;
		public static final int CONFIG = 0x02;
		public static final int SYNC = 0x04; // no-op
		public static final int LATCH = 0x08;
		public static final int READ_ONLY = 0x40;
		
		public static final int ALL = CHEAT | CONFIG | SYNC | LATCH | READ_ONLY;
		
		public static String toString(int flags)
		{
			if((flags &= ALL) == 0) return "none";
			
			boolean first = true;
			StringBuilder builder = new StringBuilder();
			
			if((flags & CHEAT) != 0) first = appendFlag(builder, "cheat", first);
			if((flags & CONFIG) != 0) first = appendFlag(builder, "config", first);
			if((flags & SYNC) != 0) first = appendFlag(builder, "sync", first);
			if((flags & LATCH) != 0) first = appendFlag(builder, "latch", first);
			if((flags & READ_ONLY) != 0) first = appendFlag(builder, "read_only", first);
			
			return builder.toString();
		}
		
		private static boolean appendFlag(StringBuilder builder, String name, boolean first)
		{
			if(first) first = false;
			else builder.append(", ");
			
			builder.append(name);
			
			return first;
		}
	}
	
	private final CvarRegistry registry;
	
	private final String name;
	private final int flags;
	
	protected final Object lock = new Object();
	protected final Object latchLock;
	
	Cvar(CvarRegistry registry, String name, int flags)
	{
		this.registry = registry;
		
		this.name = name;
		this.flags = flags & Flags.ALL;
		
		latchLock = testFlag(Flags.LATCH) ? new Object() : null;
	}
	
	public abstract Type getType();
	
	public String name()
	{
		return name;
	}
	
	public int getFlags()
	{
		return flags;
	}
	
	public boolean testFlag(int flag)
	{
		return (flags & flag) != 0;
	}
	
	abstract SetResult setString(String value, boolean loading);
	
	public SetResult setString(String value)
	{
		return setString(value, false);
	}
	
	public abstract String asString(boolean quoteStrings);
	
	public String asString()
	{
		return asString(true);
	}
	
	public abstract String defaultValueAsString(boolean quoteStrings);
	
	public String defaultValueAsString()
	{
		return defaultValueAsString(true);
	}
	
	public abstract String latchValueAsString(boolean quoteStrings);
	
	public String latchValueAsString()
	{
		return latchValueAsString(true);
	}
	
	public abstract void reset();
	
	public CvarRegistry getRegistry()
	{
		return registry;
	}
	
	@Override
	public String toString()
	{
		String latch = latchValueAsString();
		return name + " -> " + asString() + " (default " + defaultValueAsString() + ", flags [" + Flags.toString(getFlags()) +
				"]" + (latch != null ? (", latch " + latch) : (testFlag(Cvar.Flags.LATCH) ? ", no latch value" : "")) + ")";
	}
}
