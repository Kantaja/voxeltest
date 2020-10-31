package info.kuonteje.voxeltest.world;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.FrustumIntersection;
import org.joml.Vector3i;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.block.tag.ITransparentBlock;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.render.ChunkRenderer;
import info.kuonteje.voxeltest.render.IRenderable;
import info.kuonteje.voxeltest.render.block.BlockModel;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class Chunk
{
	private final World world;
	private final IChunkPosition pos;
	
	private final Vector3i offset;
	
	private ChunkRenderer renderer = null;
	
	private boolean cachedShouldRender = false;
	private final IRenderable opaque, transparent;
	
	private volatile boolean dirty = false;
	
	private int minX = 32, maxX = -1;
	private int minY = 32, maxY = -1;
	private int minZ = 32, maxZ = -1;
	
	private int blockCount = 0;
	
	private short[] blocks = new short[32 * 32 * 32];
	
	public Chunk(World world, IChunkPosition pos)
	{
		VoxelTest.addRenderHook(() -> renderer = new ChunkRenderer());
		
		opaque = new IRenderable()
		{
			@Override
			public boolean shouldRender(FrustumIntersection frustum)
			{
				return cachedShouldRender = blockCount > 0 && renderer != null && renderer.getTotalTriangles() > 0
						&& frustum.testAab(minX + offset.x, minY + offset.y, minZ + offset.z, maxX + offset.x + 1.0F, maxY + offset.y + 1.0F, maxZ + offset.z + 1.0F);
			}
			
			@Override
			public void render()
			{
				renderer.renderOpaque();
			}
		};
		
		transparent = new IRenderable()
		{
			@Override
			public boolean shouldRender(FrustumIntersection frustum)
			{
				// opaque always runs first
				return cachedShouldRender;
			}
			
			@Override
			public void render()
			{
				renderer.renderTransparent();
			}
		};
		
		this.world = world;
		this.pos = pos.immutable();
		
		this.offset = new Vector3i(pos.x() * 32, pos.y() * 32, pos.z() * 32);
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public IChunkPosition getPos()
	{
		return pos;
	}
	
	public void setDirty()
	{
		dirty = true;
	}
	
	public void setBlock(int x, int y, int z, Block block)
	{
		if(block != null)
		{
			if(x < minX) minX = x;
			if(x > maxX) maxX = x;
			
			if(y < minY) minY = y;
			if(y > maxY) maxY = y;
			
			if(z < minZ) minZ = z;
			if(z > maxZ) maxZ = z;
			
			blockCount++;
		}
		
		blocks[(x << 10) | (z << 5) | y] = block == null ? 0 : (short)DefaultRegistries.BLOCKS.getIdx(block);
		setDirty();
	}
	
	Block getBlock(int x, int y, int z)
	{
		return DefaultRegistries.BLOCKS.getByIdx(blocks[(x << 10) | (z << 5) | y] & 0xFFFF);
	}
	
	void tick()
	{
		if(dirty && renderer != null)
		{
			VoxelTest.getThreadPool().execute(() -> regenMesh());
			dirty = false;
		}
	}
	
	private void regenMesh()
	{
		MutableChunkPosition adj = new MutableChunkPosition(0, 0, 0);
		
		Chunk e = world.getLoadedChunk(adj.set(pos.x() + 1, pos.y(), pos.z()).immutable());
		Chunk w = world.getLoadedChunk(adj.set(pos.x() - 1, pos.y(), pos.z()).immutable());
		Chunk u = world.getLoadedChunk(adj.set(pos.x(), pos.y() + 1, pos.z()).immutable());
		Chunk d = world.getLoadedChunk(adj.set(pos.x(), pos.y() - 1, pos.z()).immutable());
		Chunk s = world.getLoadedChunk(adj.set(pos.x(), pos.y(), pos.z() + 1).immutable());
		Chunk n = world.getLoadedChunk(adj.set(pos.x(), pos.y(), pos.z() - 1).immutable());
		
		final IntSet transparentIndices = new IntAVLTreeSet();
		
		int opaqueFaces = 0, transparentFaces = 0;
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					int storageIdx = (x << 10) | (z << 5) | y;
					int idx = blocks[storageIdx] & 0xFFFF;
					
					if(idx != 0)
					{
						if(isTransparentBlock(idx, getPos(), x, y, z))
						{
							transparentIndices.add(storageIdx);
							
							if(isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x + 1, y, z)) transparentFaces++;
							if(isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x - 1, y, z)) transparentFaces++;
							if(isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x, y + 1, z)) transparentFaces++;
							if(isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x, y - 1, z)) transparentFaces++;
							if(isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z + 1)) transparentFaces++;
							if(isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z - 1)) transparentFaces++;
						}
						else
						{
							if(isTransparent(n, s, e, w, u, d, x + 1, y, z)) opaqueFaces++;
							if(isTransparent(n, s, e, w, u, d, x - 1, y, z)) opaqueFaces++;
							if(isTransparent(n, s, e, w, u, d, x, y + 1, z)) opaqueFaces++;
							if(isTransparent(n, s, e, w, u, d, x, y - 1, z)) opaqueFaces++;
							if(isTransparent(n, s, e, w, u, d, x, y, z + 1)) opaqueFaces++;
							if(isTransparent(n, s, e, w, u, d, x, y, z - 1)) opaqueFaces++;
						}
					}
				}
			}
		}
		
		renderer.setTriangles(opaqueFaces * 2, transparentFaces * 2);
		
		//if(opaqueFaces + transparentFaces > 0)
		{
			FloatBuffer vertexBuf = renderer.getVertexBuffer();
			FloatBuffer texCoordBuf = renderer.getTexCoordBuffer();
			FloatBuffer lightBuf = renderer.getLightBuffer();
			IntBuffer texLayerBuf = renderer.getTextureLayerBuffer();
			ByteBuffer tintBuf = renderer.getTintBuffer();
			
			for(int x = 0; x < 32; x++)
			{
				for(int z = 0; z < 32; z++)
				{
					for(int y = 0; y < 32; y++)
					{
						int storageIdx = (x << 10) | (z << 5) | y;
						
						if(!transparentIndices.contains(storageIdx))
						{
							int idx = blocks[storageIdx] & 0xFFFF;
							
							if(idx != 0)
							{
								BlockModel model = DefaultRegistries.BLOCK_MODELS.getById(DefaultRegistries.BLOCKS.getByIdx(idx).getId());
								
								boolean ev = isTransparent(n, s, e, w, u, d, x + 1, y, z);
								boolean wv = isTransparent(n, s, e, w, u, d, x - 1, y, z);
								boolean uv = isTransparent(n, s, e, w, u, d, x, y + 1, z);
								boolean dv = isTransparent(n, s, e, w, u, d, x, y - 1, z);
								boolean sv = isTransparent(n, s, e, w, u, d, x, y, z + 1);
								boolean nv = isTransparent(n, s, e, w, u, d, x, y, z - 1);
								
								model.getVertices(vertexBuf, x + offset.x, y + offset.y, z + offset.z, nv, sv, ev, wv, uv, dv);
								model.getTextureCoords(texCoordBuf, nv, sv, ev, wv, uv, dv);
								model.getLight(lightBuf, nv, sv, ev, wv, uv, dv);
								model.getTextureLayers(texLayerBuf, nv, sv, ev, wv, uv, dv);
								model.getTints(tintBuf, nv, sv, ev, wv, uv, dv);
							}
						}
					}
				}
			}
			
			for(int x = 0; x < 32; x++)
			{
				for(int z = 0; z < 32; z++)
				{
					for(int y = 0; y < 32; y++)
					{
						int storageIdx = (x << 10) | (z << 5) | y;
						int idx = blocks[storageIdx] & 0xFFFF;
						
						if(idx != 0 && transparentIndices.contains(storageIdx))
						{
							BlockModel model = DefaultRegistries.BLOCK_MODELS.getById(DefaultRegistries.BLOCKS.getByIdx(idx).getId());
							
							boolean ev = isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x + 1, y, z);
							boolean wv = isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x - 1, y, z);
							boolean uv = isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x, y + 1, z);
							boolean dv = isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x, y - 1, z);
							boolean sv = isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z + 1);
							boolean nv = isTransparentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z - 1);
							
							model.getVertices(vertexBuf, x + offset.x, y + offset.y, z + offset.z, nv, sv, ev, wv, uv, dv);
							model.getTextureCoords(texCoordBuf, nv, sv, ev, wv, uv, dv);
							model.getLight(lightBuf, nv, sv, ev, wv, uv, dv);
							model.getTextureLayers(texLayerBuf, nv, sv, ev, wv, uv, dv);
							model.getTints(tintBuf, nv, sv, ev, wv, uv, dv);
						}
					}
				}
			}
			
			vertexBuf.flip();
			texCoordBuf.flip();
			lightBuf.flip();
			texLayerBuf.flip();
			tintBuf.flip();
			
			VoxelTest.addRenderHook(renderer::loadMesh);
		}
	}
	
	/*
	private boolean isEmpty(Chunk n, Chunk s, Chunk e, Chunk w, Chunk u, Chunk d, int x, int y, int z)
	{
		return (x < 0 && (w == null || w.blocks[((x + 32) << 10) | (z << 5) | y] == 0))
				|| (x >= 32 && (e == null || e.blocks[((x - 32) << 10) | (z << 5) | y] == 0))
				|| (y < 0 && (d == null || d.blocks[(x << 10) | (z << 5) | (y + 32)] == 0))
				|| (y >= 32 && (u == null || u.blocks[(x << 10) | (z << 5) | (y - 32)] == 0))
				|| (z < 0 && (n == null || n.blocks[(x << 10) | ((z + 32) << 5) | y] == 0))
				|| (z >= 32 && (s == null || s.blocks[(x << 10) | ((z - 32) << 5) | y] == 0))
				|| (x >= 0 && x < 32 && y >= 0 && y < 32 && z >= 0 && z < 32 && blocks[(x << 10) | (z << 5) | y] == 0);
	}
	 */
	
	private boolean isTransparent(Chunk n, Chunk s, Chunk e, Chunk w, Chunk u, Chunk d, int x, int y, int z)
	{
		return (x < 0 && (w == null || isTransparentBlock(w.blocks[((x + 32) << 10) | (z << 5) | y], w.getPos(), x, y, z)))
				|| (x >= 32 && (e == null || isTransparentBlock(e.blocks[((x - 32) << 10) | (z << 5) | y], e.getPos(), x, y, z)))
				|| (y < 0 && (d == null || isTransparentBlock(d.blocks[(x << 10) | (z << 5) | (y + 32)], d.getPos(), x, y, z)))
				|| (y >= 32 && (u == null || isTransparentBlock(u.blocks[(x << 10) | (z << 5) | (y - 32)], u.getPos(), x, y, z)))
				|| (z < 0 && (n == null || isTransparentBlock(n.blocks[(x << 10) | ((z + 32) << 5) | y], n.getPos(), x, y, z)))
				|| (z >= 32 && (s == null || isTransparentBlock(s.blocks[(x << 10) | ((z - 32) << 5) | y], s.getPos(), x, y, z)))
				|| (x >= 0 && x < 32 && y >= 0 && y < 32 && z >= 0 && z < 32 && isTransparentBlock(blocks[(x << 10) | (z << 5) | y], getPos(), x, y, z));
	}
	
	private boolean isTransparentNeighborTransparent(int block, Chunk n, Chunk s, Chunk e, Chunk w, Chunk u, Chunk d, int x, int y, int z)
	{
		return (x < 0 && (w == null || isTransparentFaceVisible(block, w.blocks[((x + 32) << 10) | (z << 5) | y], w.getPos(), x, y, z)))
				|| (x >= 32 && (e == null || isTransparentFaceVisible(block, e.blocks[((x - 32) << 10) | (z << 5) | y], e.getPos(), x, y, z)))
				|| (y < 0 && (d == null || isTransparentFaceVisible(block, d.blocks[(x << 10) | (z << 5) | (y + 32)], d.getPos(), x, y, z)))
				|| (y >= 32 && (u == null || isTransparentFaceVisible(block, u.blocks[(x << 10) | (z << 5) | (y - 32)], u.getPos(), x, y, z)))
				|| (z < 0 && (n == null || isTransparentFaceVisible(block, n.blocks[(x << 10) | ((z + 32) << 5) | y], n.getPos(), x, y, z)))
				|| (z >= 32 && (s == null || isTransparentFaceVisible(block, s.blocks[(x << 10) | ((z - 32) << 5) | y], s.getPos(), x, y, z)))
				|| (x >= 0 && x < 32 && y >= 0 && y < 32 && z >= 0 && z < 32 && isTransparentFaceVisible(block, blocks[(x << 10) | (z << 5) | y], getPos(), x, y, z));
	}
	
	private boolean isTransparentBlock(int idx, IChunkPosition chunk, int x, int y, int z)
	{
		return idx == 0 || (DefaultRegistries.BLOCKS.getByIdx(idx) instanceof ITransparentBlock b && b.isTransparent(world, chunk.x() * 32 + x, chunk.y() * 32 + y, chunk.z() * 32 + z));
	}
	
	private boolean isTransparentFaceVisible(int block, int neighbor, IChunkPosition neighborChunk, int nx, int ny, int nz)
	{
		return block != neighbor && isTransparentBlock(neighbor, neighborChunk, nx, ny, nz);
	}
	
	public IRenderable opaque()
	{
		return opaque;
	}
	
	public IRenderable transparent()
	{
		return transparent;
	}
	
	void destroy()
	{
		if(renderer != null) renderer.destroy();
	}
}