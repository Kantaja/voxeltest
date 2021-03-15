package info.kuonteje.voxeltest.data;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class RegistryManager
{
	private static final Object2ObjectMap<Class<? extends RegistryEntry<?>>, Registry<? extends RegistryEntry<?>>> masterRegistry = new Object2ObjectOpenHashMap<>();
	
	@SuppressWarnings("unchecked")
	public static <T extends RegistryEntry<T>> Registry<T> getRegistry(Class<? super T> type, T defaultValue)
	{
		return (Registry<T>)masterRegistry.computeIfAbsent((Class<T>)type, t -> new Registry<T>((Class<? super T>)t, defaultValue)); // actually horrible
	}
	
	public static <T extends RegistryEntry<T>> Registry<T> getRegistry(Class<? super T> type)
	{
		return RegistryManager.<T>getRegistry(type, null);
	}
	
	public static void freezeAll()
	{
		System.out.println("Freezing registries");
		masterRegistry.values().forEach(Registry::freeze);
	}
}
