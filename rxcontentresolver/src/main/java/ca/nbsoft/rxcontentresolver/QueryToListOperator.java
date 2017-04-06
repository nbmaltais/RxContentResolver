package ca.nbsoft.rxcontentresolver;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

/**
 * This class is an rxjava2 port from https://github.com/square/sqlbrite
 */
final class QueryToListOperator<T> implements ObservableOperator<List<T>, Query> {
    final Function<Cursor, T> mapper;

    QueryToListOperator(Function<Cursor, T> mapper) {
        this.mapper = mapper;
    }

    /*@Override
    public Subscriber<? super SqlBrite.Query> call(final Subscriber<? super List<T>> subscriber) {
        return new Subscriber<SqlBrite.Query>(subscriber) {
            @Override
            public void onNext(SqlBrite.Query query) {
                try {
                    Cursor cursor = query.run();
                    if (cursor == null || subscriber.isUnsubscribed()) {
                        return;
                    }
                    List<T> items = new ArrayList<>(cursor.getCount());
                    try {
                        while (cursor.moveToNext()) {
                            items.add(mapper.apply(cursor));
                        }
                    } finally {
                        cursor.close();
                    }
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(items);
                    }
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    onError(OnErrorThrowable.addValueAsLastCause(e, query.toString()));
                }
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }
        };
    }*/

    @Override
    public Observer<? super Query> apply(@NonNull final Observer<? super List<T>> observer) throws Exception {
        return new Observer<Query>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Query query) {
                try {
                    Cursor cursor = query.run();
                    if (cursor == null /*|| observer.()*/) {
                        return;
                    }
                    List<T> items = new ArrayList<>(cursor.getCount());
                    try {
                        while (cursor.moveToNext()) {
                            items.add(mapper.apply(cursor));
                        }
                    } finally {
                        cursor.close();
                    }
                    /*if (!observer.())*/ {
                        observer.onNext(items);
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
