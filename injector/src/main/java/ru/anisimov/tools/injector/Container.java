package ru.anisimov.tools.injector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ru.anisimov.tools.injector.exceptions.ResolutionException;
import ru.anisimov.tools.injector.syntax.registration.RegistrationInterface;

public class Container {
	private Map<ServiceKey, ServiceEntry> services = new HashMap<>();
	private Container parent;
	
	private Container() { this.parent = null; }
	
	private Container(Container parent) { this.parent = parent; }
	
	public Container createChildContainer() {
		return new Container(this);
	}
	
	private void register(ServiceKey key, ServiceEntry entry) {
		services.put(key, entry);
	}
	
	public <TService> TService resolve(Class<TService> type, 
			Object... args) throws ResolutionException {
		return resolve(null, type, args);
	}
	
	public <TService> TService resolve(String name, Class<TService> type, 
			Object... args) throws ResolutionException {
		return resolveImpl(name, type, args, true);
	}
	
	public <TService> TService tryResolve(Class<TService> type, 
			Object... args) throws ResolutionException {
		return tryResolve(null, type, args);
	}
	
	public <TService> TService tryResolve(String name, Class<TService> type, 
			Object... args) throws ResolutionException {
		return resolveImpl(name, type, args, false);
	}
	
	@SuppressWarnings("unchecked")
	private <TService> TService resolveImpl(String name, Class<TService> type, 
			Object[] args, boolean throwIfMissing) throws ResolutionException {
		ServiceKey key = ServiceKey.valueOf(type, args);
		key.setName(name);
		ServiceEntry entry = getEntry(key);
		if (entry != null) {
			switch(entry.getScope()) {
			case CONTAINER:
				ServiceEntry containerEntry;
				if (entry.container != this) {
					containerEntry = entry.cloneFor(this);
					register(key, containerEntry);
				} else {
					containerEntry = entry;
				}
				
				if (containerEntry.getInstance() == null) {
					Factory<?> factory = containerEntry.getFactory();
					containerEntry.setInstance(factory.newInstance(args));
				}
				return (TService) containerEntry.getInstance();
			case HIERARCHY:
				if (entry.getInstance() == null) {
					Factory<?> factory = entry.getFactory();
					entry.setInstance(factory.newInstance(args));
				}
				return (TService) entry.getInstance();
			case NONE:
				Factory<TService> factory = (Factory<TService>) entry.getFactory();
				return factory.newInstance(args);
			default:
				throw new ResolutionException("Unknown scope");
			}
		}
		if (throwIfMissing) {
			throw new ResolutionException();
		}
		return null;
	}
	
	private ServiceEntry getEntry(ServiceKey key) {
		ServiceEntry result = null;
		Container container = this;
		while (container != null) {
			if (container.services.containsKey(key)) {
				result = container.services.get(key);
				break;
			}
			container = container.parent;
		}
		return result;
	}
	
	public static class Builder implements ru.anisimov.tools.injector.Builder<Container> {
		private List<Registration> registrations = new ArrayList<>();
		
		private Builder() {}
		
		public static Builder newInstance() {
			return new Builder();
		}
		
		@Override
		public Container build() {
			Container container = new Container();
			for (Registration registration: registrations) {
				ServiceKey key = new ServiceKey(registration.getServiceType(), 
						registration.getArgs());
				key.setName(registration.getName());
				ServiceEntry entry = ServiceEntry.Builder.
						newInstance(registration.getFactory()).
						reuseScope(registration.getReuseScope()).
						container(container).build();
				container.register(key, entry);
			}
			return container;
		}
		
		public <TService> RegistrationInterface register(Class<TService> type, Factory<TService> factory, Class<?>... args) {
			Registration registration = new Registration(type, factory, args);
			registrations.add(registration);
			return registration;
		}
	}
	
	static class ServiceEntry {
		private Factory<?> factory;
		private ReuseScope scope;
		private Object instance;
		private Container container;
		
		private ServiceEntry(Factory<?> factory) {
			this.factory = factory;
		}
		
		public ServiceEntry cloneFor(Container newContainer) {
			return Builder.newInstance(factory).
					reuseScope(scope).
					container(newContainer).build();
		}
		
		public Factory<?> getFactory() {
			return factory;
		}

		public Object getInstance() {
			return instance;
		}
		
		private void setInstance(Object instance) {
			this.instance = instance;
		}
		
		public ReuseScope getScope() {
			return scope;
		}
		
		private void setScope(ReuseScope scope) {
			this.scope = scope;
		}
		
		public Container getContainer() {
			return container;
		}
		
		private void setContainer(Container container) {
			this.container = container;
		}
		
		public static class Builder implements ru.anisimov.tools.injector.Builder<ServiceEntry> {
			private ServiceEntry entry;
			
			private	Builder(Factory<?> factory) {
				entry = new ServiceEntry(factory);
			}
			
			public static Builder newInstance(Factory<?> factory) {
				return new Builder(factory);
			}
			
			public Builder reuseScope(ReuseScope scope) {
				entry.setScope(scope);
				return this;
			}
			
			public Builder instance(Object instance) {
				entry.setInstance(instance);
				return this;
			}
			
			public Builder container(Container container) {
				entry.setContainer(container);
				return this;
			}
			
			@Override
			public ServiceEntry build() {
				return entry;
			}
		}
	}
	
	static class ServiceKey {
		private Class<?> resultType;
		private Class<?>[] argTypes;
		private String name;
		
		public ServiceKey(Class<?> resultType, Class<?>... args) {
			this.resultType = resultType;
			this.argTypes = args;
		}
		
		public static ServiceKey valueOf(Class<?> type, Object... args) {
			Class<?>[] argTypes = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
			}
			return new ServiceKey(type, argTypes);
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
			if (!(obj instanceof ServiceKey)) {
				return false;
			}
			ServiceKey tObj = (ServiceKey) obj;
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