package info.kuonteje.voxeltest.console;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import info.kuonteje.voxeltest.util.MiscUtil;
import info.kuonteje.voxeltest.util.functional.BooleanBiConsumer;
import info.kuonteje.voxeltest.util.functional.DoubleBiConsumer;
import info.kuonteje.voxeltest.util.functional.LongBiConsumer;
import info.kuonteje.voxeltest.util.functional.ToBoolBiFunction;
import info.kuonteje.voxeltest.util.functional.ToBoolBooleanBiFunction;
import info.kuonteje.voxeltest.util.functional.ToBoolDoubleBiFunction;
import info.kuonteje.voxeltest.util.functional.ToBoolLongBiFunction;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.longs.Long2LongFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;

public class CvarRegistry
{
	public static enum SetResult
	{
		CALLBACK_CANCELED(4),
		I64_SET(1),
		F64_SET(2),
		STRING_SET(3),
		NOT_FOUND(0),
		INVALID_I64(-1),
		INVALID_F64(-2),
		INVALID_STRING(-3), // null
		CHEATS_REQUIRED(-4),
		READ_ONLY(-5);
		
		private final int idx;
		
		private SetResult(int idx)
		{
			this.idx = idx;
		}
		
		public int getIdx()
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
	
	private String getCached(String name)
	{
		if(!loaded) load(console.getConfigPath());
		
		if(loadCache != null)
		{
			String cached = loadCache.remove(name);
			if(loadCache.size() == 0) loadCache = null;
			return cached;
		}
		else return null;
	}
	
	public CvarI64 getCvarI64(String name, long initialValue, int flags, Long2LongFunction transformer, ToBoolLongBiFunction callback, boolean createIfMissing)
	{
		name = name.toLowerCase();
		
		synchronized(cvars)
		{
			Cvar existing = cvars.get(name);
			
			if(existing != null) return existing.getType() == Cvar.Type.I64 ? (CvarI64)existing : null;
			else if(!createIfMissing) return null;
			
			CvarI64 result = new CvarI64(this, name, initialValue, flags, transformer, callback);
			
			if(result.testFlag(Cvar.Flags.CONFIG))
			{
				String cached = getCached(name);
				if(cached != null) result.setString(cached, true);
			}
			
			cvars.put(name, result);
			
			return result;
		}
	}
	
	public CvarI64 getCvarI64C(String name, long initialValue, int flags, Long2LongFunction transformer, LongBiConsumer callback, boolean createIfMissing)
	{
		return getCvarI64(name, initialValue, flags, transformer, callback == null ? null : (n, o) ->
		{
			callback.accept(n, o);
			return true;
		}, createIfMissing);
	}
	
	public CvarI64 getCvarI64(String name, long initialValue, int flags, Long2LongFunction transformer, ToBoolLongBiFunction callback)
	{
		return getCvarI64(name, initialValue, flags, transformer, callback, true);
	}
	
	public CvarI64 getCvarI64C(String name, long initialValue, int flags, Long2LongFunction transformer, LongBiConsumer callback)
	{
		return getCvarI64C(name, initialValue, flags, transformer, callback, true);
	}
	
	public CvarI64 getCvarI64(String name, long initialValue, int flags, Long2LongFunction transformer)
	{
		return getCvarI64C(name, initialValue, flags, transformer, (LongBiConsumer)null, true);
	}
	
	public CvarF64 getCvarF64(String name, double initialValue, int flags, Double2DoubleFunction transformer, ToBoolDoubleBiFunction callback, boolean createIfMissing)
	{
		name = name.toLowerCase();
		
		synchronized(cvars)
		{
			Cvar existing = cvars.get(name);
			
			if(existing != null) return existing.getType() == Cvar.Type.F64 ? (CvarF64)existing : null;
			else if(!createIfMissing) return null;
			
			CvarF64 result = new CvarF64(this, name, initialValue, flags, transformer, callback);
			
			if(result.testFlag(Cvar.Flags.CONFIG))
			{
				String cached = getCached(name);
				if(cached != null) result.setString(cached, true);
			}
			
			cvars.put(name, result);
			
			return result;
		}
	}
	
	public CvarF64 getCvarF64C(String name, double initialValue, int flags, Double2DoubleFunction transformer, DoubleBiConsumer callback, boolean createIfMissing)
	{
		return getCvarF64(name, initialValue, flags, transformer, callback == null ? null : (n, o) ->
		{
			callback.accept(n, o);
			return true;
		}, createIfMissing);
	}
	
