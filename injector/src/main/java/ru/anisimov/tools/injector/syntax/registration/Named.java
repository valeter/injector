package ru.anisimov.tools.injector.syntax.registration;

public interface Named<TResult> {
	TResult named(String string);
}
