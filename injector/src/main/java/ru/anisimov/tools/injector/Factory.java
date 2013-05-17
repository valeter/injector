package ru.anisimov.tools.injector;

public interface Factory<T> {
	T newInstance(Object... args);
}