	public CvarF64 getCvarF64(String name, double initialValue, int flags, Double2DoubleFunction transformer, ToBoolDoubleBiFunction callback)
	{
		return getCvarF64(name, initialValue, flags, transformer, callback, true);
	}
	
	public CvarF64 getCvarF64C(String name, double initialValue, int flags, Double2DoubleFunction transformer, DoubleBiConsumer callback)
	{
		return getCvarF64C(name, initialValue, flags, transformer, callback, true);
	}
	
	public CvarF64 getCvarF64(String name, double initialValue, int flags, Double2DoubleFunction transformer)
	{
		return getCvarF64C(name, initialValue, flags, transformer, (DoubleBiConsumer)null, true);
	}
	
	public CvarString getCvarString(String name, String initialValue, int flags, Function<String, String> transformer, ToBoolBiFunction<String, String> callback, boolean createIfMissing)
	{
		name = name.toLowerCase();
		
		synchronized(cvars)
		{
			Cvar existing = cvars.get(name);
			
			if(existing != null) return existing.getType() == Cvar.Type.STRING ? (CvarString)existing : null;
			else if(!createIfMissing) return null;
			
			CvarString result = new CvarString(this, name, initialValue, flags, transformer, callback);
			
			if(result.testFlag(Cvar.Flags.CONFIG))
			{
				String cached = getCached(name);
				if(cached != null) result.setString(cached, true);
			}
			
			cvars.put(name, result);
			
			return result;
		}
	}
	
	public CvarString getCvarStringC(String name, String initialValue, int flags, Function<String, String> transformer, BiConsumer<String, String> callback, boolean createIfMissing)
	{
		return getCvarString(name, initialValue, flags, transformer, callback == null ? null : (n, o) ->
		{
			callback.accept(n, o);
			return true;
		}, createIfMissing);
	}
	
	public CvarString getCvarString(String name, String initialValue, int flags, Function<String, String> transformer, ToBoolBiFunction<String, String> callback)
	{
		return getCvarString(name, initialValue, flags, transformer, callback, true);
	}
	
	public CvarString getCvarStringC(String name, String initialValue, int flags, Function<String, String> transformer, BiConsumer<String, String> callback)
	{
		return getCvarStringC(name, initialValue, flags, transformer, callback, true);
	}
	
	public CvarString getCvarString(String name, String initialValue, int flags, Function<String, String> transformer)
	{
		return getCvarStringC(name, initialValue, flags, transformer, (BiConsumer<String, String>)null, true);
	}
	
	public CvarI64 getCvarBool(String name, boolean initialValue, int flags, ToBoolBooleanBiFunction callback, boolean createIfMissing)
	{
		return getCvarI64(name, initialValue ? 1L : 0L, flags, CvarI64.BOOL_TRANSFORMER, callback == null ? null : (n, o) -> callback.apply(n != 0L, o != 0L), createIfMissing);
	}
	
	public CvarI64 getCvarBool(String name, boolean initialValue, int flags, ToBoolBooleanBiFunction callback)
	{
		return getCvarBool(name, initialValue, flags, callback, true);
	}
	
	public CvarI64 getCvarBoolC(String name, boolean initialValue, int flags, BooleanBiConsumer callback, boolean createIfMissing)
	{
		return getCvarI64(name, initialValue ? 1L : 0L, flags, CvarI64.BOOL_TRANSFORMER, callback == null ? null : (n, o) ->
		{
			callback.accept(n != 0L, o != 0L);
			return true;
		}, createIfMissing);
	}
	
	public CvarI64 getCvarBoolC(String name, boolean initialValue, int flags, BooleanBiConsumer callback)
	{
		return getCvarBoolC(name, initialValue, flags, callback, true);
	}
	
	public CvarI64 getCvarBool(String name, boolean initialValue, int flags)
	{
		return getCvarBool(name, initialValue, flags, null, true);
	}
	
	public Cvar getCvar(String name)
	{
		return cvars.get(name.toLowerCase());
	}
	
	public SetResult set(String name, String value)
	{
		Cvar cvar = getCvar(name);
		return cvar == null ? SetResult.NOT_FOUND : cvar.setString(value);
	}
	
	public Cvar.Type getType(String name)
	{
		Cvar cvar = getCvar(name);
		return cvar == null ? Cvar.Type.NONE : cvar.getType();
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
	
	public Console getConsole()
	{
		return console;
	}
}
