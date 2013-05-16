package ru.anisimov.tools.injector;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ContainerTest {
	@Test
	public void registerTypeAndGetInstance() {
		BarFactory barFactory = new BarFactory();
		Container container = new Container();
		container.register(IBar.class, barFactory);
		IBar bar = container.resolve(IBar.class);
		
		assertNotNull(bar);
		assertEquals(bar.getClass(), Bar.class);
	}
	
	@Test
	public void resolveGetsDependenciesInjected() {
		BarFactory barFactory = new BarFactory();
		final Container container = new Container();
		container.register(IBar.class, barFactory);
		class FooFactory implements ContainerFactory<IFoo> {
			@Override
			public IFoo newInstance() { return new Foo(container.resolve(IBar.class)); }
		}
		FooFactory fooFactory = new FooFactory();
		container.register(IFoo.class, fooFactory);
		IFoo foo = container.resolve(IFoo.class);
		
		assertNotNull(foo);
		assertEquals(foo.getClass(), Foo.class);
	}
	
	private interface IBar {}
	private interface IFoo {}
	
	private class Bar implements IBar {}
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

	private class BarFactory implements ContainerFactory<IBar> {
		@Override
		public IBar newInstance() { return new Bar(); }
	}
}
