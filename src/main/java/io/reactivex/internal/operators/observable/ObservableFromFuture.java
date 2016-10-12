/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.internal.operators.observable;

import io.reactivex.internal.functions.ObjectHelper;
import java.util.concurrent.*;

import io.reactivex.*;
import io.reactivex.disposables.*;
import io.reactivex.exceptions.Exceptions;

public final class ObservableFromFuture<T> extends Observable<T> {
    final Future<? extends T> future;
    final long timeout;
    final TimeUnit unit;

    public ObservableFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {
        this.future = future;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public void subscribeActual(Observer<? super T> s) {
        Disposable d = Disposables.empty();
        s.onSubscribe(d);
        if (!d.isDisposed()) {
            T v;
            try {
                v = ObjectHelper.requireNonNull(unit != null ? future.get(timeout, unit) : future.get(), "Future returned null");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                if (!d.isDisposed()) {
                    s.onError(ex);
                }
                return;
            } finally {
                future.cancel(true); // TODO ?? not sure about this
            }
            if (!d.isDisposed()) {
                s.onNext(v);
                s.onComplete();
            }
        }
    }
}
