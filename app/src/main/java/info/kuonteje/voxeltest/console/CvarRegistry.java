package info.kuonteje.voxeltest.console;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import info.kuonteje.voxeltest.util.MiscUtil;
import info.kuonteje.voxeltest.util.functional.BooleanBiConsumer;
import info.kuonteje.voxeltest.util.functional.BooleanUnaryOperator;
import info.kuonteje.voxeltest.util.functional.DoubleBiConsumer;
import info.kuonteje.voxeltest.util.functional.LongBiConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;

public class CvarRegistry
{
	public static enum SetResult
	{
		I64_SET(1),
		F64_SET(2),
		STRING_SET(3),
		ENUM_SET(5),
		NOT_FOUND(0),
		INVALID_I64(-1),
		INVALID_F64(-2),
		INVALID_STRING(-3), // null
		CHEATS_REQUIRED(-4),
		READ_ONLY(-5),
		INVALID_ENUM(-6);
		
		private final int idx;
		
		private SetResult(int idx)
		{
			this.idx = idx;
		}
		
		public int idx()
		{
			return idx;
		}
	}
	
	private final Console console;
	private final Object2ObjectMap<String, Cvar> cvars = new Object2ObjectRBTreeMap<>();
	
	private boolean loaded = false;
	private Object2ObjectMap<String, String> loadCache = null;
	
	public CvarRegistry(Console console)
	{
		this.console = console;
		
		console.addCommand("cvarlist", (c, args) ->
		{
			String search = args.size() > 1 ? args.get(1).toLowerCase() : null;
			
			synchronized(cvars)
			{
				Object2ObjectMaps.fastForEach(cvars, e ->
				{
					if(search == null || e.getValue().name().contains(search)) System.out.println(e.getValue().toString());
				});
			}
		}, Command.Flags.IMMUTABLE);
		
		console.addCommand("reset", (c, args) ->
		{
			if(args.size() < 2)
			{
				System.out.println("reset <cvar>");
				return;
			}
			
			String name = args.get(1).toLowerCase();
			Cvar cvar = cvars.get(name);
			
			if(cvar == null)
			{
				System.out.println("Cvar '" + name + "' not found");
				return;
			}
			
			cvar.reset();
		}, Command.Flags.IMMUTABLE);
		
		console.addCommand("resetall", (c, args) ->
		{
			boolean cheats = c.cheatsEnabled();
			
			synchronized(cvars)
			{
				Object2ObjectMaps.fastForEach(cvars, e ->
				{
					if(cheats || !e.getValue().testFlag(Cvar.Flags.CHEAT)) e.getValue().reset();
				});
			}
		}, Command.Flags.IMMUTABLE);
	}
	
	public void save(Path configPath)
	{
		synchronized(cvars)
		{
			try(BufferedWriter writer = Files.newBufferedWriter(configPath.resolve("cvars.cfg")))
			{
				cvars.values().stream().filter(c -> c.testFlag(Cvar.Flags.CONFIG)).forEach(c ->
				{
					try
					{
						writer.write(c.name() + " " + c.asString() + "\n");
					}
					catch(IOException e)
					{
						System.err.println("Failed to save cvar " + c.name() + " -> " + c.asString());
						e.printStackTrace();
					}
				});
			}
			catch(IOException e)
			{
				System.err.println("Failed to save cvars");
				e.printStackTrace();
			}
		}
	}
	
	public void load(Path configPath)
	{
		if(!loaded)
		{
			try(Stream<String> lines = Files.lines(configPath.resolve("cvars.cfg")))
			{
				loadCache = new Object2ObjectOpenHashMap<>();
				
				List<List<String>> setLines = MiscUtil.flatten(lines
						.map(String::trim)
						.filter(l -> !l.isEmpty() && l.charAt(0) != '#')
						.map(Console::parseCommand)
						.collect(Collectors.toList()));
				setLines.stream().filter(l -> l.size() > 1).forEach(l -> loadCache.put(l.get(0).toLowerCase(), l.get(1)));
			}
			catch(NoSuchFileException e) {}
			catch(IOException e)
			{
				System.err.println("failed to read cvars.cfg");
				loadCache = null;
			}
			
			loaded = true;
		}
	}
	
