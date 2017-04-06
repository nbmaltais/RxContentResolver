/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.nbsoft.rxcontentresolver;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CheckResult;
import android.util.Log;

import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A lightweight wrapper around {@link ContentResolver} which allows for continuously observing
 * the result of a query.
 *
 * This class is an rxjava2 port from https://github.com/square/sqlbrite
 */
public final class RxContentResolver {

    /**
     * A simple indirection for logging debug messages.
     */
    public interface Logger {
        void log(String message);
    }
    static final Logger DEFAULT_LOGGER = new Logger() {
        @Override
        public void log(String message) {
            Log.d("SqlBrite", message);
        }
    };
    final Handler contentObserverHandler = new Handler(Looper.getMainLooper());

    final ContentResolver contentResolver;
    private final Logger logger;
    private final Scheduler scheduler;
    private final ObservableTransformer<Query, Query> queryTransformer;

    volatile boolean logging;

    RxContentResolver(ContentResolver contentResolver, Logger logger, Scheduler scheduler,
                      ObservableTransformer<Query, Query> queryTransformer) {
        this.contentResolver = contentResolver;
        this.logger = logger;
        this.scheduler = scheduler;
        this.queryTransformer = queryTransformer;
    }

    RxContentResolver(ContentResolver contentResolver, Scheduler scheduler) {
        this.contentResolver = contentResolver;
        this.logger = DEFAULT_LOGGER;
        this.scheduler = scheduler;
        this.queryTransformer = null;
    }

    RxContentResolver(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        this.logger = DEFAULT_LOGGER;
        this.scheduler = Schedulers.io();
        this.queryTransformer = null;
    }

    static public RxContentResolver from(Context context)
    {
        return new RxContentResolver(context.getContentResolver());
    }

    /**
     * Control whether debug logging is enabled.
     */
    public void setLoggingEnabled(boolean enabled) {
        logging = enabled;
    }

    /**
     * Create an observable which will notify subscribers with a {@linkplain Query query} for
     * execution. Subscribers are responsible for <b>always</b> closing {@link Cursor} instance
     * returned from the {@link Query}.
     * <p>
     * Subscribers will receive an immediate notification for initial data as well as subsequent
     * notifications for when the supplied {@code uri}'s data changes. Unsubscribe when you no longer
     * want updates to a query.
     * <p>
     * Since content resolver triggers are inherently asynchronous, items emitted from the returned
     * observable use the {@link Scheduler} supplied. For
     * consistency, the immediate notification sent on subscribe also uses this scheduler. As such,
     * calling {@link Observable#subscribeOn subscribeOn} on the returned observable has no effect.
     * <p>
     * Note: To skip the immediate notification and only receive subsequent notifications when data
     * has changed call {@code skip(1)} on the returned observable.
     * <p>
     * <b>Warning:</b> this method does not perform the query! Only by subscribing to the returned
     * {@link Observable} will the operation occur.
     *
     * @see ContentResolver#query(Uri, String[], String, String[], String)
     * @see ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)
     */
    @CheckResult
    @NonNull
    public QueryObservable createQuery(@NonNull final Uri uri, @Nullable final String[] projection,
                                         @Nullable final String selection, @Nullable final String[] selectionArgs, @Nullable
                                       final String sortOrder, final boolean notifyForDescendents) {
        final Query query = new Query() {
            @Override
            public Cursor run() {
                long startNanos = nanoTime();
                Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);

                if (logging) {
                    long tookMillis = NANOSECONDS.toMillis(nanoTime() - startNanos);
                    log("QUERY (%sms)\n  uri: %s\n  projection: %s\n  selection: %s\n  selectionArgs: %s\n  "
                                    + "sortOrder: %s\n  notifyForDescendents: %s", tookMillis, uri,
                            Arrays.toString(projection), selection, Arrays.toString(selectionArgs), sortOrder,
                            notifyForDescendents);
                }

                return cursor;
            }
        };


        ObservableOnSubscribe<Query> subscribe = new ObservableOnSubscribe<Query>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<Query> e) throws Exception {
                final ContentObserver observer = new ContentObserver(contentObserverHandler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        e.onNext(query);
                    }
                };
                contentResolver.registerContentObserver(uri, notifyForDescendents, observer);
                e.setDisposable(new Disposable() {
                    boolean disposed=false;
                    @Override
                    public void dispose() {
                        contentResolver.unregisterContentObserver(observer);
                        disposed=true;
                    }

                    @Override
                    public boolean isDisposed() {
                        return disposed;
                    }
                });

                e.onNext(query); // Trigger initial query.
            }


        };
        final Observable<Query> queryObservable = Observable.create(subscribe) //
                //.onBackpressureLatest() // Guard against uncontrollable frequency of upstream emissions.
                .observeOn(scheduler) //
                //.compose(queryTransformer) // Apply the user's query transformer.
                //.onBackpressureLatest() // Guard against uncontrollable frequency of scheduler executions.
        ;

        return new QueryObservable(queryObservable);

        //return queryObservable;

        /*// TODO switch to .to() when non-@Experimental
        return new QueryObservable(new OnSubscribe<Query>() {
            @Override
            public void call(Subscriber<? super Query> subscriber) {
                queryObservable.unsafeSubscribe(subscriber);
            }
        });*/
    }

    void log(String message, Object... args) {
        if (args.length > 0) message = String.format(message, args);
        logger.log(message);
    }
}
