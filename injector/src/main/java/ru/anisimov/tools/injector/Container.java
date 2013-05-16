package ru.anisimov.tools.injector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Container {
	private Map<TServiceKey, ContainerFactory<?>> factories = new HashMap<>();
	
	public <TService> void register(Class<TService> type, ContainerFactory<TService> factory, Class<?>... args) {
		TServiceKey key = new TServiceKey(type, args);
		factories.put(key, factory);
	}
	
	@SuppressWarnings("unchecked")
	public <TService> TService resolve(Class<TService> type, Object... args) {
		ContainerFactory<?> factory = factories.get(TServiceKey.valueOf(type, args));
		if (factory != null) {
			return (TService) factory.newInstance(args);
		}
		return null;
	}
	
	static class TServiceKey {
		private Class<?> resultType;
		private Class<?>[] argTypes;
		
		public TServiceKey(Class<?> resultType, Class<?>... args) {
			this.resultType = resultType;
			this.argTypes = args;
		}
		
		public static TServiceKey valueOf(Class<?> type, Object... args) {
			Class<?>[] argTypes = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
			}
			return new TServiceKey(type, argTypes);
		}
		
		public Class<?> getResultType() {
			return resultType;
		}
		
		public Class<?>[] getArgTypes() {
			return argTypes;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TServiceKey)) {
				return false;
			}
			TServiceKey tObj = (TServiceKey) obj;
			return Objects.equals(getResultType(), tObj.getResultType()) && Arrays.equals(getArgTypes(), tObj.getArgTypes());
		}
		
		@Override
		public int hashCode() {
			int p = 17, q = 31;
			int result = p;
			result = result * q + resultType.hashCode();
			result = result * q + Arrays.hashCode(argTypes);
			return result;
		}
	}
}