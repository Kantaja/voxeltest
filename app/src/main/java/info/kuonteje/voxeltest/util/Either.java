package info.kuonteje.voxeltest.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Either<L, R>
{
	private Either() {}
	
	public final boolean isLeft()
	{
		return this instanceof Left;
	}
	
	public final boolean isRight()
	{
		return this instanceof Right;
	}
	
	public abstract Optional<L> getLeft();
	public abstract Optional<R> getRight();
	
	public void match(Consumer<L> left, Consumer<R> right)
	{
		if(isLeft()) left.accept(getLeft().get());
		else right.accept(getRight().get());
	}
	
	public Either<L, R> mapToLeft(Function<R, L> func)
	{
		return isLeft() ? this : left(func.apply(getRight().get()));
	}
	
	public Either<L, R> mapToRight(Function<L, R> func)
	{
		return isRight() ? this : right(func.apply(getLeft().get()));
	}
	
	public static <L, R> Either<L, R> left(L value)
	{
		return new Left<>(value);
	}
	
	public static <L, R> Either<L, R> right(R value)
	{
		return new Right<>(value);
	}
	
	private static final class Left<L, R> extends Either<L, R>
	{
		private Optional<L> value;
		
		private Left(L value)
		{
			this.value = Optional.of(value);
		}
		
		@Override
		public Optional<L> getLeft()
		{
			return value;
		}
		
		@Override
		public Optional<R> getRight()
		{
			return Optional.empty();
		}
	}
	
	private static final class Right<L, R> extends Either<L, R>
	{
		private Optional<R> value;
		
		private Right(R value)
		{
			this.value = Optional.of(value);
		}
		
		@Override
		public Optional<L> getLeft()
		{
			return Optional.empty();
		}
		
		@Override
		public Optional<R> getRight()
		{
			return value;
		}
	}
}
