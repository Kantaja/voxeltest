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
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

// TODO still some excessive synchronization
// pregen bug seems to be gone?
// also configurable worldgen
public class World implements Ticks.ITickHandler
{
	private final long seed;
	
	private final IChunk emptyChunk = new EmptyChunk(this);
	private final Long2ObjectMap<IChunk> chunks = new Long2ObjectAVLTreeMap<>();
	
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
			System.out.println("(" + x + ", " + y + ", " + z + ") -> " + getChunkStatus(x, y, z).toString());
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
		
		loadingChunks.arriveAndAwaitAdvance();
		
		System.out.println("Queued " + totalQueuedChunks + " chunks for generation");
		
		if(chunkProvider instanceof TimingChunkProvider timer)
		{
			double totalTime = timer.totalTime();
			int totalChunks = timer.totalChunks();
			
			System.out.println("Generating " + totalChunks + " chunks took " + (Math.round(totalTime * 100000.0) / 100.0) +
					" ms, average " + (Math.round((totalTime / totalChunks) * 100000.0) / 100.0) + " ms each, " +
					(Math.round((totalChunks / totalTime) * 100.0) / 100.0) + " chunks/sec/thread (" +
					(Math.round(((totalChunks * Runtime.getRuntime().availableProcessors()) / totalTime) * 100.0) / 100.0) + " chunks/sec)");
		}
	}
	
	public long getSeed()
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
	
	public IChunk getChunk(int x, int y, int z, MissingChunkAction missingAction)
	{
		synchronized(chunks)
		{
			IChunk chunk = chunks.get(ChunkPosition.key(x, y, z));
			
			if(chunk == null) return missingAction == MissingChunkAction.GENERATE ? tryCreateChunk(x, y, z) : (missingAction == MissingChunkAction.GENERATE_PREGEN ? createPregen(x, y, z) : emptyChunk);
			else if(chunk instanceof PregenChunk && missingAction == MissingChunkAction.GENERATE) return tryCreateChunk(x, y, z);
			else return chunk;
		}
	}
	
	public IChunk getChunkOrPregen(int x, int y, int z)
	{
		return getChunk(x, y, z, MissingChunkAction.GENERATE_PREGEN);
	}
	
	public IChunk getLoadedChunk(int x, int y, int z)
	{
		IChunk chunk = getChunk(x, y, z, MissingChunkAction.NOTHING);
		return chunk instanceof Chunk c ? c : emptyChunk;
	}
	
	public ChunkStatus getChunkStatus(int x, int y, int z)
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
	
	public int getBlockIdx(int x, int y, int z, MissingChunkAction missingAction)
	{
		IChunk chunk = getChunk(x >> 5, y >> 5, z >> 5, missingAction);
		return chunk.getBlockIdx(x & 0x1F, y & 0x1F, z & 0x1F);
	}
	
	public int getBlockIdx(int x, int y, int z)
	{
		return getBlockIdx(x, y, z, MissingChunkAction.NOTHING);
	}
	
	public Block getBlock(int x, int y, int z, MissingChunkAction missingAction)
	{
		IChunk chunk = getChunk(x >> 5, y >> 5, z >> 5, missingAction);
		return chunk.getBlock(x & 0x1F, y & 0x1F, z & 0x1F);
	}
	
	public Block getBlock(int x, int y, int z)
	{
		return getBlock(x, y, z, MissingChunkAction.NOTHING);
	}
	
	public void setBlockIdx(int x, int y, int z, int idx)
	{
		getChunkOrPregen(x >> 5, y >> 5, z >> 5).setBlockIdx(x & 0x1F, y & 0x1F, z & 0x1F, idx);
	}
	
	public void setBlock(int x, int y, int z, Block block)
	{
		getChunkOrPregen(x >> 5, y >> 5, z >> 5).setBlock(x & 0x1F, y & 0x1F, z & 0x1F, block);
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
