package info.kuonteje.voxeltest.data.objects;

import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.Registry;
import info.kuonteje.voxeltest.world.worldgen.stage.GenerationStage;
import info.kuonteje.voxeltest.world.worldgen.stage.NoGenerationStageConfig;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.CellularCaveGenerator;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.ChunkDecorator;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.DefaultBaseGenerator;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.DefaultChunkWaterer;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.GradientCaveGenerator;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.HeightmapBaseGenerator;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.OldBaseGenerator;

public class GenerationStages
{
	public static final Registry<GenerationStage<?>> REGISTRY = DefaultRegistries.GENERATION_STAGES;
	
	public static final GenerationStage<DefaultBaseGenerator.Config> DEFAULT_BASE = REGISTRY.register(new GenerationStage<>("default_base", DefaultBaseGenerator.Config.class, DefaultBaseGenerator.DEFAULT_CONFIG, DefaultBaseGenerator::factory));
	public static final GenerationStage<HeightmapBaseGenerator.Config> HEIGHTMAP_BASE = REGISTRY.register(new GenerationStage<>("heightmap_base", HeightmapBaseGenerator.Config.class, HeightmapBaseGenerator.DEFAULT_CONFIG, HeightmapBaseGenerator::factory));
	public static final GenerationStage<OldBaseGenerator.Config> OLD_BASE = REGISTRY.register(new GenerationStage<>("old_base", OldBaseGenerator.Config.class, OldBaseGenerator.DEFAULT_CONFIG, OldBaseGenerator::factory));
	public static final GenerationStage<NoGenerationStageConfig> DEFAULT_WATER = REGISTRY.register(new GenerationStage<>("default_water", NoGenerationStageConfig.class, GenerationStage.NO_CONFIG, DefaultChunkWaterer::factory));
	public static final GenerationStage<CellularCaveGenerator.Config> CELLULAR_CAVES = REGISTRY.register(new GenerationStage<>("cellular_caves", CellularCaveGenerator.Config.class, CellularCaveGenerator.DEFAULT_CONFIG, CellularCaveGenerator::factory));
	public static final GenerationStage<GradientCaveGenerator.Config> GRADIENT_CAVES = REGISTRY.register(new GenerationStage<>("gradient_caves", GradientCaveGenerator.Config.class, GradientCaveGenerator.DEFAULT_CONFIG, GradientCaveGenerator::factory));
	public static final GenerationStage<ChunkDecorator.Config> DECORATOR = REGISTRY.register(new GenerationStage<>("decorator", ChunkDecorator.Config.class, null, ChunkDecorator::factory));
	
	public static void init()
	{
		// static init
	}
}
