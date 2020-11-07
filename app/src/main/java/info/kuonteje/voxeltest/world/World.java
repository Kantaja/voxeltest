package info.kuonteje.voxeltest.world;

import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

import info.kuonteje.voxeltest.Ticks;
import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.render.Renderer;
import info.kuonteje.voxeltest.world.worldgen.GeneratingChunkProvider;
import info.kuonteje.voxeltest.world.worldgen.TimingChunkProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

// TODO fix excessive synchronization on chunk accesses, it's the bottleneck in feature generation
// also extremely rare chunks staying as pregen
// also configurable worldgen
public class World implements Ticks.ITickHandler
{
	private final long seed;
	
	private final Long2ObjectMap<Int2ObjectMap<IChunk>> chunks = new Long2ObjectAVLTreeMap<>();
	
	private IChunkProvider chunkProvider;
	
	public World(long seed)
	{
		this.seed = seed;
		chunkProvider = new TimingChunkProvider(new GeneratingChunkProvider(this));
		
		initialLoad();
		
		VoxelTest.CONSOLE.addCommand("chunkstatus", (c, a) ->
		{
			int x = Integer.parseInt(a.get(1));
			int y = Integer.parseInt(a.get(2));
			int z = Integer.parseInt(a.get(3));
			System.out.println("(" + x + ", " + y + ", " + z + ") -> " + getChunkStatus(new ChunkPosition(x, y, z)).toString());
		}, 0);
		
		VoxelTest.CONSOLE.addCommand("chunktime", (c, a) ->
		{
			double totalTime = Chunk.meshTimer.totalTime();
			int totalMeshes = Chunk.meshTimer.totalOps();
			
			System.out.println("Generating " + totalMeshes + " chunk meshes took " + (Math.round(totalTime * 100000.0) / 100.0) +
					" ms, average " + (Math.round((totalTime / totalMeshes) * 100000.0) / 100.0) + " ms each, " + (Math.round((totalMeshes / totalTime) * 100.0) / 100.0) + " meshes/sec");
		}, 0);
	}
	
	public World()
	{
		this(new Random().nextLong());
	}
	