	private String cached(String name)
	{
		if(!loaded) load(console.configPath());
		
		if(loadCache != null)
		{
			String cached = loadCache.remove(name);
			if(loadCache.size() == 0) loadCache = null;
			return cached;
		}
		else return null;
	}
	
	// hey wouldn't it be nice if this language had default arguments
	private CvarI64 cvarI64(String name, long initialValue, int flags, LongUnaryOperator transformer, LongBiConsumer callback, boolean createIfMissing)
	{
		name = name.toLowerCase();
		
		synchronized(cvars)
		{
			Cvar existing = cvars.get(name);
			
			if(existing != null) return existing.type() == Cvar.Type.I64 ? (CvarI64)existing : null;
			else if(!createIfMissing) return null;
			
			CvarI64 result = new CvarI64(this, name, initialValue, flags, transformer, callback);
			
			if(result.testFlag(Cvar.Flags.CONFIG))
			{
				String cached = cached(name);
				if(cached != null) result.setString(cached, true);
			}
			
			cvars.put(name, result);
			
			return result;
		}
	}
	
	public CvarI64 cvarI64(String name, long initialValue, int flags, LongUnaryOperator transformer, LongBiConsumer callback)
	{
		return cvarI64(name, initialValue, flags, transformer, callback, true);
	}
	
	public CvarI64 cvarI64(String name, long initialValue, int flags, LongUnaryOperator transformer)
	{
		return cvarI64(name, initialValue, flags, transformer, null, true);
	}
	
	public CvarI64 cvarI64(String name, long initialValue, int flags)
	{
		return cvarI64(name, initialValue, flags, null, null, true);
	}
	
	public CvarI64 cvarI64(String name)
	{
		return cvarI64(name, 0L, 0, null, null, false);
	}
	
	private CvarF64 cvarF64(String name, double initialValue, int flags, DoubleUnaryOperator transformer, DoubleBiConsumer callback, boolean createIfMissing)
	{
		name = name.toLowerCase();
		
		synchronized(cvars)
		{
			Cvar existing = cvars.get(name);
			
			if(existing != null) return existing.type() == Cvar.Type.F64 ? (CvarF64)existing : null;
			else if(!createIfMissing) return null;
			
			CvarF64 result = new CvarF64(this, name, initialValue, flags, transformer, callback);
			
			if(result.testFlag(Cvar.Flags.CONFIG))
			{
				String cached = cached(name);
				if(cached != null) result.setString(cached, true);
			}
			
			cvars.put(name, result);
			
			return result;
		}
	}
	
	public CvarF64 cvarF64(String name, double initialValue, int flags, DoubleUnaryOperator transformer, DoubleBiConsumer callback)
	{
		return cvarF64(name, initialValue, flags, transformer, callback, true);
	}
	
	public CvarF64 cvarF64(String name, double initialValue, int flags, DoubleUnaryOperator transformer)
	{
		return cvarF64(name, initialValue, flags, transformer, null, true);
	}
	
	public CvarF64 cvarF64(String name, double initialValue, int flags)
	{
		return cvarF64(name, initialValue, flags, null, null, true);
	}
	
	public CvarF64 cvarF64(String name)
	{
		return cvarF64(name, 0.0, 0, null, null, false);
	}
	
	private CvarString cvarString(String name, String initialValue, int flags, Function<String, String> transformer, BiConsumer<String, String> callback, boolean createIfMissing)
	{
		name = name.toLowerCase();
		
		synchronized(cvars)
		{
			Cvar existing = cvars.get(name);
			
			if(existing != null) return existing.type() == Cvar.Type.STRING ? (CvarString)existing : null;
			else if(!createIfMissing) return null;
			
			CvarString result = new CvarString(this, name, initialValue, flags, transformer, callback);
			
			if(result.testFlag(Cvar.Flags.CONFIG))
			{
				String cached = cached(name);
				if(cached != null) result.setString(cached, true);
			}
			
			cvars.put(name, result);
			
			return result;
		}
	}
	
	public CvarString cvarString(String name, String initialValue, int flags, Function<String, String> transformer, BiConsumer<String, String> callback)
	{
		return cvarString(name, initialValue, flags, transformer, callback, true);
	}
	
	public CvarString cvarString(String name, String initialValue, int flags, Function<String, String> transformer)
	{
		return cvarString(name, initialValue, flags, transformer, null, true);
	}
	
