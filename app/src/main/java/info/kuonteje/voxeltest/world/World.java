package info.kuonteje.voxeltest.world;

import java.util.Optional;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

import info.kuonteje.voxeltest.Ticks;
import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarI64;
import info.kuonteje.voxeltest.render.Renderer;
import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.util.MiscUtil;
import info.kuonteje.voxeltest.world.worldgen.GeneratingChunkProvider;
import info.kuonteje.voxeltest.world.worldgen.TimingChunkProvider;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

// TODO still some excessive synchronization
public class World implements Ticks.ITickHandler
{
	public static final CvarI64 svWorldgenTimer = VoxelTest.CONSOLE.cvars().cvarBool("sv_worldgen_timer", false, Cvar.Flags.CONFIG);
	
	// generates full new chunks instead of pregen chunks
	// TODO infinite loop from feature generators
	public static final CvarI64 svCascadingWorldgen = VoxelTest.CONSOLE.cvars().cvarBool("sv_cascading_worldgen", false, Cvar.Flags.READ_ONLY);
	
	private final long seed;
	
	private final IChunk emptyChunk = new EmptyChunk(this);
	private final Long2ObjectMap<IChunk> chunks = new Long2ObjectAVLTreeMap<>();
	
	private IChunkProvider chunkProvider;
	
	public World(long seed)
	{
		this.seed = seed;
		
		this.chunkProvider = new GeneratingChunkProvider(this);
		if(svWorldgenTimer.asBool()) this.chunkProvider = new TimingChunkProvider(this.chunkProvider);
		
		initialLoad();
		
		VoxelTest.CONSOLE.addCommand("chunkstatus", (c, a) ->
		{
			if(a.size() < 4)
			{
				System.out.println("chunkstatus <x> <y> <z>");
				return;
			}
			
			int x = Integer.parseInt(a.get(1));
			int y = Integer.parseInt(a.get(2));
			int z = Integer.parseInt(a.get(3));
			
			System.out.println("(" + x + ", " + y + ", " + z + ") -> " + statusOfChunkAt(x, y, z).toString());
		}, 0);
		
		VoxelTest.CONSOLE.addCommand("chunktime", (c, a) ->
		{
			double totalTime = Chunk.meshTimer.totalTime();
			int totalMeshes = Chunk.meshTimer.totalOps();
			
			System.out.println("Generating " + totalMeshes + " chunk meshes took " + MathUtil.roundDisplay(totalTime * 1000.0) +
					" ms, average " + MathUtil.roundDisplay(totalTime / totalMeshes * 1000.0) +
					" ms each, " + MathUtil.roundDisplay(totalMeshes / totalTime) / 100.0 + " meshes/sec/thread (" +
					MathUtil.roundDisplay((totalMeshes * VoxelTest.eWorkers.asInt()) / totalTime) + " meshes/sec)");
		}, 0);
	}
	
	public World()
	{
		this(MiscUtil.randomSeed());
	}
	
	private void initialLoad()
	{
		Phaser loadingChunks = new Phaser(1);
		int totalQueuedChunks = 0;
		
		for(int x = -8; x < 8; x++)
		{
			for(int z = -8; z < 8; z++)
			{
				for(int y = -4; y <= 3; y++)
				{
					ChunkPosition pos = new ChunkPosition(x, y, z);
					
					loadingChunks.register();
					totalQueuedChunks++;
					
					VoxelTest.threadPool().execute(() ->
					{
						try
						{
							tryCreateChunk(pos);
						}
						catch(Exception e)
						{
							new RuntimeException("Failed to generate chunk at " + pos.toString(), e).printStackTrace();
						}
						finally
						{
							loadingChunks.arriveAndDeregister();
						}
					});
				}
			}
		}
		
		System.out.println("Queued " + totalQueuedChunks + " chunks for generation");
		
		loadingChunks.arriveAndAwaitAdvance();
		
		if(chunkProvider instanceof TimingChunkProvider timer)
		{
			double totalTime = timer.totalTime();
			int totalChunks = timer.totalChunks();
			
			System.out.println("Generating " + totalChunks + " chunks took " + MathUtil.roundDisplay(totalTime * 1000.0) +
					" ms, average " + MathUtil.roundDisplay(totalTime / totalChunks * 1000.0) +
					" ms each, " + MathUtil.roundDisplay(totalChunks / totalTime) + " chunks/sec/thread (" +
					MathUtil.roundDisplay((totalChunks * VoxelTest.eWorkers.asInt()) / totalTime) + " chunks/sec)");
		}
	}
	
	public long seed()
	{
		return seed;
	}
	
	private Chunk tryCreateChunk(ChunkPosition pos)
	{
		Chunk chunk = chunkProvider.getChunk(pos);
		long key = pos.key();
		
		synchronized(chunks)
		{
			IChunk existing = chunks.get(key);
			
			if(existing instanceof Chunk c) return c; // race while generating
			if(existing instanceof PregenChunk pregen && pregen != null) pregen.apply(chunk);
			
			chunks.put(key, chunk);
		}
		
		return chunk;
	}
	
