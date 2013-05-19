package ru.anisimov.tools.injector;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ru.anisimov.tools.injector.exceptions.ResolutionException;

@RunWith(JUnit4.class)
public class ContainerTest {
	@Test
	public void registerTypeAndGetInstance() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory);
		
		Container container = builder.build();
		IBar bar = container.resolve(IBar.class);
		
		assertNotNull(bar);
		assertEquals(bar.getClass(), Bar.class);
	}
	
	@Test
	public void resolveGetsDependenciesInjected() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		
		final Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory);
		
		class FooFactory implements Factory<IFoo> {
			@SuppressWarnings("unused")
			@Override
			public IFoo newInstance(Object... args) { 
				try {
					return new Foo(builder.build().resolve(IBar.class));
				} catch (ResolutionException e) {
					throw new RuntimeException(e);
				} 
			}
		}
		FooFactory fooFactory = new FooFactory();
		builder.register(IFoo.class, fooFactory);
		
		Container container = builder.build();
		IFoo foo = container.resolve(IFoo.class);
		
		assertNotNull(foo);
		assertEquals(foo.getClass(), Foo.class);
	}
	
	@Test
	public void constructorArgumentsArePassedOnResolve() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@Override
			public IBar newInstance(Object... args) { return new Bar((String) args[0], (boolean) args[1]); }
		}
		BarFactory barFactory = new BarFactory();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory, String.class, Boolean.class);
		
		Container container = builder.build();
		Bar bar = (Bar)container.resolve(IBar.class, "first", true);
		
		assertEquals(bar.getArg0(), "first");
		assertEquals(bar.isArg1(), true);
	}
	
	@Test
	public void registerMultipleConstructorOverloads() throws ResolutionException {
		class BarFactory1 implements Factory<IBar> {
			@Override
			public IBar newInstance(Object... args) { return new Bar((String) args[0], (boolean) args[1]); }
		}
		BarFactory1 barFactory1 = new BarFactory1();
		class BarFactory2 implements Factory<IBar> {
			@Override
			public IBar newInstance(Object... args) { return new Bar((String) args[0]); }
		}
		BarFactory2 barFactory2 = new BarFactory2();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory1, String.class, Boolean.class);
		builder.register(IBar.class, barFactory2, String.class);
		
		Container container = builder.build();
		Bar bar1 = (Bar)container.resolve(IBar.class, "first", true);
		Bar bar2 = (Bar)container.resolve(IBar.class, "first");
		
		assertEquals(bar1.getArg0(), "first");
		assertEquals(bar1.isArg1(), true);
		
		assertEquals(bar2.getArg0(), "first");
		assertEquals(bar2.isArg1(), false);
	}
	
	@Test
	public void resolveNamedServices() throws ResolutionException {
		class BarFactory1 implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar("a"); }
		}
		BarFactory1 barFactory1 = new BarFactory1();
		
		class BarFactory2 implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar("b"); }
		}
		BarFactory2 barFactory2 = new BarFactory2();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory1).named("a");
		builder.register(IBar.class, barFactory2).named("b");
		
		Container container = builder.build();
		Bar a = (Bar) container.resolve("a", IBar.class);
		Bar b = (Bar) container.resolve("b", IBar.class);
		
		assertEquals(a.arg0, "a");
		assertEquals(b.arg0, "b");
	}
	
	@Test
	public void registrationOrderDoesNotMatter() throws ResolutionException {
		class BarFactory0 implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar("noname"); }
		}
		BarFactory0 barFactory0 = new BarFactory0();
		
		class BarFactory1 implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar("a"); }
		}
		BarFactory1 barFactory1 = new BarFactory1();
		
		class BarFactory2 implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar("b"); }
		}
		BarFactory2 barFactory2 = new BarFactory2();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory0);
		builder.register(IBar.class, barFactory1).named("a");
		builder.register(IBar.class, barFactory2).named("b");
		
		Container container = builder.build();
		Bar a = (Bar) container.resolve("a", IBar.class);
		Bar b = (Bar) container.resolve("b", IBar.class);
		Bar c = (Bar) container.resolve(IBar.class);
		
		assertEquals(a.getArg0(), "a");
		assertEquals(b.getArg0(), "b");
		assertEquals(c.getArg0(), "noname");
	}
	
	@Test(expected=ResolutionException.class)
	public void resolveNotRegisteredThrows() throws ResolutionException {
		Container.Builder builder = Container.Builder.newInstance();
		Container container = builder.build();
		container.resolve(IFoo.class);
	}
	
	@Test
	public void tryResolveNotRegisteredServiceReturnsNull() throws ResolutionException {
		Container.Builder builder = Container.Builder.newInstance();
		Container container = builder.build();
		IFoo foo = container.tryResolve(IFoo.class);
		assertNull(foo);
	}
	
	@Test
	public void constructorArgumentsArePassedOnTryResolve() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@Override
			public IBar newInstance(Object... args) { return new Bar((String) args[0], (boolean) args[1]); }
		}
		BarFactory barFactory = new BarFactory();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory, String.class, Boolean.class);
		
		Container container = builder.build();
		Bar bar = (Bar) container.tryResolve(IBar.class, "first", true);
		
		assertEquals(bar.getArg0(), "first");
		assertEquals(bar.isArg1(), true);
	}
	
	@Test
	public void instanceIsReusedWithinContainer() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory).reusedWithin(ReuseScope.CONTAINER);
		
		Container container = builder.build();
		
		IBar bar1 = container.resolve(IBar.class);
		IBar bar2 = container.resolve(IBar.class);
		
		assertSame(bar1, bar2);
	}
	
	@Test
	public void ServiceRegisterOnParentResolvesOnChild() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory);
		
		Container container = builder.build();
		Container child = container.createChildContainer();
		
		IBar bar = child.resolve(IBar.class);
		assertNotNull(bar);
	}
	
	@Test
	public void HierarchyScopedInstanceIsReusedOnSameContainer() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory).reusedWithin(ReuseScope.HIERARCHY);
		
		Container container = builder.build();
		
		IBar bar1 = container.resolve(IBar.class);
		IBar bar2 = container.resolve(IBar.class);
		assertSame(bar1, bar2);
	}
	
	@Test
	public void HierarchyScopedInstanceIsReusedOnParentContainer() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory).reusedWithin(ReuseScope.HIERARCHY);
		
		Container parent = builder.build();
		Container child = parent.createChildContainer();
		
		IBar bar1 = child.resolve(IBar.class);
		IBar bar2 = parent.resolve(IBar.class);
		assertSame(bar1, bar2);
	}
	
	@Test
	public void HierarchyScopedInstanceIsReusedOnChildContainer() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory).reusedWithin(ReuseScope.HIERARCHY);
		
		Container parent = builder.build();
		Container child = parent.createChildContainer();
		
		IBar bar1 = parent.resolve(IBar.class);
		IBar bar2 = child.resolve(IBar.class);
		assertSame(bar1, bar2);
	}
	
	@Test
	public void ContainerScopedInstanceIsNotReusedOnChild() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory).reusedWithin(ReuseScope.CONTAINER);
		
		Container parent = builder.build();
		Container child = parent.createChildContainer();
		
		IBar bar1 = parent.resolve(IBar.class);
		IBar bar2 = child.resolve(IBar.class);
		assertNotSame(bar1, bar2);
	}
	
	@Test
	public void ContainerOwnedInstancesAreDisposed() throws ResolutionException {
		class BarFactory implements Factory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		
		Container.Builder builder = Container.Builder.newInstance();
		builder.register(IBar.class, barFactory).reusedWithin(ReuseScope.CONTAINER);
		
		builder.register(IBar.class, barFactory).reusedWithin(ReuseScope.CONTAINER);
		builder.register(IBar.class, barFactory).named("asd").reusedWithin(ReuseScope.CONTAINER);
		
		Container parent = builder.build();
		Container child = parent.createChildContainer();
		
		IBar bar1 = parent.resolve(IBar.class);
		IBar bar2 = child.resolve(IBar.class);
		assertNotSame(bar1, bar2);
	}
	 
	private interface IBar {}
	private interface IFoo {}
	
	private class Bar implements IBar {
		private String arg0;
		private boolean arg1;
		
		public Bar() {
			this("", false);
		}
		
		public Bar(String arg0) {
			this(arg0, false);
		}
		
		public Bar(String arg0, boolean arg1) {
			this.arg0 = arg0;
			this.arg1 = arg1;
		}

		public String getArg0() {
			return arg0;
		}

		public boolean isArg1() {
			return arg1;
		}
	}
	private class Foo implements IFoo {
		private IBar bar;
		
		public Foo(IBar bar) {
			this.bar = bar;
		}
		
		@SuppressWarnings("unused")
		public IBar getBar() {
			return bar;
		} 
	}
}