	private void initialLoad()
	{
		Phaser loadingChunks = new Phaser(1);
		int totalQueuedChunks = 0;
		
		for(int x = -8; x < 8; x++)
		{
			for(int z = -8; z < 8; z++)
			{
				for(int y = -4; y <= 2; y++)
				{
					ChunkPosition pos = new ChunkPosition(x, y, z);
					
					loadingChunks.register();
					totalQueuedChunks++;
					
					VoxelTest.getThreadPool().execute(() ->
					{
						try
						{
							getChunk(pos, MissingChunkAction.GENERATE);
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
		
		//loadingChunks.add(VoxelTest.getThreadPool().submit(() -> getChunk(new ChunkPosition(0, 0, 0), true)));
		
		loadingChunks.arriveAndAwaitAdvance();
		
		System.out.println("Queued " + totalQueuedChunks + " chunks for generation");
		
		if(chunkProvider instanceof TimingChunkProvider timer)
		{
			double totalTime = timer.totalTime();
			int totalChunks = timer.totalChunks();
			
			System.out.println("Generating " + totalChunks + " chunks took " + (Math.round(totalTime * 100000.0) / 100.0) +
					" ms, average " + (Math.round((totalTime / totalChunks) * 100000.0) / 100.0) + " ms each, " + (Math.round((totalChunks / totalTime) * 100.0) / 100.0) + " chunks/sec");
		}
	}
	
	public long getSeed()
	{
		return seed;
	}
	
	// MUST SYNCHRONIZE ON CHUNKS
	private Int2ObjectMap<IChunk> getColumn(IChunkPosition pos, boolean create)
	{
		return create ? chunks.computeIfAbsent(pos.plane(), p -> new Int2ObjectAVLTreeMap<>()) : chunks.get(pos.plane());
	}
	
	private Chunk createChunk(IChunkPosition pos)
	{
		Chunk chunk = chunkProvider.getChunk(pos.immutable());
		
		synchronized(chunks)
		{
			Int2ObjectMap<IChunk> column = getColumn(pos, true);
			
			synchronized(column)
			{
				PregenChunk pregen = (PregenChunk)column.get(pos.y());
				if(pregen != null) pregen.apply(chunk);
				
				column.put(pos.y(), chunk);
			}
		}
		
		return chunk;
	}
	
	private PregenChunk createPregen(IChunkPosition pos)
	{
		PregenChunk chunk = new PregenChunk(this, pos);
		
		synchronized(chunks)
		{
			Int2ObjectMap<IChunk> column = getColumn(pos, true);
			
			synchronized(column)
			{
				column.put(pos.y(), chunk);
			}
		}
		
		return chunk;
	}
	
	public IChunk getChunk(IChunkPosition pos, MissingChunkAction missingAction)
	{
		Int2ObjectMap<IChunk> column = getColumn(pos, false);
		
		if(column == null) return missingAction == MissingChunkAction.GENERATE ? createChunk(pos) : (missingAction == MissingChunkAction.GENERATE_PREGEN ? createPregen(pos) : null);
		else
		{
			// don't like locking the entire map for this, but we deadlock otherwise
			synchronized(chunks)
			{
				synchronized(column)
				{
					IChunk chunk = column.get(pos.y());
					
					if(chunk == null) return missingAction == MissingChunkAction.GENERATE ? createChunk(pos) : (missingAction == MissingChunkAction.GENERATE_PREGEN ? createPregen(pos) : null);
					else if(chunk instanceof PregenChunk && missingAction == MissingChunkAction.GENERATE) return createChunk(pos);
					else return chunk;
				}
			}
		}
	}
	
	public IChunk getChunkOrPregen(IChunkPosition pos)
	{
		return getChunk(pos, MissingChunkAction.GENERATE_PREGEN);
	}
	
	public Chunk getLoadedChunk(IChunkPosition pos)
	{
		IChunk chunk = getChunk(pos, MissingChunkAction.NOTHING);
		return chunk instanceof Chunk c ? c : null;
	}
	
	public ChunkStatus getChunkStatus(IChunkPosition pos)
	{
		synchronized(chunks)
		{
			Int2ObjectMap<IChunk> column = getColumn(pos, false);
			if(column == null) return ChunkStatus.NOT_LOADED;
			
			synchronized(column)
			{
				IChunk chunk = column.get(pos.y());
				
				if(chunk == null) return ChunkStatus.NOT_LOADED;
				else if(chunk instanceof PregenChunk) return ChunkStatus.PREGEN;
				else return ChunkStatus.LOADED;
			}
		}
	}
	
	public int getBlockIdx(int x, int y, int z, MissingChunkAction missingAction)
	{
		IChunk chunk = getChunk(new ChunkPosition(x >> 5, y >> 5, z >> 5), missingAction);
		return chunk.getBlockIdx(x & 0x1F, y & 0x1F, z & 0x1F);
	}
	
	public int getBlockIdx(int x, int y, int z)
	{
		return getBlockIdx(x, y, z, MissingChunkAction.NOTHING);
	}
	
	public Block getBlock(int x, int y, int z, MissingChunkAction missingAction)
	{
		IChunk chunk = getChunk(new ChunkPosition(x >> 5, y >> 5, z >> 5), missingAction);
		return chunk.getBlock(x & 0x1F, y & 0x1F, z & 0x1F);
	}
	
	public Block getBlock(int x, int y, int z)
	{
		return getBlock(x, y, z, MissingChunkAction.NOTHING);
	}
	
	public void setBlockIdx(int x, int y, int z, int idx)
	{
		getChunk(new ChunkPosition(x >> 5, y >> 5, z >> 5), MissingChunkAction.GENERATE_PREGEN).setBlockIdx(x & 0x1F, y & 0x1F, z & 0x1F, idx);
	}
	
	public void setBlock(int x, int y, int z, Block block)
	{
		getChunk(new ChunkPosition(x >> 5, y >> 5, z >> 5), MissingChunkAction.GENERATE_PREGEN).setBlock(x & 0x1F, y & 0x1F, z & 0x1F, block);
	}
	
	public void render(Renderer renderer)
	{
		forEachLoadedChunk(c ->
		{
			renderer.renderSolid(c.solid());
			renderer.renderTranslucent(c.translucent());
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
		chunks.values().forEach(col -> col.values().forEach(c ->
		{
			if(c instanceof Chunk chunk) action.accept(chunk);
		}));
	}
}
