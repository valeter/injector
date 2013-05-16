package ru.anisimov.tools.injector;

import java.util.HashMap;
import java.util.Map;

public class Container {
	private Map<Class<?>, ContainerFactory<?>> factories = new HashMap<>();
	
	public <TService> void register(Class<TService> type, ContainerFactory<TService> factory) {
		factories.put(type, factory);
	}
	
	@SuppressWarnings("unchecked")
	public <TService> TService resolve(Class<TService> type) {
		ContainerFactory<?> factory = factories.get(type);
		return (TService) factory.newInstance();
	}
}
