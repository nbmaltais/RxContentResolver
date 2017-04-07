package ca.nbsoft.rxcontentresolver;

import android.database.Cursor;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.observers.BasicFuseableObserver;

/**
 * Created by Nicolas on 2017-04-04.
 */

public class QueryToOneOperator<T> extends Observable<T> {
    final Observable<Query> source;
    final Function<Cursor, T> mapper;
    boolean emitDefault;
    T defaultValue;

    QueryToOneOperator(Observable<Query> source, Function<Cursor, T> mapper, boolean emitDefault, T defaultValue) {
        this.source = source;
        this.mapper = mapper;
        this.emitDefault = emitDefault;
        this.defaultValue = defaultValue;
    }

    @Override
    protected void subscribeActual(final Observer<? super T> observer) {

        source.subscribe(new MapToOneObserver<T>(observer, mapper,emitDefault,defaultValue));


        // ???? why do I need to call an operator here???
        // If I subscribe directly to source, onNext receive null...
        /*source.map(new Function<Query, Query>() {
            @Override
            public Query apply(@NonNull Query query) throws Exception {
                return query;
            }
        }).subscribe(new Observer<Query>() {
            @Override
            public void onSubscribe(Disposable d) {
                observer.onSubscribe(d);
            }

            @Override
            public void onNext(Query query) {

                try {
                    boolean emit = false;
                    T item = null;
                    Cursor cursor = query.run();
                    if (cursor != null) {
                        try {
                            if (cursor.moveToNext()) {
                                item = mapper.apply(cursor);
                                emit = true;
                                if (cursor.moveToNext()) {
                                    throw new IllegalStateException("Cursor returned more than 1 row");
                                }
                            }
                        } finally {
                            cursor.close();
                        }
                    }

                    {
                        if (emit) {
                            observer.onNext(item);
                        } else if (emitDefault) {
                            observer.onNext(defaultValue);
                        } else {
                            //request(1L); // Account upstream for the lack of downstream emission.
                        }
                    }
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    //onError(OnErrorThrowable.addValueAsLastCause(e, query.toString()));
                    onError(e);
                }
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        });*/
    }


    static final class MapToOneObserver<U> extends BasicFuseableObserver<Query, U> {
        final Function<Cursor, U> mapper;
        final boolean emitDefault;
        final U defaultValue;

        MapToOneObserver(Observer<? super U> actual, Function<Cursor, U> mapper, boolean emitDefault, U defaultValue) {
            super(actual);
            this.mapper = mapper;
            this.emitDefault = emitDefault;
            this.defaultValue = defaultValue;
        }

        @Override
        public void onNext(Query t) {
            if (done) {
                return;
            }

            if (sourceMode != NONE) {
                actual.onNext(null);
                return;
            }

            U v;

            try {
                v = getFromQuery(t);
            } catch (Throwable ex) {
                fail(ex);
                return;
            }
            if (v != null) {
                actual.onNext(v);
            }
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Nullable
        @Override
        public U poll() throws Exception {
            Query t = qs.poll();
            return t != null ? getFromQuery(t) : null;
        }

        private U getFromQuery(Query query) throws Exception {
            boolean emit = false;
            U item = null;
            Cursor cursor = query.run();
            if (cursor != null) {
                try {
                    if (cursor.moveToNext()) {
                        item = mapper.apply(cursor);
                        emit = true;
                        if (cursor.moveToNext()) {
                            throw new IllegalStateException("Cursor returned more than 1 row");
                        }
                    }
                } finally {
                    cursor.close();
                }
            }

            if (emit) {
                return item;
            } else if (emitDefault) {
                return defaultValue;
            } else {
                return null;
            }

        }
    }
}
