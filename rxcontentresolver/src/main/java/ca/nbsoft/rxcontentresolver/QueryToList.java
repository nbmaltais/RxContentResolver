package ca.nbsoft.rxcontentresolver;

import android.database.Cursor;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

/**
 * Created by Nicolas on 2017-04-04.
 * This class is an rxjava2 port from https://github.com/square/sqlbrite
 */

public final class QueryToList<T> extends Observable<List<T>> {

    public interface MapperToList<T>
    {
        List<T> apply(@NonNull Cursor cursor) throws Exception;
    }
    public interface MapperToItem<T>
    {
        T apply(@NonNull Cursor cursor) throws Exception;
    }

    final Observable<Query> source;
    final MapperToList<T> mapper;

    static public <T> QueryToList<T> fromMapper( Observable<Query> source, MapperToList<T> mapper )
    {
        return new QueryToList<T>(source,mapper);
    }

    static public <T> QueryToList<T> fromMapper( Observable<Query> source, final MapperToItem<T> mapper )
    {
        return new QueryToList<T>(source,
                new MapperToList<T>() {
                    @Override
                    public List<T> apply(@NonNull Cursor cursor) throws Exception {
                        List<T> results = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            results.add(mapper.apply(cursor));
                        }
                        return results;
                    }
                });
    }

    public QueryToList(Observable<Query> source, MapperToList<T> mapper) {
        this.source = source;
        this.mapper = mapper;
    }



    @Override
    protected void subscribeActual(final Observer<? super List<T>> observer) {

       /* source.subscribe(new Observer<Query>() {
            @Override
            public void onSubscribe(Disposable d) {
                observer.onSubscribe(d);
            }

            @Override
            public void onNext(Query query) {
                try {

                    Cursor cursor = query.run();
                    if (cursor != null) {
                        try {
                            observer.onNext(mapper.apply(cursor));
                        } finally {
                            cursor.close();
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

        source.map(new Function<Query, List<T>>() {
            @Override
            public List<T> apply(@NonNull Query query) throws Exception {
                Cursor cursor = query.run();
                if(cursor==null)
                {
                    throw new Exception("Null cursor");
                }
                try {
                    return mapper.apply(cursor);
                }
                finally {
                    cursor.close();
                }

            }
        }).subscribe(observer);
    }
}
