package info.kuonteje.voxeltest.world;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import info.kuonteje.voxeltest.Ticks;
import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.render.ChunkShaderBindings;
import info.kuonteje.voxeltest.render.Renderer;
import info.kuonteje.voxeltest.world.worldgen.ChunkProviderGenerate;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class World implements Ticks.ITickHandler
{
	private final long seed;
	
	private final Long2ObjectMap<Int2ObjectMap<Chunk>> chunks = new Long2ObjectAVLTreeMap<>();
	
	private IChunkProvider chunkProvider;
	
	public World(long seed)
	{
		this.seed = seed;
		
		chunkProvider = new ChunkProviderGenerate(this);
		
		List<Future<?>> loadingChunks = new ObjectArrayList<>();
		
		for(int x = -8; x < 8; x++)
		{
			for(int z = -8; z < 8; z++)
			{
				for(int y = -4; y <= 2; y++)
				{
					final int fx = x;
					final int fy = y;
					final int fz = z;
					loadingChunks.add(VoxelTest.getThreadPool().submit(() -> getChunk(new ChunkPosition(fx, fy, fz), true)));
				}
			}
		}
		
		//loadingChunks.add(VoxelTest.getThreadPool().submit(() -> getChunk(new ChunkPosition(0, 0, 0), true)));
		
		loadingChunks.forEach(f ->
		{
			try
			{
				f.get();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		});
		
		VoxelTest.CONSOLE.addCommand("chunkinfo", (c, a) ->
		{
			int x = Integer.parseInt(a.get(1));
			int y = Integer.parseInt(a.get(2));
			int z = Integer.parseInt(a.get(3));
			System.out.println("Chunk at (" + x + ", " + y + ", " + z + ") is " + (getLoadedChunk(new ChunkPosition(x, y, z)) == null ? "not loaded" : "loaded"));
		}, 0);
	}
	
	public World()
	{
		this(new Random().nextLong());
	}
	
	public long getSeed()
	{
		return seed;
	}
	
	private Int2ObjectMap<Chunk> getColumn(IChunkPosition pos, boolean create)
	{
		synchronized(chunks)
		{
			return create ? chunks.computeIfAbsent(pos.plane(), p -> new Int2ObjectAVLTreeMap<>()) : chunks.get(pos.plane());
		}
	}
	
	private Chunk createChunk(IChunkPosition pos)
	{
		Chunk chunk = chunkProvider.getChunk(pos.immutable());
		Int2ObjectMap<Chunk> column = getColumn(pos, true);
		
		synchronized(column)
		{
			column.put(pos.y(), chunk);
		}
		
		return chunk;
	}
	
	public Chunk getChunk(IChunkPosition pos, boolean generate)
	{
		Int2ObjectMap<Chunk> column = getColumn(pos, false);
		
		if(column == null) return generate ? createChunk(pos) : null;
		else
		{
			synchronized(column)
			{
				Chunk chunk = column.get(pos.y());
				
				if(chunk == null) return generate ? createChunk(pos) : null;
				else return chunk;
			}
		}
	}
	
	public Chunk getLoadedChunk(IChunkPosition pos)
	{
		return getChunk(pos, false);
	}
	
	public Block getBlock(int x, int y, int z, boolean generate)
	{
		Chunk chunk = getChunk(new ChunkPosition(x >> 5, y >> 5, z >> 5), generate);
		
		return chunk.getBlock(x & 0x1F, y & 0x1F, z & 0x1F);
	}
	
	public void render(Renderer renderer)
	{
		glUniform1i(ChunkShaderBindings.BASE_TRIANGLE_ID, 0);
		forEachLoadedChunk(c -> renderer.render(c.opaque()));
		
		glEnable(GL_BLEND);
		forEachLoadedChunk(c -> renderer.render(c.transparent()));
		glDisable(GL_BLEND);
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
		chunks.values().forEach(col -> col.values().forEach(action));
		//Long2ObjectMaps.fastForEach(chunks, e -> Int2ObjectMaps.fastForEach(e.getValue(), c -> action.accept(c.getValue())));
	}
}
