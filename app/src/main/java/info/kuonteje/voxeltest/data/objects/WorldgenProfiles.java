package info.kuonteje.voxeltest.data.objects;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import info.kuonteje.voxeltest.VoxelTest;
import info.kuonteje.voxeltest.assets.AssetLoader;
import info.kuonteje.voxeltest.data.DefaultRegistries;
import info.kuonteje.voxeltest.data.EntryId;
import info.kuonteje.voxeltest.data.Registry;
import info.kuonteje.voxeltest.world.worldgen.config.WorldgenProfile;
import info.kuonteje.voxeltest.world.worldgen.config.data.GeneratorConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

public class WorldgenProfiles
{
	public static final Registry<WorldgenProfile> REGISTRY = DefaultRegistries.WORLDGEN_PROFILES;
	
	static
	{
		REGISTRY.createChildDataMap(WorldgenProfile.OVERRIDES_ID, GeneratorConfig.class);
	}
	
	public static final WorldgenProfile DEFAULT = REGISTRY.defaultValue();
	
	//
	
	public static void init()
	{
		REGISTRY.addFreezeCallback(r ->
		{
			Object2ObjectMap<EntryId, GeneratorConfig> overrides = r.childDataMap(WorldgenProfile.OVERRIDES_ID, GeneratorConfig.class);
			
			Path configPath = VoxelTest.CFG_PATH.resolve("worldgen");
			
			for(WorldgenProfile profile : r)
			{
				if(overrides.containsKey(profile.id())) continue;
				
				System.out.println("No override registered for worldgen profile " + profile.id());
				
				Path overridePath = configPath.resolve(profile.id().getDomain() + "/" + profile.id().getId() + ".json");
				
				try
				{
					Files.createDirectories(overridePath.getParent());
				}
				catch(IOException e)
				{
					e.printStackTrace(); // ?
				}
				
				if(Files.exists(overridePath))
				{
					try(InputStream in = Files.newInputStream(overridePath))
					{
						GeneratorConfig override = VoxelTest.OBJECT_MAPPER.readValue(in, GeneratorConfig.class);
						overrides.put(profile.id(), override);
					}
					catch(IOException e)
					{
						new RuntimeException("Failed to load WorldgenProfile override " + profile.id().toString(), e).printStackTrace();
					}
				}
				else
				{
					try(InputStream in = AssetLoader.assetStream(AssetTypes.WORLDGEN_PROFILE, profile.assetId()))
					{
						try
						{
							Files.copy(in, overridePath);
						}
						catch(IOException e)
						{
							new RuntimeException("Failed to create WorldgenProfile override " + profile.id().toString(), e).printStackTrace();
						}
					}
					catch(IOException e)
					{
						new RuntimeException("Failed to load asset \"" + profile.assetId().toString() + "\" of type " + AssetTypes.WORLDGEN_PROFILE.id().toString(), e).printStackTrace();
					}
				}
			}
		});
	}
	
	public static void addConfigOverride(EntryId id, GeneratorConfig config)
	{
		REGISTRY.childDataMap(WorldgenProfile.OVERRIDES_ID, GeneratorConfig.class).put(id, config);
	}
	
	public static GeneratorConfig overrideFor(EntryId id)
	{
		return REGISTRY.childDataMap(WorldgenProfile.OVERRIDES_ID, GeneratorConfig.class).get(id);
	}
}
