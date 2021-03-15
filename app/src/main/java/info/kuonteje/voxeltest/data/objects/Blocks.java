package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.block.Block;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;
import info.kuonteje.voxeltest.render.block.RenderType;

public class Blocks
{
	public static final Registry<Block> REGISTRY = DefaultRegistries.BLOCKS;
	
	public static final Block STONE = REGISTRY.register(new Block("stone"));
	public static final Block DIRT = REGISTRY.register(new Block("dirt"));
	public static final Block GRASS = REGISTRY.register(new Block("grass"));
	public static final Block WATER = REGISTRY.register(new Block("water", RenderType.TRANSLUCENT));
	public static final Block SAND = REGISTRY.register(new Block("sand"));
	public static final Block GLASS = REGISTRY.register(new Block("glass", RenderType.TRANSLUCENT));
	public static final Block LOG = REGISTRY.register(new Block("log"));
	public static final Block LEAVES = REGISTRY.register(new Block("leaves", RenderType.CUTOUT));
	public static final Block EMERALD_ORE = REGISTRY.register(new Block("emerald_ore"));
	public static final Block GRAVEL = REGISTRY.register(new Block("gravel"));
	
	public static void init()
	{
		// static init
	}
}