	public CvarString cvarString(String name, String initialValue, int flags)
	{
		return cvarString(name, initialValue, flags, null, null, true);
	}
	
	public CvarString cvarString(String name)
	{
		return cvarString(name, null, 0, null, null, false);
	}
	
	private CvarI64 cvarBool(String name, boolean initialValue, int flags, BooleanUnaryOperator transformer, BooleanBiConsumer callback, boolean createIfMissing)
	{
		return cvarI64(name, initialValue ? 1L : 0L, flags, transformer == null ? CvarI64.BOOL_TRANSFORMER
				: CvarI64.BOOL_TRANSFORMER.andThen(v -> transformer.applyAsBool(v != 0L) ? 1L : 0L),
				callback == null ? null : (n, o) -> callback.accept(n != 0L, o != 0L), createIfMissing);
	}
	
	public CvarI64 cvarBool(String name, boolean initialValue, int flags, BooleanUnaryOperator transformer, BooleanBiConsumer callback)
	{
		return cvarBool(name, initialValue, flags, transformer, callback, true);
	}
	
	public CvarI64 cvarBool(String name, boolean initialValue, int flags, BooleanUnaryOperator transformer)
	{
		return cvarBool(name, initialValue, flags, transformer, null, true);
	}
	
	public CvarI64 cvarBool(String name, boolean initialValue, int flags)
	{
		return cvarBool(name, initialValue, flags, null, null, true);
	}
	
	public Optional<CvarI64> cvarBool(String name)
	{
		return Optional.ofNullable(cvarBool(name, false, 0, null, null, false));
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Enum<T>> CvarEnum<T> cvarEnum(Class<T> enumType, String name, T initialValue, int flags, Function<T, T> transformer, BiConsumer<T, T> callback, boolean createIfMissing)
	{
		name = name.toLowerCase();
		
		synchronized(cvars)
		{
			Cvar existing = cvars.get(name);
			
			if(existing != null)
			{
				if(existing.type() == Cvar.Type.STRING) return null;
				
				CvarEnum<?> e = (CvarEnum<?>)existing;
				return enumType == CvarEnum.ANY_TYPE || e.enumType() == enumType ? (CvarEnum<T>)e : null;
			}
			else if(!createIfMissing) return null;
			
			CvarEnum<T> result = new CvarEnum<>(this, enumType, name, initialValue, flags, transformer, callback);
			
			if(result.testFlag(Cvar.Flags.CONFIG))
			{
				String cached = cached(name);
				if(cached != null) result.setString(cached, true);
			}
			
			cvars.put(name, result);
			
			return result;
		}
	}
	
	public <T extends Enum<T>> CvarEnum<T> cvarEnum(Class<T> enumType, String name, T initialValue, int flags, Function<T, T> transformer, BiConsumer<T, T> callback)
	{
		return cvarEnum(enumType, name, initialValue, flags, transformer, callback, true);
	}
	
	public <T extends Enum<T>> CvarEnum<T> cvarEnum(Class<T> enumType, String name, T initialValue, int flags, Function<T, T> transformer)
	{
		return cvarEnum(enumType, name, initialValue, flags, transformer, null, true);
	}
	
	public <T extends Enum<T>> CvarEnum<T> cvarEnum(Class<T> enumType, String name, T initialValue, int flags)
	{
		return cvarEnum(enumType, name, initialValue, flags, null, null, true);
	}
	
	public <T extends Enum<T>> Optional<CvarEnum<T>> cvarEnum(Class<T> enumType, String name)
	{
		return Optional.ofNullable(cvarEnum(enumType, name, null, 0, null, null, false));
	}
	
	public Optional<Cvar> get(String name)
	{
		return Optional.ofNullable(cvars.get(name.toLowerCase()));
	}
	
	public SetResult set(String name, String value)
	{
		return get(name).map(c -> c.setString(value)).orElse(SetResult.NOT_FOUND);
	}
	
	public Cvar.Type typeOf(String name)
	{
		return get(name).map(Cvar::type).orElse(Cvar.Type.NONE);
	}
	
	void resetCheats()
	{
		synchronized(cvars)
		{
			Object2ObjectMaps.fastForEach(cvars, e ->
			{
				if(e.getValue().testFlag(Cvar.Flags.CHEAT)) e.getValue().reset();
			});
		}
	}
	
	public Console console()
	{
		return console;
	}
}
