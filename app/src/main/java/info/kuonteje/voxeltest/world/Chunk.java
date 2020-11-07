package info.kuonteje.voxeltest.world;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.FrustumIntersection;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.block.tag.ICutoutBlock;
import info.kuonteje.voxeltest.block.tag.ITranslucentBlock;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.render.ChunkRenderable;
import info.kuonteje.voxeltest.render.ChunkRenderer;
import info.kuonteje.voxeltest.render.IRenderable;
import info.kuonteje.voxeltest.render.block.BlockModel;
import info.kuonteje.voxeltest.util.ConcurrentTimer;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public final class Chunk implements IChunk
{
	public static final ConcurrentTimer meshTimer = new ConcurrentTimer();
	
	private final World world;
	private final IChunkPosition pos;
	
	private final Vector3dc center;
	
	private final Vector3i offset;
	
	private ChunkRenderer renderer = null;
	
	private final ChunkRenderable solid, translucent;
	
	private volatile boolean dirty = false;
	
	private int minX = 32, maxX = -1;
	private int minY = 32, maxY = -1;
	private int minZ = 32, maxZ = -1;
	
	private int blockCount = 0;
	
	private short[] blocks = new short[32 * 32 * 32];
	
	public Chunk(World world, IChunkPosition pos)
	{
		solid = new ChunkRenderable(this, true);
		translucent = new ChunkRenderable(this, false);
		
		VoxelTest.addRenderHook(() ->
		{
			renderer = new ChunkRenderer();
			
			solid.setRenderer(renderer);
			translucent.setRenderer(renderer);
		});
		
		this.world = world;
		this.pos = pos.immutable();
		
		this.center = new Vector3d(pos.worldX() + 16.0, pos.worldY() + 16.0, pos.worldZ() + 16.0);
		
		this.offset = new Vector3i(pos.worldX(), pos.worldY(), pos.worldZ());
	}
	
	@Override
	public World getWorld()
	{
		return world;
	}
	
	@Override
	public IChunkPosition getPos()
	{
		return pos;
	}
	
	@Override
	public int getBlockIdx(int x, int y, int z)
	{
		return (x < 0 || x >= 32 || y < 0 || y >= 32 || z < 0 || z >= 32) ? 0 : blocks[storageIdx(x, y, z)] & 0xFFFF;
	}
	
	@Override
	public Block getBlock(int x, int y, int z)
	{
		return DefaultRegistries.BLOCKS.getByIdx(getBlockIdx(x, y, z));
	}
	
	public void setDirty()
	{
		dirty = true;
	}
	
	@Override
	public void setBlockIdx(int x, int y, int z, int idx)
	{
		int storageIdx = storageIdx(x, y, z);
		
		if(blocks[storageIdx] == idx) return;
		
		if(idx != 0)
		{
			if(blocks[storageIdx] == 0)
			{
				if(x < minX) minX = x;
				if(x > maxX) maxX = x;
				
				if(y < minY) minY = y;
				if(y > maxY) maxY = y;
				
				if(z < minZ) minZ = z;
				if(z > maxZ) maxZ = z;
				
				blockCount++;
			}
		}
		else blockCount--; // eventually I'll fix aabbs
		
		blocks[storageIdx] = idx > DefaultRegistries.BLOCKS.maxIdx() ? 0 : (short)idx;
		setDirty();
	}
	
	@Override
	public void setBlock(int x, int y, int z, Block block)
	{
		setBlockIdx(x, y, z, DefaultRegistries.BLOCKS.getIdx(block));
	}
	
	void tick()
	{
		if(dirty && renderer != null)
		{
			VoxelTest.getThreadPool().execute(() -> meshTimer.time(() -> regenMesh()));
			dirty = false;
		}
	}
	
	// TODO optimize, this takes 30 ms
	// huge capacity for it, but not urgent
	private void regenMesh()
	{
		MutableChunkPosition adj = new MutableChunkPosition(0, 0, 0);
		
		Chunk e = world.getLoadedChunk(adj.set(pos.x() + 1, pos.y(), pos.z()).immutable());
		Chunk w = world.getLoadedChunk(adj.set(pos.x() - 1, pos.y(), pos.z()).immutable());
		Chunk u = world.getLoadedChunk(adj.set(pos.x(), pos.y() + 1, pos.z()).immutable());
		Chunk d = world.getLoadedChunk(adj.set(pos.x(), pos.y() - 1, pos.z()).immutable());
		Chunk s = world.getLoadedChunk(adj.set(pos.x(), pos.y(), pos.z() + 1).immutable());
		Chunk n = world.getLoadedChunk(adj.set(pos.x(), pos.y(), pos.z() - 1).immutable());
		
		final IntSet translucentIndices = new IntAVLTreeSet();
		
		int solidFaces = 0, translucentFaces = 0;
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					int storageIdx = storageIdx(x, y, z);
					int idx = blocks[storageIdx] & 0xFFFF;
					
					if(idx != 0)
					{
						if(isTranslucent(idx, getPos(), x, y, z))
						{
							translucentIndices.add(storageIdx);
							
							if(isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x + 1, y, z)) translucentFaces++;
							if(isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x - 1, y, z)) translucentFaces++;
							if(isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y + 1, z)) translucentFaces++;
							if(isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y - 1, z)) translucentFaces++;
							if(isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z + 1)) translucentFaces++;
							if(isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z - 1)) translucentFaces++;
						}
						else
						{
							if(hasTransparency(n, s, e, w, u, d, x + 1, y, z)) solidFaces++;
							if(hasTransparency(n, s, e, w, u, d, x - 1, y, z)) solidFaces++;
							if(hasTransparency(n, s, e, w, u, d, x, y + 1, z)) solidFaces++;
							if(hasTransparency(n, s, e, w, u, d, x, y - 1, z)) solidFaces++;
							if(hasTransparency(n, s, e, w, u, d, x, y, z + 1)) solidFaces++;
							if(hasTransparency(n, s, e, w, u, d, x, y, z - 1)) solidFaces++;
						}
					}
				}
			}
		}
		
		renderer.setTriangles(solidFaces * 2, translucentFaces * 2);
		
		if(solidFaces + translucentFaces > 0)
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
						int storageIdx = storageIdx(x, y, z);
						
						if(!translucentIndices.contains(storageIdx))
						{
							int idx = blocks[storageIdx] & 0xFFFF;
							
							if(idx != 0)
							{
								BlockModel model = DefaultRegistries.BLOCK_MODELS.getById(DefaultRegistries.BLOCKS.getByIdx(idx).getId());
								
								boolean ev = hasTransparency(n, s, e, w, u, d, x + 1, y, z);
								boolean wv = hasTransparency(n, s, e, w, u, d, x - 1, y, z);
								boolean uv = hasTransparency(n, s, e, w, u, d, x, y + 1, z);
								boolean dv = hasTransparency(n, s, e, w, u, d, x, y - 1, z);
								boolean sv = hasTransparency(n, s, e, w, u, d, x, y, z + 1);
								boolean nv = hasTransparency(n, s, e, w, u, d, x, y, z - 1);
								
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
						int storageIdx = storageIdx(x, y, z);
						int idx = blocks[storageIdx] & 0xFFFF;
						
						if(idx != 0 && translucentIndices.contains(storageIdx))
						{
							BlockModel model = DefaultRegistries.BLOCK_MODELS.getById(DefaultRegistries.BLOCKS.getByIdx(idx).getId());
							
							boolean ev = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x + 1, y, z);
							boolean wv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x - 1, y, z);
							boolean uv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y + 1, z);
							boolean dv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y - 1, z);
							boolean sv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z + 1);
							boolean nv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z - 1);
							
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
	
	private boolean hasTransparency(Chunk n, Chunk s, Chunk e, Chunk w, Chunk u, Chunk d, int x, int y, int z)
	{
		return (x < 0 && (w == null || hasTransparency(w.blocks[storageIdx(x + 32, y, z)], w.getPos(), x, y, z)))
				|| (x >= 32 && (e == null || hasTransparency(e.blocks[storageIdx(x - 32, y, z)], e.getPos(), x, y, z)))
				|| (y < 0 && (d == null || hasTransparency(d.blocks[storageIdx(x, y + 32, z)], d.getPos(), x, y, z)))
				|| (y >= 32 && (u == null || hasTransparency(u.blocks[storageIdx(x, y - 32, z)], u.getPos(), x, y, z)))
				|| (z < 0 && (n == null || hasTransparency(n.blocks[storageIdx(x, y, z + 32)], n.getPos(), x, y, z)))
				|| (z >= 32 && (s == null || hasTransparency(s.blocks[storageIdx(x, y, z - 32)], s.getPos(), x, y, z)))
				|| (x >= 0 && x < 32 && y >= 0 && y < 32 && z >= 0 && z < 32 && hasTransparency(blocks[storageIdx(x, y, z)], getPos(), x, y, z));
	}
	
	/*
	private boolean isTranslucent(Chunk n, Chunk s, Chunk e, Chunk w, Chunk u, Chunk d, int x, int y, int z)
	{
		return (x < 0 && (w == null || isTranslucent(w.blocks[storageIdx(x + 32, y, z)], w.getPos(), x, y, z)))
				|| (x >= 32 && (e == null || isTranslucent(e.blocks[storageIdx(x - 32, y, z)], e.getPos(), x, y, z)))
				|| (y < 0 && (d == null || isTranslucent(d.blocks[storageIdx(x, y + 32, z)], d.getPos(), x, y, z)))
				|| (y >= 32 && (u == null || isTranslucent(u.blocks[storageIdx(x, y - 32, z)], u.getPos(), x, y, z)))
				|| (z < 0 && (n == null || isTranslucent(n.blocks[storageIdx(x, y, z + 32)], n.getPos(), x, y, z)))
				|| (z >= 32 && (s == null || isTranslucent(s.blocks[storageIdx(x, y, z - 32)], s.getPos(), x, y, z)))
				|| (x >= 0 && x < 32 && y >= 0 && y < 32 && z >= 0 && z < 32 && isTranslucent(blocks[storageIdx(x, y, z)], getPos(), x, y, z));
	}
	 */
	
	private boolean isTranslucentNeighborTransparent(int block, Chunk n, Chunk s, Chunk e, Chunk w, Chunk u, Chunk d, int x, int y, int z)
	{
		return (x < 0 && (w == null || isTranslucentFaceVisible(block, w.blocks[storageIdx(x + 32, y, z)], w.getPos(), x, y, z)))
				|| (x >= 32 && (e == null || isTranslucentFaceVisible(block, e.blocks[storageIdx(x - 32, y, z)], e.getPos(), x, y, z)))
				|| (y < 0 && (d == null || isTranslucentFaceVisible(block, d.blocks[storageIdx(x, y + 32, z)], d.getPos(), x, y, z)))
				|| (y >= 32 && (u == null || isTranslucentFaceVisible(block, u.blocks[storageIdx(x, y - 32, z)], u.getPos(), x, y, z)))
				|| (z < 0 && (n == null || isTranslucentFaceVisible(block, n.blocks[storageIdx(x, y, z + 32)], n.getPos(), x, y, z)))
				|| (z >= 32 && (s == null || isTranslucentFaceVisible(block, s.blocks[storageIdx(x, y, z - 32)], s.getPos(), x, y, z)))
				|| (x >= 0 && x < 32 && y >= 0 && y < 32 && z >= 0 && z < 32 && isTranslucentFaceVisible(block, blocks[storageIdx(x, y, z)], getPos(), x, y, z));
	}
	
	private boolean hasTransparency(int idx, IChunkPosition chunk, int x, int y, int z)
	{
		if(idx == 0) return true;
		Block block = DefaultRegistries.BLOCKS.getByIdx(idx);
		return block instanceof ICutoutBlock || (block instanceof ITranslucentBlock b && b.hasTransparency(world, chunk.worldX() + x, chunk.worldY() + y, chunk.worldZ() + z));
	}
	
	private boolean isTranslucent(int idx, IChunkPosition chunk, int x, int y, int z)
	{
		return idx != 0 && (DefaultRegistries.BLOCKS.getByIdx(idx) instanceof ITranslucentBlock b && b.hasTransparency(world, chunk.worldX() + x, chunk.worldY() + y, chunk.worldZ() + z));
	}
	
	private boolean isTranslucentFaceVisible(int block, int neighbor, IChunkPosition neighborChunk, int nx, int ny, int nz)
	{
		if(neighbor == 0) return true;
		
		Block b = DefaultRegistries.BLOCKS.getByIdx(neighbor);
		
		if(b instanceof ICutoutBlock) return true;
		if(b instanceof ITranslucentBlock tl) return block != neighbor || !tl.blocksAdjacentFaces(world, neighborChunk.worldX() + nx, neighborChunk.worldY() + ny, neighborChunk.worldZ() + nz);
		
		return false;
	}
	
	public IRenderable solid()
	{
		return solid;
	}
	
	public IRenderable translucent()
	{
		return translucent;
	}
	
	public int blockCount()
	{
		return blockCount;
	}
	
	public boolean empty()
	{
		return blockCount == 0;
	}
	
	public Vector3dc getCenter()
	{
		return center;
	}
	
	public boolean testFrustum(FrustumIntersection frustum)
	{
		return frustum.testAab(minX + offset.x, minY + offset.y, minZ + offset.z, maxX + offset.x + 1.0F, maxY + offset.y + 1.0F, maxZ + offset.z + 1.0F);
	}
	
	void destroy()
	{
		if(renderer != null) renderer.destroy();
	}
}
