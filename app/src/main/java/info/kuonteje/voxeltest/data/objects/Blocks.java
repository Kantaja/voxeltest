package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.block.BasicTranslucentBlock;
import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.block.LeavesBlock;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;

public class Blocks
{
	public static final Registry<Block> REGISTRY = DefaultRegistries.BLOCKS;
	
	public static final Block STONE = REGISTRY.register(new Block("stone"));
	public static final Block DIRT = REGISTRY.register(new Block("dirt"));
	public static final Block GRASS = REGISTRY.register(new Block("grass"));
	public static final BasicTranslucentBlock WATER = REGISTRY.register(new BasicTranslucentBlock("water"));
	public static final Block SAND = REGISTRY.register(new Block("sand"));
	public static final BasicTranslucentBlock GLASS = REGISTRY.register(new BasicTranslucentBlock("glass"));
	public static final Block LOG = REGISTRY.register(new Block("log"));
	public static final LeavesBlock LEAVES = REGISTRY.register(new LeavesBlock("leaves"));
	
	public static void init()
	{
		// static init
	}
}
