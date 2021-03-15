package info.kuonteje.voxeltest.world;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.objects.BlockModels;
import info.kuonteje.voxeltest.render.ChunkRenderable;
import info.kuonteje.voxeltest.render.ChunkRenderer;
import info.kuonteje.voxeltest.render.Renderable;
import info.kuonteje.voxeltest.render.block.BlockModel;
import info.kuonteje.voxeltest.render.block.RenderType;
import info.kuonteje.voxeltest.util.ConcurrentLazy;
import info.kuonteje.voxeltest.util.ConcurrentTimer;
import info.kuonteje.voxeltest.util.DoubleFrustum;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class Chunk implements IChunk
{
	private static class Data
	{
		private short[] blocks = new short[32 * 32 * 32];
		private BitSet opacity = new BitSet(32 * 32 * 32);
	}
	
	public static final ConcurrentTimer meshTimer = new ConcurrentTimer();
	
	private final World world;
	private final ChunkPosition pos;
	
	private final Vector3i offset;
	private final Vector3dc center;
	
	private ChunkRenderer renderer = null;
	
	private final ChunkRenderable solid, translucent;
	
	private volatile boolean dirty = false;
	
	private int minX = 32, maxX = -1;
	private int minY = 32, maxY = -1;
	private int minZ = 32, maxZ = -1;
	
	private int blockCount = 0;
	private final Object2ObjectMap<RenderType, AtomicInteger> blockRenderTypes = new Object2ObjectOpenHashMap<>();
	
	private final ConcurrentLazy<Data> data = ConcurrentLazy.of(Data::new);
	
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
		
		this.offset = new Vector3i(pos.worldX(), pos.worldY(), pos.worldZ());
		this.center = new Vector3d(offset.x + 16.0, offset.y + 16.0, offset.z + 16.0);
	}
	
	@Override
	public World world()
	{
		return world;
	}
	
	@Override
	public ChunkPosition pos()
	{
		return pos;
	}
	
	public long chunkSeed()
	{
		return pos.chunkSeed(world.seed());
	}
	
	public long columnSeed()
	{
		return pos.columnSeed(world.seed());
	}
	
	@Override
	public int blockIdxAtInternal(int x, int y, int z)
	{
		return data.got() ? data.get().blocks[storageIdx(x, y, z)] : 0;
	}
	
	@Override
	public Optional<Block> blockAt(int x, int y, int z)
	{
		return DefaultRegistries.BLOCKS.byIdx(blockIdxAt(x, y, z));
	}
	
	public void setDirty()
	{
		dirty = true;
	}
	
	@Override
	public void setBlockIdx(int x, int y, int z, int idx, int flags, BlockPredicate predicate)
	{
		setBlock(x, y, z, DefaultRegistries.BLOCKS.byIdxRaw(idx), flags, predicate);
	}
	
	@Override
	public void setBlock(int x, int y, int z, Block block, int flags, BlockPredicate predicate)
	{
		int idx = block == null ? 0 : block.idx();
		
		if(idx == 0 && !data.got()) return;
		
		int storageIdx = storageIdx(x, y, z);
		
		Data data = this.data.get();
		
		int existing = data.blocks[storageIdx];
		
		if(existing == idx) return;
		
		int wx = pos.worldX() + x;
		int wy = pos.worldY() + y;
		int wz = pos.worldZ() + z;
		
		if(predicate != null && !predicate.test(world, existing, wx, wy, wz)) return;
		
		RenderType oldRenderType = existing == 0 ? RenderType.NONE : DefaultRegistries.BLOCKS.byIdxRaw(existing).renderType();
		
		if(idx != 0)
		{
			RenderType renderType = block.renderType();
			
			if(existing == 0)
			{
				if(x < minX) minX = x;
				if(x > maxX) maxX = x;
				
				if(y < minY) minY = y;
				if(y > maxY) maxY = y;
				
				if(z < minZ) minZ = z;
				if(z > maxZ) maxZ = z;
				
				blockCount++;
				
				blockRenderTypes.computeIfAbsent(renderType, k -> new AtomicInteger(0)).getAndIncrement();
			}
			else if(renderType != oldRenderType)
			{
				blockRenderTypes.get(oldRenderType).getAndDecrement();
				blockRenderTypes.computeIfAbsent(renderType, k -> new AtomicInteger(0)).getAndIncrement();
			}
		}
		else
		{
			blockCount--; // eventually I'll fix aabbs
			
			blockRenderTypes.get(oldRenderType).getAndDecrement();
		}
		
		if(idx > DefaultRegistries.BLOCKS.maxIdx())
		{
			data.blocks[storageIdx] = 0;
			data.opacity.set(storageIdx);
		}
		else
		{
			data.blocks[storageIdx] = (short)idx;
			data.opacity.set(storageIdx, block != null && block.renderType().isOpaque());
		}
		
		if((flags & SetFlags.NO_UPDATE) == 0)
		{
			if(idx != 0) block.onPlaced(world, wx, wy, wz);
			world.updateAround(wx, wy, wz);
		}
		
		setDirty();
	}
	
	@Override
	public boolean hasTransparency(int x, int y, int z)
	{
		return !data.got() || !data.get().opacity.get(storageIdx(x, y, z));
	}
	
	void tick()
	{
		if(dirty && renderer != null)
		{
			VoxelTest.threadPool().execute(() -> meshTimer.time(() -> regenMesh()));
			dirty = false;
		}
	}
	
	private void regenMesh()
	{
		if(!data.got()) return;
		
		short[] blocks = data.get().blocks;
		
		IChunk e = world.loadedChunkAt(pos.x() + 1, pos.y(), pos.z());
		IChunk w = world.loadedChunkAt(pos.x() - 1, pos.y(), pos.z());
		IChunk u = world.loadedChunkAt(pos.x(), pos.y() + 1, pos.z());
		IChunk d = world.loadedChunkAt(pos.x(), pos.y() - 1, pos.z());
		IChunk s = world.loadedChunkAt(pos.x(), pos.y(), pos.z() + 1);
		IChunk n = world.loadedChunkAt(pos.x(), pos.y(), pos.z() - 1);
		
		// TODO is this worth the tiny speedup?
		AtomicInteger unrenderedBlockCounter = blockRenderTypes.get(RenderType.NONE);
		AtomicInteger solidBlockCounter = blockRenderTypes.get(RenderType.SOLID);
		AtomicInteger cutoutBlockCounter = blockRenderTypes.get(RenderType.CUTOUT);
		AtomicInteger translucentBlockCounter = blockRenderTypes.get(RenderType.TRANSLUCENT);
		
		int unrenderedBlocks = unrenderedBlockCounter == null ? 0 : unrenderedBlockCounter.get();
		int solidAndCutoutBlocks = (solidBlockCounter == null ? 0 : solidBlockCounter.get()) + (cutoutBlockCounter == null ? 0 : cutoutBlockCounter.get());
		int translucentBlocks = translucentBlockCounter == null ? 0 : translucentBlockCounter.get();
		
		final IntSet translucentIndices = (unrenderedBlocks == 0 && translucentBlocks == 0) ? null : new IntAVLTreeSet();
		
		int solidFaces = 0, translucentFaces = 0;
		
		for(int x = 0; x < 32; x++)
		{
			for(int z = 0; z < 32; z++)
			{
				for(int y = 0; y < 32; y++)
				{
					int storageIdx = storageIdx(x, y, z);
					int idx = blocks[storageIdx];
					
					if(idx != 0)
					{
						if(solidAndCutoutBlocks == 0 || isTranslucent(idx, pos(), x, y, z))
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
			FloatBuffer vertexBuf = renderer.vertexBuffer();
			FloatBuffer texCoordBuf = renderer.texCoordBuffer();
			IntBuffer texLayerBuf = renderer.textureLayerBuffer();
			ByteBuffer tintBuf = renderer.tintBuffer();
			
			if(solidAndCutoutBlocks > 0)
			{
				for(int x = 0; x < 32; x++)
				{
					for(int z = 0; z < 32; z++)
					{
						for(int y = 0; y < 32; y++)
						{
							int storageIdx = storageIdx(x, y, z);
							int idx = blocks[storageIdx];
							
							if(idx != 0 && ((unrenderedBlocks == 0 && translucentBlocks == 0) || !translucentIndices.contains(storageIdx)))
							{
								boolean ev = hasTransparency(n, s, e, w, u, d, x + 1, y, z);
								boolean wv = hasTransparency(n, s, e, w, u, d, x - 1, y, z);
								boolean uv = hasTransparency(n, s, e, w, u, d, x, y + 1, z);
								boolean dv = hasTransparency(n, s, e, w, u, d, x, y - 1, z);
								boolean sv = hasTransparency(n, s, e, w, u, d, x, y, z + 1);
								boolean nv = hasTransparency(n, s, e, w, u, d, x, y, z - 1);
								
								if(ev || wv || uv || dv || sv || nv)
								{
									BlockModel model = BlockModels.getCachedModel(idx);
									
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
			
			if(translucentBlocks > 0)
			{
				for(int x = 0; x < 32; x++)
				{
					for(int z = 0; z < 32; z++)
					{
						for(int y = 0; y < 32; y++)
						{
							int storageIdx = storageIdx(x, y, z);
							int idx = blocks[storageIdx];
							
							if(idx != 0 && ((unrenderedBlocks == 0 && solidAndCutoutBlocks == 0) || translucentIndices.contains(storageIdx)))
							{
								boolean ev = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x + 1, y, z);
								boolean wv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x - 1, y, z);
								boolean uv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y + 1, z);
								boolean dv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y - 1, z);
								boolean sv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z + 1);
								boolean nv = isTranslucentNeighborTransparent(idx, n, s, e, w, u, d, x, y, z - 1);
								
								if(ev || wv || uv || dv || sv || nv)
								{
									BlockModel model = BlockModels.getCachedModel(idx);
									
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
		if(x < 0) return isTranslucentFaceVisible(block, w.blockIdxAtInternal(x + 32, y, z), w.pos(), x, y, z);
		else if(x >= 32) return isTranslucentFaceVisible(block, e.blockIdxAtInternal(x - 32, y, z), e.pos(), x, y, z);
		else if(y < 0) return isTranslucentFaceVisible(block, d.blockIdxAtInternal(x, y + 32, z), d.pos(), x, y, z);
		else if(y >= 32) return isTranslucentFaceVisible(block, u.blockIdxAtInternal(x, y - 32, z), u.pos(), x, y, z);
		else if(z < 0) return isTranslucentFaceVisible(block, n.blockIdxAtInternal(x, y, z + 32), n.pos(), x, y, z);
		else if(z >= 32) return isTranslucentFaceVisible(block, s.blockIdxAtInternal(x, y, z - 32), s.pos(), x, y, z);
		else return isTranslucentFaceVisible(block, data.get().blocks[storageIdx(x, y, z)], pos(), x, y, z);
	}
	
	private boolean isTranslucent(int idx, ChunkPosition chunk, int x, int y, int z)
	{
		return idx != 0 && DefaultRegistries.BLOCKS.byIdxRaw(idx).renderType() == RenderType.TRANSLUCENT;
	}
	
	private boolean isTranslucentFaceVisible(int block, int neighbor, ChunkPosition neighborChunk, int nx, int ny, int nz)
	{
		return neighbor == 0 || switch(DefaultRegistries.BLOCKS.byIdxRaw(neighbor).renderType()) {
		case CUTOUT -> true;
		case TRANSLUCENT -> block != neighbor;
		default -> false;
		};
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
	
	public Vector3dc center()
	{
		return center;
	}
	
	public boolean testFrustum(DoubleFrustum frustum)
	{
		return frustum.testAab(minX + offset.x, minY + offset.y, minZ + offset.z, maxX + offset.x + 1.0, maxY + offset.y + 1.0, maxZ + offset.z + 1.0);
	}
	
	void destroy()
	{
		if(renderer != null) renderer.destroy();
	}
}
