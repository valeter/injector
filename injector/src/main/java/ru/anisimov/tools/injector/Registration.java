package ru.anisimov.tools.injector;

public class Registration {
	private String serviceName;
	private Class<?> serviceType; 
	private Factory<?> factory;
	private Class<?>[] args;
	private ReuseScope reuseScope;
	
	<TService> Registration(Class<TService> type, 
			Factory<TService> factory, Class<?>... args) {
		this.serviceName = null;
		this.reuseScope = ReuseScope.NONE;
		this.serviceType = type;
		this.factory = factory;
		this.args = args;
	}
	
	public <TService> void named(String name) {
		serviceName = name;
	}
	
	public void reuseWithin(ReuseScope scope) {
		this.reuseScope = scope;
	}

	public String getName() {
		return serviceName;
	}

	public Class<?> getServiceType() {
		return serviceType;
	}

	public Factory<?> getFactory() {
		return factory;
	}

	public Class<?>[] getArgs() {
		return args;
	}
	
	public ReuseScope getReuseScope() {
		return reuseScope;
	}
}