	private Chunk tryCreateChunk(int x, int y, int z)
	{
		return tryCreateChunk(new ChunkPosition(x, y, z));
	}
	
	private PregenChunk createPregen(int x, int y, int z)
	{
		ChunkPosition pos = new ChunkPosition(x, y, z);
		PregenChunk pregen = new PregenChunk(this, pos);
		
		chunks.put(pos.key(), pregen);
		
		return pregen;
	}
	
	public IChunk chunkAt(int x, int y, int z, MissingChunkAction missingAction)
	{
		if(!ChunkPosition.isValid(x, y, z)) return emptyChunk;
		
		synchronized(chunks)
		{
			IChunk chunk = chunks.get(ChunkPosition.key(x, y, z));
			
			if(chunk == null) return missingAction == MissingChunkAction.GENERATE ? tryCreateChunk(x, y, z) : (missingAction == MissingChunkAction.GENERATE_PREGEN ? createPregen(x, y, z) : emptyChunk);
			else if(chunk instanceof PregenChunk && missingAction == MissingChunkAction.GENERATE) return tryCreateChunk(x, y, z);
			else return chunk;
		}
	}
	
	public IChunk chunkOrPregenAt(int x, int y, int z)
	{
		return chunkAt(x, y, z, svCascadingWorldgen.asBool() ? MissingChunkAction.GENERATE : MissingChunkAction.GENERATE_PREGEN);
	}
	
	public IChunk loadedChunkAt(int x, int y, int z)
	{
		IChunk chunk = chunkAt(x, y, z, MissingChunkAction.NOTHING);
		return chunk instanceof Chunk c ? c : emptyChunk;
	}
	
	public ChunkStatus statusOfChunkAt(int x, int y, int z)
	{
		synchronized(chunks)
		{
			IChunk chunk = chunks.get(ChunkPosition.key(x, y, z));
			
			if(chunk == null) return ChunkStatus.NOT_LOADED;
			else if(chunk instanceof PregenChunk) return ChunkStatus.PREGEN;
			else return ChunkStatus.LOADED;
		}
	}
	
	public IChunk emptyChunk()
	{
		return emptyChunk;
	}
	
	public int blockIdxAt(int x, int y, int z, MissingChunkAction missingAction)
	{
		IChunk chunk = chunkAt(x >> 5, y >> 5, z >> 5, missingAction);
		return chunk.blockIdxAt(x & 0x1F, y & 0x1F, z & 0x1F);
	}
	
	public int blockIdxAt(int x, int y, int z)
	{
		return blockIdxAt(x, y, z, MissingChunkAction.NOTHING);
	}
	
	public Optional<Block> blockAt(int x, int y, int z, MissingChunkAction missingAction)
	{
		IChunk chunk = chunkAt(x >> 5, y >> 5, z >> 5, missingAction);
		return chunk.blockAt(x & 0x1F, y & 0x1F, z & 0x1F);
	}
	
	public Optional<Block> blockAt(int x, int y, int z)
	{
		return blockAt(x, y, z, MissingChunkAction.NOTHING);
	}
	
	public void setBlockIdx(int x, int y, int z, int idx, int flags, BlockPredicate predicate)
	{
		chunkOrPregenAt(x >> 5, y >> 5, z >> 5).setBlockIdx(x & 0x1F, y & 0x1F, z & 0x1F, idx, flags, predicate);
	}
	
	public void setBlock(int x, int y, int z, Block block, int flags, BlockPredicate predicate)
	{
		chunkOrPregenAt(x >> 5, y >> 5, z >> 5).setBlock(x & 0x1F, y & 0x1F, z & 0x1F, block, flags, predicate);
	}
	
	public void setBlockIdx(int x, int y, int z, int idx, int flags)
	{
		setBlockIdx(x, y, z, idx, flags, null);
	}
	
	public void setBlock(int x, int y, int z, Block block, int flags)
	{
		setBlock(x, y, z, block, flags, null);
	}
	
	public void update(int x, int y, int z)
	{
		blockAt(x, y, z).ifPresent(b -> b.update(this, x, y, z));
	}
	
	public void updateAround(int x, int y, int z)
	{
		update(x - 1, y, z);
		update(x + 1, y, z);
		update(x, y - 1, z);
		update(x, y + 1, z);
		update(x, y, z - 1);
		update(x, y, z + 1);
	}
	
	public void render()
	{
		forEachLoadedChunk(c ->
		{
			Renderer.renderSolid(c.solid());
			Renderer.renderTranslucent(c.translucent());
		});
	}
	
	@Override
	public void tick(double delta)
	{
		forEachLoadedChunk(Chunk::tick);
	}
	
	@Override
	public String name()
	{
		return "world";
	}
	
	public void destroy()
	{
		forEachLoadedChunk(Chunk::destroy);
	}
	
	public void forEachLoadedChunk(Consumer<Chunk> action)
	{
		chunks.values().forEach(c ->
		{
			if(c instanceof Chunk chunk) action.accept(chunk);
		});
	}
}
