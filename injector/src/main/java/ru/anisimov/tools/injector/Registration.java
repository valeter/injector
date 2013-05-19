package ru.anisimov.tools.injector;

import ru.anisimov.tools.injector.syntax.registration.RegistrationInterface;
import ru.anisimov.tools.injector.syntax.registration.ReusedWithin;

public class Registration implements RegistrationInterface {
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
	
	@Override
	public ReusedWithin<?> named(String name) {
		serviceName = name;
		return this;
	}
	
	@Override
	public Object reusedWithin(ReuseScope scope) {
		reuseScope = scope;
		return null;
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
