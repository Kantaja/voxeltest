package info.kuonteje.voxeltest.console;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

public class Command
{
	public static class Flags
	{
		public static final int NONE = 0x00;
		public static final int CHEAT = 0x01;
		public static final int IMMUTABLE = 0x02;
		
		private static final int ALL = CHEAT | IMMUTABLE;
	}
	
	private final Console console;
	
	private final String name;
	private final int flags;
	
	private final BiConsumer<Console, List<String>> action;
	private final BooleanSupplier predicate;
	
	Command(Console console, String name, BiConsumer<Console, List<String>> action, int flags, BooleanSupplier predicate)
	{
		this.console = console;
		
		this.name = name;
		this.flags = flags & Flags.ALL;
		
		this.action = action;
		this.predicate = predicate;
	}
	
	public String name()
	{
		return name;
	}
	
	public int flags()
	{
		return flags;
	}
	
	public boolean testFlag(int flag)
	{
		return (flags & flag) != 0;
	}
	
	public void execute(List<String> args)
	{
		if(predicate == null || predicate.getAsBoolean()) action.accept(console, args);
	}
}
