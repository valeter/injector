package ru.anisimov.tools.injector;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ContainerTest {
	@Test
	public void registerTypeAndGetInstance() {
		class BarFactory implements ContainerFactory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		Container container = new Container();
		container.register(IBar.class, barFactory);
		IBar bar = container.resolve(IBar.class);
		
		assertNotNull(bar);
		assertEquals(bar.getClass(), Bar.class);
	}
	
	@Test
	public void resolveGetsDependenciesInjected() {
		class BarFactory implements ContainerFactory<IBar> {
			@SuppressWarnings("unused")
			@Override
			public IBar newInstance(Object... args) { return new Bar(); }
		}
		BarFactory barFactory = new BarFactory();
		final Container container = new Container();
		container.register(IBar.class, barFactory);
		class FooFactory implements ContainerFactory<IFoo> {
			@SuppressWarnings("unused")
			@Override
			public IFoo newInstance(Object... args) { return new Foo(container.resolve(IBar.class)); }
		}
		FooFactory fooFactory = new FooFactory();
		container.register(IFoo.class, fooFactory);
		IFoo foo = container.resolve(IFoo.class);
		
		assertNotNull(foo);
		assertEquals(foo.getClass(), Foo.class);
	}
	
	@Test
	public void constructorArgumentsArePassedOnResolve() {
		class BarFactory implements ContainerFactory<IBar> {
			@Override
			public IBar newInstance(Object... args) { return new Bar((String) args[0], (boolean) args[1]); }
		}
		BarFactory barFactory = new BarFactory();
		final Container container = new Container();
		container.register(IBar.class, barFactory, String.class, Boolean.class);
		Bar bar = (Bar)container.resolve(IBar.class, "first", true);
		
		assertEquals(bar.getArg0(), "first");
		assertEquals(bar.isArg1(), true);
	}
	
	@Test
	public void registerMultipleConstructorOverloads() {
		final Container container = new Container();
		class BarFactory1 implements ContainerFactory<IBar> {
			@Override
			public IBar newInstance(Object... args) { return new Bar((String) args[0], (boolean) args[1]); }
		}
		BarFactory1 barFactory1 = new BarFactory1();
		class BarFactory2 implements ContainerFactory<IBar> {
			@Override
			public IBar newInstance(Object... args) { return new Bar((String) args[0]); }
		}
		BarFactory2 barFactory2 = new BarFactory2();
		
		container.register(IBar.class, barFactory1, String.class, Boolean.class);
		container.register(IBar.class, barFactory2, String.class);
		
		Bar bar1 = (Bar)container.resolve(IBar.class, "first", true);
		Bar bar2 = (Bar)container.resolve(IBar.class, "first");
		
		assertEquals(bar1.getArg0(), "first");
		assertEquals(bar1.isArg1(), true);
		
		assertEquals(bar2.getArg0(), "first");
		assertEquals(bar2.isArg1(), false);
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
