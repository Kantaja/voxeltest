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
import info.kuonteje.voxeltest.render.Renderable;
import info.kuonteje.voxeltest.render.block.BlockModel;
import info.kuonteje.voxeltest.util.ConcurrentTimer;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public final class Chunk implements IChunk
{
	public static final ConcurrentTimer meshTimer = new ConcurrentTimer();
	
	private final World world;
	private final ChunkPosition pos;
	
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
	
	public Chunk(World world, ChunkPosition pos)
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
		this.pos = pos;
		
		this.center = new Vector3d(pos.worldX() + 16.0, pos.worldY() + 16.0, pos.worldZ() + 16.0);
		
		this.offset = new Vector3i(pos.worldX(), pos.worldY(), pos.worldZ());
	}
	
	@Override
	public World getWorld()
	{
		return world;
	}
	
	@Override
	public ChunkPosition getPos()
	{
		return pos;
	}
	
	@Override
	public int getBlockIdxInternal(int x, int y, int z)
	{
		return blocks[storageIdx(x, y, z)] & 0x7FFF;
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
		setBlock(x, y, z, DefaultRegistries.BLOCKS.getByIdx(idx));
	}
	
	@Override
	public void setBlock(int x, int y, int z, Block block)
	{
		int idx = block == null ? 0 : block.getIdx();
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
		
		blocks[storageIdx] = idx > DefaultRegistries.BLOCKS.maxIdx() ? 0
				: (short)(idx | ((block == null || block instanceof ITranslucentBlock || block instanceof ICutoutBlock) ? 0 : (1 << 15)));
		
		setDirty();
	}
	
	@Override
	public boolean hasTransparency(int x, int y, int z)
	{
		return ((blocks[storageIdx(x, y, z)] >> 15) & 0x1) == 0;
	}
	
	void tick()
	{
		if(dirty && renderer != null)
		{
			VoxelTest.getThreadPool().execute(() -> meshTimer.time(() -> regenMesh()));
			dirty = false;
		}
	}
	
	private void regenMesh()
	{
		IChunk e = world.getLoadedChunk(pos.x() + 1, pos.y(), pos.z());
		IChunk w = world.getLoadedChunk(pos.x() - 1, pos.y(), pos.z());
		IChunk u = world.getLoadedChunk(pos.x(), pos.y() + 1, pos.z());
		IChunk d = world.getLoadedChunk(pos.x(), pos.y() - 1, pos.z());
		IChunk s = world.getLoadedChunk(pos.x(), pos.y(), pos.z() + 1);
		IChunk n = world.getLoadedChunk(pos.x(), pos.y(), pos.z() - 1);
		
		final IntSet translucentIndices = new IntAVLTreeSet();
		
		int solidFaces = 0, translucentFaces = 0;
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					int storageIdx = storageIdx(x, y, z);
					int idx = blocks[storageIdx] & 0x7FFF;
					
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
							int idx = blocks[storageIdx] & 0x7FFF;
							
							if(idx != 0)
							{
								boolean ev = hasTransparency(n, s, e, w, u, d, x + 1, y, z);
								boolean wv = hasTransparency(n, s, e, w, u, d, x - 1, y, z);
								boolean uv = hasTransparency(n, s, e, w, u, d, x, y + 1, z);
								boolean dv = hasTransparency(n, s, e, w, u, d, x, y - 1, z);
								boolean sv = hasTransparency(n, s, e, w, u, d, x, y, z + 1);
								boolean nv = hasTransparency(n, s, e, w, u, d, x, y, z - 1);
								
								if(ev || wv || uv || dv || sv || nv)
								{
									BlockModel model = DefaultRegistries.BLOCK_MODELS.getById(DefaultRegistries.BLOCKS.getByIdx(idx).getId());
									
									model.getVertices(vertexBuf, x + offset.x, y + offset.y, z + offset.z, nv, sv, ev, wv, uv, dv);
									model.getTextureCoords(texCoordBuf, nv, sv, ev, wv, uv, dv);
									model.getTextureLayers(texLayerBuf, nv, sv, ev, wv, uv, dv);
									model.getTints(tintBuf, nv, sv, ev, wv, uv, dv);
								}
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
						int idx = blocks[storageIdx] & 0x7FFF;
						
						if(idx != 0 && translucentIndices.contains(storageIdx))
						{
							boolean ev = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x + 1, y, z);
							boolean wv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x - 1, y, z);
							boolean uv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y + 1, z);
							boolean dv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y - 1, z);
							boolean sv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z + 1);
							boolean nv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z - 1);
							
							if(ev || wv || uv || dv || sv || nv)
							{
								BlockModel model = DefaultRegistries.BLOCK_MODELS.getById(DefaultRegistries.BLOCKS.getByIdx(idx).getId());
								
								model.getVertices(vertexBuf, x + offset.x, y + offset.y, z + offset.z, nv, sv, ev, wv, uv, dv);
								model.getTextureCoords(texCoordBuf, nv, sv, ev, wv, uv, dv);
								model.getTextureLayers(texLayerBuf, nv, sv, ev, wv, uv, dv);
								model.getTints(tintBuf, nv, sv, ev, wv, uv, dv);
							}
						}
					}
				}
			}
			
			vertexBuf.flip();
			texCoordBuf.flip();
			texLayerBuf.flip();
			tintBuf.flip();
			
			VoxelTest.addRenderHook(renderer::loadMesh);
		}
	}
	
	private boolean hasTransparency(IChunk n, IChunk s, IChunk e, IChunk w, IChunk u, IChunk d, int x, int y, int z)
	{
		if (x < 0) return w.hasTransparency(x + 32, y, z);
		else if(x >= 32) return e.hasTransparency(x - 32, y, z);
		else if(y < 0) return d.hasTransparency(x, y + 32, z);
		else if(y >= 32) return u.hasTransparency(x, y - 32, z);
		else if(z < 0) return n.hasTransparency(x, y, z + 32);
		else if(z >= 32) return s.hasTransparency(x, y, z - 32);
		else return hasTransparency(x, y, z);
	}
	
	@SuppressWarnings("deprecation")
	private boolean isTranslucentNeighborTransparent(int block, IChunk n, IChunk s, IChunk e, IChunk w, IChunk u, IChunk d, int x, int y, int z)
	{
		if(x < 0) return isTranslucentFaceVisible(block, w.getBlockIdxInternal(x + 32, y, z), w.getPos(), x, y, z);
		else if(x >= 32) return isTranslucentFaceVisible(block, e.getBlockIdxInternal(x - 32, y, z), e.getPos(), x, y, z);
		else if(y < 0) return isTranslucentFaceVisible(block, d.getBlockIdxInternal(x, y + 32, z), d.getPos(), x, y, z);
		else if(y >= 32) return isTranslucentFaceVisible(block, u.getBlockIdxInternal(x, y - 32, z), u.getPos(), x, y, z);
		else if(z < 0) return isTranslucentFaceVisible(block, n.getBlockIdxInternal(x, y, z + 32), n.getPos(), x, y, z);
		else if(z >= 32) return isTranslucentFaceVisible(block, s.getBlockIdxInternal(x, y, z - 32), s.getPos(), x, y, z);
		else return isTranslucentFaceVisible(block, blocks[storageIdx(x, y, z)], getPos(), x, y, z);
	}
	
	private boolean isTranslucent(int idx, ChunkPosition chunk, int x, int y, int z)
	{
		return idx != 0 && DefaultRegistries.BLOCKS.getByIdx(idx) instanceof ITranslucentBlock;
	}
	
	private boolean isTranslucentFaceVisible(int block, int neighbor, ChunkPosition neighborChunk, int nx, int ny, int nz)
	{
		if(neighbor == 0) return true;
		
		Block b = DefaultRegistries.BLOCKS.getByIdx(neighbor);
		
		if(b instanceof ICutoutBlock) return true;
		if(b instanceof ITranslucentBlock) return block != neighbor;
		
		return false;
	}
	
	public Renderable solid()
	{
		return solid;
	}
	
	public Renderable translucent()
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
