package com.netflix.fabricator.supplier;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

/**
 * Extension to the supplier interface to allow for a notification
 * callback whenever the value changes.  Note that the function
 * may be called in response to get() or from an underlying update
 * mechanism.
 * 
 * @author elandau
 *
 * @param <T>
 */
public interface ListenableSupplier<T> extends Supplier<T> {
    public void onChange(Function<T, Void> func);
}
