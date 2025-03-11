package io.github.pangju666.commons.lang.utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StreamUtils {
	protected StreamUtils() {
	}

	public static <T> List<T> toNonNullList(final Collection<T> entities) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyList();
		}
		return entities.stream()
			.filter(Objects::nonNull)
			.toList();
	}

	public static <T> Set<T> toNonNullSet(final Collection<T> entities) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptySet();
		}
		return entities.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	public static <T, S> List<S> toList(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyList();
		}
		return entities.stream()
			.filter(Objects::nonNull)
			.map(function)
			.toList();
	}

	public static <T, S> List<S> toUniqueList(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyList();
		}
		return entities.stream()
			.filter(Objects::nonNull)
			.map(function)
			.distinct()
			.toList();
	}

	public static <T, S> Set<S> toSet(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptySet();
		}
		return entities.stream()
			.filter(Objects::nonNull)
			.map(function)
			.collect(Collectors.toSet());
	}

	public static <T, S> Map<S, T> toMap(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyMap();
		}
		return entities.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(function, entity -> entity));
	}

	public static <T, S> Map<S, List<T>> group(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyMap();
		}
		return entities.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.groupingBy(function,
				Collectors.mapping(entity -> entity, Collectors.toList())));
	}

	public static <T, S> Map<S, Long> groupCount(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyMap();
		}
		return entities.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.groupingBy(function, Collectors.counting()));
	}

	public static <T> List<T> toNonNullListByParallel(final Collection<T> entities) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyList();
		}
		return entities.parallelStream()
			.filter(Objects::nonNull)
			.toList();
	}

	public static <T> Set<T> toNonNullSetByParallel(final Collection<T> entities) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptySet();
		}
		return entities.parallelStream()
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	public static <T, S> List<S> toListByParallel(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyList();
		}
		return entities.parallelStream()
			.filter(Objects::nonNull)
			.map(function)
			.toList();
	}

	public static <T, S> List<S> toUniqueListByParallel(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyList();
		}
		return entities.parallelStream()
			.filter(Objects::nonNull)
			.map(function)
			.distinct()
			.toList();
	}

	public static <T, S> Set<S> toSetByParallel(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptySet();
		}
		return entities.parallelStream()
			.filter(Objects::nonNull)
			.map(function)
			.collect(Collectors.toSet());
	}

	public static <T, S> Map<S, T> toMapByParallel(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyMap();
		}
		return entities.parallelStream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(function, entity -> entity));
	}

	public static <T, S> Map<S, List<T>> groupByParallel(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyMap();
		}
		return entities.parallelStream()
			.filter(Objects::nonNull)
			.collect(Collectors.groupingBy(function,
				Collectors.mapping(entity -> entity, Collectors.toList())));
	}

	public static <T, S> Map<S, Long> groupCountByParallel(final Collection<T> entities, final Function<T, S> function) {
		if (CollectionUtils.isEmpty(entities)) {
			return Collections.emptyMap();
		}
		return entities.parallelStream()
			.filter(Objects::nonNull)
			.collect(Collectors.groupingBy(function, Collectors.counting()));
	}
}