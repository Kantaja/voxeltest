package info.kuonteje.voxeltest.util.functional;

public interface ThrowingFunction<K, V, E extends Throwable>
{
	V apply(K k) throws E;
}
