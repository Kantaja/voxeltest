package info.kuonteje.voxeltest.console;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Console
{
	public static enum CommandResult
	{
		INVALID_I64,
		INVALID_F64,
		INVALID_STRING,
		NOT_FOUND,
		CHEATS_REQUIRED,
		EMPTY_LINE,
		COMMAND_EXECUTED,
		CVAR_PRINTED,
		CVAR_SET,
		CVAR_READ_ONLY
	}
	
	private final Path configPath;
	
	private final CvarRegistry cvars = new CvarRegistry(this);
	
	private final CvarI64 svCheats = cvars.getCvarI64C("sv_cheats", 0, Cvar.Flags.SYNC, CvarI64.BOOL_TRANSFORMER, (n, o) ->
	{
		if(n != o && n == 0L) cvars.resetCheats();
	});
	
	private final Object2ObjectMap<String, Command> commands = new Object2ObjectOpenHashMap<>();
	
	public Console(Path configPath)
	{
		this.configPath = configPath;
		
		addCommand("saveconfig", (c, a) -> save(), Command.Flags.IMMUTABLE);
	}
	
	public Path getConfigPath()
	{
		return configPath;
	}
	
	public boolean cheatsEnabled()
	{
		return svCheats.getAsBool();
	}
	
	public CvarRegistry cvars()
	{
		return cvars;
	}
	
	public boolean addCommand(String name, BiConsumer<Console, List<String>> action, int flags, BooleanSupplier predicate)
	{
		name = name.trim().toLowerCase();
		
		Command existing = commands.get(name);
		if(existing != null && existing.testFlag(Command.Flags.IMMUTABLE)) return false;
		
		commands.put(name, new Command(this, name, action, flags, predicate));
		
		return true;
	}
	
	public boolean addCommand(String name, BiConsumer<Console, List<String>> action, int flags)
	{
		return addCommand(name, action, flags, null);
	}
	
	private static final String[] CVAR_ERROR_STRINGS = { "Command or cvar '%1$s' not found", "Invalid i64 '%2$s'", "Invalid f64 '%2$s'", "Invalid string '%2$s'???", "sv_cheats must not be 0", "Cvar '%1$s' is read-only" };
	private static final CommandResult[] CVAR_ERROR_RESULTS = { CommandResult.NOT_FOUND, CommandResult.INVALID_I64, CommandResult.INVALID_F64, CommandResult.INVALID_STRING, CommandResult.CHEATS_REQUIRED, CommandResult.CVAR_READ_ONLY };
	
	public CommandResult execute(List<String> line)
	{
		if(line.isEmpty()) return CommandResult.EMPTY_LINE;
		
		String commandName = line.get(0).toLowerCase();
		Command command = commands.get(commandName);
		
		if(command != null)
		{
			if(command.testFlag(Command.Flags.CHEAT) && !cheatsEnabled())
			{
				System.err.println("sv_cheats must not be 0");
				return CommandResult.CHEATS_REQUIRED;
			}
			else
			{
				command.execute(line);
				return CommandResult.COMMAND_EXECUTED;
			}
		}
		else if(line.size() == 1)
		{
			Cvar cvar = cvars.getCvar(commandName);
			
			if(cvar != null)
			{
				String latch = cvar.latchValueAsString();
				
				System.out.println(commandName + " -> " + cvar.asString() + " (default " + cvar.defaultValueAsString() + ", flags [" + Cvar.Flags.toString(cvar.getFlags()) + "]"
						+ (!latch.equals("null") ? (", latch " + latch) : (cvar.testFlag(Cvar.Flags.LATCH) ? ", no latch value" : "")) + ")");
				
				return CommandResult.CVAR_PRINTED;
			}
			else
			{
				System.out.println("Command or cvar '" + commandName + "' not found");
				return CommandResult.NOT_FOUND;
			}
		}
		else
		{
			int cvarErr = cvars.set(commandName, line.get(1)).getIdx();
			
			if(cvarErr <= 0)
			{
				System.out.printf(CVAR_ERROR_STRINGS[-cvarErr] + "\n", line.get(0), line.get(1));
				return CVAR_ERROR_RESULTS[-cvarErr];
			}
			else return CommandResult.CVAR_SET;
		}
	}
	
	public List<CommandResult> execute(String line)
	{
		List<List<String>> parsed = parseCommand(line);
		return parsed.isEmpty() ? List.of(CommandResult.EMPTY_LINE) : parsed.stream().map(this::execute).collect(Collectors.toList());
	}
	
	public void save()
	{
		cvars.save(configPath);
		System.out.println("Saved config");
	}
	
	public static List<List<String>> parseCommand(String line)
	{
		if(line.isBlank()) return Collections.emptyList();
		
		List<List<String>> result = new LinkedList<>();
		
		List<String> current = new ObjectArrayList<>();
		
		StringBuilder buffer = new StringBuilder(line.length());
		
		boolean quote = false;
		boolean escape = false;
		
		char[] chars = line.toCharArray();
		
		char c;
		
		for(int i = 0; i < chars.length; i++)
		{
			c = chars[i];
			
			if(escape)
			{
				escape = false;
				buffer.append(c);
			}
			else
			{
				if(Character.isWhitespace(c))
				{
					if(quote) buffer.append(c);
					else if(buffer.length() > 0)
					{
						current.add(buffer.toString());
						buffer.setLength(0);
					}
				}
				else if(c == '"')
				{
					quote = !quote;
					
					if(!quote || buffer.length() > 0)
					{
						current.add(buffer.toString());
						buffer.setLength(0);
					}
				}
				else if(c == ';')
				{
					if(quote) buffer.append(c);
					else
					{
						if(buffer.length() > 0)
						{
							current.add(buffer.toString());
							buffer.setLength(0);
						}
						
						if(current.size() > 0) result.add(Collections.unmodifiableList(current));
						
						current = new ArrayList<>();
					}
				}
				else if(c == '\\') escape = true;
				else buffer.append(c);
			}
		}
		
		if(buffer.length() > 0) current.add(buffer.toString());
		
		if(current.size() > 0) result.add(Collections.unmodifiableList(current));
		
		return Collections.unmodifiableList(result);
	}
}
