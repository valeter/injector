package ru.anisimov.tools.injector;

public interface ContainerFactory<T> {
	T newInstance();
}
