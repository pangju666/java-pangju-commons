package io.github.pangju666.commons.lang.utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StreamUtils {
	protected StreamUtils() {
	}

	public static <T> List<T> toNonNullList(final Collection<T> entities) {
		return entities.stream()
				.filter(Objects::nonNull)
				.toList();
	}

	public static <T> Set<T> toNonNullSet(final Collection<T> entities) {
		return entities.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	public static <T, S> List<S> toList(final Collection<T> entities, final Function<T, S> function) {
		return entities.stream()
				.filter(Objects::nonNull)
				.map(function)
				.toList();
	}

	public static <T, S> List<S> toUniqueList(final Collection<T> entities, final Function<T, S> function) {
		return entities.stream()
				.filter(Objects::nonNull)
				.map(function)
				.distinct()
				.toList();
	}

	public static <T, S> Set<S> toSet(final Collection<T> entities, final Function<T, S> function) {
		return entities.stream()
				.filter(Objects::nonNull)
				.map(function)
				.collect(Collectors.toSet());
	}

	public static <T, S> Map<S, T> toMap(final Collection<T> entities, final Function<T, S> function) {
		return entities.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(function, entity -> entity));
	}

	public static <T, S> Map<S, List<T>> group(final Collection<T> entities, final Function<T, S> function) {
		return entities.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(function, Collectors.mapping(entity -> entity, Collectors.toList())));
	}

	public static <T, S> Map<S, Long> groupCount(final Collection<T> entities, final Function<T, S> function) {
		return entities.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(function, Collectors.counting()));
	}
}