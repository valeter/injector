package ru.anisimov.tools.injector.syntax.registration;

import ru.anisimov.tools.injector.ReuseScope;

public interface ReusedWithin<TResult> {
	TResult reusedWithin(ReuseScope scope);
}
