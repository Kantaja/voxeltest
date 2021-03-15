package info.kuonteje.voxeltest.world.worldgen;

import java.util.List;
import java.util.stream.Collectors;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.console.Cvar;
import info.kuonteje.voxeltest.console.CvarString;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.objects.Blocks;
import info.kuonteje.voxeltest.data.objects.Features;
import info.kuonteje.voxeltest.data.objects.GenerationStages;
import info.kuonteje.voxeltest.data.objects.WorldgenProfiles;
import info.kuonteje.voxeltest.util.Pair;
import info.kuonteje.voxeltest.world.Chunk;
import info.kuonteje.voxeltest.world.ChunkPosition;
import info.kuonteje.voxeltest.world.IChunkProvider;
import info.kuonteje.voxeltest.world.World;
import info.kuonteje.voxeltest.world.worldgen.config.data.FeatureConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.GenerationStageConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.GeneratorConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.TerrainConfig;
import info.kuonteje.voxeltest.world.worldgen.config.data.WaterConfig;
import info.kuonteje.voxeltest.world.worldgen.feature.generator.BlockReplacementFeatureGenerator;
import info.kuonteje.voxeltest.world.worldgen.feature.generator.TreeFeatureGenerator;
import info.kuonteje.voxeltest.world.worldgen.stage.IWorldGenerator;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.CellularCaveGenerator;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.ChunkDecorator;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.DefaultBaseGenerator;
import info.kuonteje.voxeltest.world.worldgen.stage.generator.GradientCaveGenerator;

public class GeneratingChunkProvider implements IChunkProvider
{
	public static final CvarString svWorldgenProfile = VoxelTest.CONSOLE.cvars().cvarString("sv_worldgen_profile", "voxeltest:default", Cvar.Flags.CONFIG | Cvar.Flags.LATCH);
	
	public static final GeneratorConfig DEFAULT_ROOT_CONFIG = GeneratorConfig.builder()
			.stages(List.of(
					GenerationStageConfig.builder().stage(GenerationStages.DEFAULT_BASE).config(DefaultBaseGenerator.DEFAULT_CONFIG).build(),
					GenerationStageConfig.builder().stage(GenerationStages.DEFAULT_WATER).build(),
					GenerationStageConfig.builder().stage(GenerationStages.GRADIENT_CAVES).config(GradientCaveGenerator.DEFAULT_CONFIG).build(),
					GenerationStageConfig.builder().stage(GenerationStages.CELLULAR_CAVES).enabled(false).config(CellularCaveGenerator.DEFAULT_CONFIG).build(),
					GenerationStageConfig.builder().stage(GenerationStages.DECORATOR).config(ChunkDecorator.Config.builder()
							.features(List.of(
									FeatureConfig.builder().feature(Features.TREE).config(TreeFeatureGenerator.DEFAULT_CONFIG).build(),
									FeatureConfig.builder().feature(Features.SINGLE_BLOCK_REPLACEMENT).config(BlockReplacementFeatureGenerator.EMERALD_ORE_CONFIG).build()
									))
							.build()).build()
					))
			.terrain(TerrainConfig.builder()
					.base(Blocks.STONE)
					.filler(Blocks.DIRT)
					.top(Blocks.GRASS)
					.build())
			.water(WaterConfig.builder()
					.liquid(Blocks.WATER)
					.liquidTop(Blocks.SAND)
					.seaLevel(0)
					.build())
			.build();
	
	private final World world;
	
	private final List<Pair<EntryId, IWorldGenerator>> stages;
	
	public GeneratingChunkProvider(World world)
	{
		this.world = world;
		
		EntryId profileId = EntryId.create(svWorldgenProfile.get());
		
		GeneratorConfig config = DefaultRegistries.WORLDGEN_PROFILES.byId(profileId).orElse(WorldgenProfiles.DEFAULT).config();
		long seed = world.seed();
		
		System.out.println("Generator profile: " + profileId.toString());
		System.out.println("Generator seed: " + seed);
		
		this.stages = config.stages().stream()
				.filter(GenerationStageConfig::enabled)
				.map(s -> Pair.of(
						s.stage().id(),
						s.stage().createGenerator(config, s.config(), seed)
						))
				.collect(Collectors.toList());
	}
	
	@Override
	public Chunk getChunk(ChunkPosition pos)
	{
		Chunk chunk = new Chunk(world, pos);
		
		stages.forEach(p ->
		{
			try
			{
				p.right().processChunk(chunk);
			}
			catch(Exception e)
			{
				new RuntimeException("Failed to run generation stage " + p.left().toString() + " for chunk " + pos.toString(), e).printStackTrace();
			}
		});
		
		return chunk;
	}
}
