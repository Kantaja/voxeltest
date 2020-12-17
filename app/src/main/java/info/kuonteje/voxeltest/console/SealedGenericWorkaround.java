package info.kuonteje.voxeltest.console;

// the keyword "non-sealed" is a travesty
// so is the fact that this is needed
abstract non-sealed class SealedGenericWorkaround extends Cvar
{
	SealedGenericWorkaround(CvarRegistry registry, String name, int flags)
	{
		super(registry, name, flags);
	}
}
