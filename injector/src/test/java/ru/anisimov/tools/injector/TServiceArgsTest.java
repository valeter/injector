package ru.anisimov.tools.injector;

import static org.junit.Assert.*;

import java.util.Objects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ru.anisimov.tools.injector.Container.ServiceKey;

@RunWith(JUnit4.class)
public class TServiceArgsTest {
	@Test
	public void equalsAndValueOfAndHashCodeTest() {
		Class<?>[] types = new Class<?>[2];
		types[0] = Bar.class;
		types[1] = Foo.class;
		
		Object[] objects = new Object[2];
		objects[0] = new Bar();
		objects[1] = new Foo();
		
		ServiceKey args1 = new ServiceKey(Object.class, types);
		ServiceKey args2 = ServiceKey.valueOf(Object.class, objects);
		
		assertEquals(args1, args2);
		assertEquals(args1.hashCode(), args2.hashCode());
	}
	
	@Test
	public void notEqualsTestDifferentArgs() {
		Class<?>[] types = new Class<?>[2];
		types[0] = Bar.class;
		types[1] = Foo.class;
		
		Object[] objects = new Object[2];
		objects[0] = new Foo();
		objects[1] = new Bar();
		
		ServiceKey args1 = new ServiceKey(Object.class, types);
		ServiceKey args2 = ServiceKey.valueOf(Object.class, objects);
		
		assertFalse(Objects.equals(args1, args2));
	}
	
	@Test
	public void notEqualsTestDifferentReturnTypes() {
		Class<?>[] types = new Class<?>[2];
		types[0] = Bar.class;
		types[1] = Foo.class;
		
		Object[] objects = new Object[2];
		objects[0] = new Bar();
		objects[1] = new Foo();
		
		ServiceKey args1 = new ServiceKey(Object.class, types);
		ServiceKey args2 = ServiceKey.valueOf(Integer.class, objects);
		
		assertFalse(Objects.equals(args1, args2));
	}
	
	class Bar { public Bar() {super();} } 
	class Foo { public Foo() {super();} }
}
