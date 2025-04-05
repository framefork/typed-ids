package org.framefork.typedIds.common;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class LazyValue<T>
{

    private final Supplier<T> initialValueSupplier;

    @Nullable
    private volatile T value;

    public LazyValue(final Supplier<T> initialValueSupplier)
    {
        this.initialValueSupplier = Objects.requireNonNull(initialValueSupplier, "initialValueSupplier must not be null");
    }

    public T get()
    {
        var result = value;
        if (result == null) {
            synchronized (this) {
                result = value;
                if (result == null) {
                    result = initialValueSupplier.get();
                    if (result == null) {
                        throw new NullPointerException("initialValueSupplier must not return null");
                    }
                    value = result;
                }
            }
        }

        return result;
    }

    public void set(final T newValue)
    {
        Objects.requireNonNull(newValue, "newValue must not be null");
        synchronized (this) {
            value = newValue;
        }
    }

}
