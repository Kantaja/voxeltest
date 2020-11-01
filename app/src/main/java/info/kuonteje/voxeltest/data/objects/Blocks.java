package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.block.TransparentBlock;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;

public class Blocks
{
	public static final Registry<Block> REGISTRY = DefaultRegistries.BLOCKS;
	
	public static final Block STONE = REGISTRY.register(new Block("stone"));
	public static final Block DIRT = REGISTRY.register(new Block("dirt"));
	public static final Block GRASS = REGISTRY.register(new Block("grass"));
	public static final TransparentBlock WATER = REGISTRY.register(new TransparentBlock("water"));
	public static final Block SAND = REGISTRY.register(new Block("sand"));
	public static final TransparentBlock GLASS = REGISTRY.register(new TransparentBlock("glass"));
	
	public static void init()
	{
		// static init
	}
}
