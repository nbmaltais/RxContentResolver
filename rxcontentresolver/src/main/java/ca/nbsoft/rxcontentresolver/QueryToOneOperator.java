package ca.nbsoft.rxcontentresolver;

import android.database.Cursor;

import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

/**
 * This class is an rxjava2 port from https://github.com/square/sqlbrite
 * @param <T>
 */
final class QueryToOneOperator<T> implements ObservableOperator<T, Query> {
    final Function<Cursor, T> mapper;
    boolean emitDefault;
    T defaultValue;

    QueryToOneOperator(Function<Cursor, T> mapper, boolean emitDefault, T defaultValue) {
        this.mapper = mapper;
        this.emitDefault = emitDefault;
        this.defaultValue = defaultValue;
    }

    /*@Override
    public Subscriber<? super SqlBrite.Query> call(final Subscriber<? super T> subscriber) {
        return new Subscriber<SqlBrite.Query>(subscriber) {
            @Override
            public void onNext(SqlBrite.Query query) {
                try {
                    boolean emit = false;
                    T item = null;
                    Cursor cursor = query.run();
                    if (cursor != null) {
                        try {
                            if (cursor.moveToNext()) {
                                item = mapper.call(cursor);
                                emit = true;
                                if (cursor.moveToNext()) {
                                    throw new IllegalStateException("Cursor returned more than 1 row");
                                }
                            }
                        } finally {
                            cursor.close();
                        }
                    }
                    if (!subscriber.isUnsubscribed()) {
                        if (emit) {
                            subscriber.onNext(item);
                        } else if (emitDefault) {
                            subscriber.onNext(defaultValue);
                        } else {
                            request(1L); // Account upstream for the lack of downstream emission.
                        }
                    }
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    onError(OnErrorThrowable.addValueAsLastCause(e, query.toString()));
                }
            }

            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }
        };
    }*/

    @Override
    public Observer<? super Query> apply(@NonNull final Observer<? super T> observer) throws Exception {
        return new Observer<Query>() {
            @Override
            public void onSubscribe(Disposable d) {

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
                    /*if (!observer.isUnsubscribed())*/ {
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
        };
    }
}
