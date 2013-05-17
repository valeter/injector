package ru.anisimov.tools.injector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Container {
	private Map<TServiceKey, Factory<?>> factories = new HashMap<>();
	
	private Container() {}
	
	public <TService> TService resolve(Class<TService> type, Object... args) {
		return resolveNamed(null, type, args);
	}
	
	@SuppressWarnings("unchecked")
	public <TService> TService resolveNamed(String name, Class<TService> type, Object... args) {
		TServiceKey key = TServiceKey.valueOf(type, args);
		key.setName(name);
		Factory<?> factory = factories.get(key);
		return (TService) factory.newInstance(args);
	}
	
	public static class Builder {
		private List<Registration> registrations = new ArrayList<>();
		
		private Builder() {}
		
		public static Builder newInstance() {
			return new Builder();
		}
		
		public Container build() {
			Container container = new Container();
			for (Registration registration: registrations) {
				TServiceKey key = new TServiceKey(registration.getServiceType(), 
						registration.getArgs());
				key.setName(registration.getName());
				container.factories.put(key, registration.getFactory());
			}
			return container;
		}
		
		public <TService> Registration register(Class<TService> type, Factory<TService> factory, Class<?>... args) {
			Registration registration = new Registration(type, factory, args);
			registrations.add(registration);
			return registration;
		}
	}
	
	static class TServiceKey {
		private Class<?> resultType;
		private Class<?>[] argTypes;
		private String name;
		
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
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
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
			return Objects.equals(getResultType(), tObj.getResultType()) && 
					Arrays.equals(getArgTypes(), tObj.getArgTypes()) &&
					Objects.equals(getName(), tObj.getName());
		}
		
		@Override
		public int hashCode() {
			int p = 17, q = 31;
			int result = p;
			result = result * q + getResultType().hashCode();
			result = result * q + Arrays.hashCode(getArgTypes());
			if (getName() != null)
				result = result * q + getName().hashCode();
			return result;
		}
	}
}