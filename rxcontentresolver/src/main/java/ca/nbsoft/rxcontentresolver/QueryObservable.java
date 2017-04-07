package ca.nbsoft.rxcontentresolver;

import android.database.Cursor;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.functions.Function;

/**
 * An {@link Observable} of {@link Query} which offers query-specific convenience operators.
 * This class is an rxjava2 port from https://github.com/square/sqlbrite
 */
public final class QueryObservable extends Observable<Query> {
    private final ObservableSource<Query> source;
  /*public QueryObservable(OnSubscribe<Query> func) {
    super(func);
  }*/

    public QueryObservable(ObservableSource<Query> source)
    {
        this.source = source;
    }


    /**
     * Given a function mapping the current row of a {@link Cursor} to {@code T}, transform each
     * emitted {@link Query} which returns a single row to {@code T}.
     * <p>
     * It is an error for a query to pass through this operator with more than 1 row in its result
     * set. Use {@code LIMIT 1} on the underlying SQL query to prevent this. Result sets with 0 rows
     * do not emit an item.
     * <p>
     * This method is equivalent to:
     * <pre>{@code
     * flatMap(q -> q.asRows(mapper).take(1))
     * }</pre>
     * and a convenience operator for:
     * <pre>{@code
     * lift(Query.mapToOne(mapper))
     * }</pre>
     *
     * @param mapper Maps the current {@link Cursor} row to {@code T}. May not return null.
     */
    @CheckResult
    @NonNull
    public final <T> Observable<T> mapToOne(@NonNull Function<Cursor, T> mapper) {
        return new QueryToOneOperator<T>(this,mapper,false,null);
    }

    /**
     * Given a function mapping the current row of a {@link Cursor} to {@code T}, transform each
     * emitted {@link Query} which returns a single row to {@code T}.
     * <p>
     * It is an error for a query to pass through this operator with more than 1 row in its result
     * set. Use {@code LIMIT 1} on the underlying SQL query to prevent this. Result sets with 0 rows
     * emit {@code defaultValue}.
     * <p>
     * This method is equivalent to:
     * <pre>{@code
     * flatMap(q -> q.asRows(mapper).take(1).defaultIfEmpty(defaultValue))
     * }</pre>
     * and a convenience operator for:
     * <pre>{@code
     * lift(Query.mapToOneOrDefault(mapper, defaultValue))
     * }</pre>
     *
     * @param mapper       Maps the current {@link Cursor} row to {@code T}. May not return null.
     * @param defaultValue Value returned if result set is empty
     */
    @CheckResult
    @NonNull
    public final <T> Observable<T> mapToOneOrDefault(@NonNull Function<Cursor, T> mapper,
                                                     T defaultValue) {
        return new QueryToOneOperator<T>(this,mapper,true,defaultValue);
    }

    /**
     * Given a function mapping the current row of a {@link Cursor} to {@code T}, transform each
     * emitted {@link Query} to a {@code List<T>}.
     * <p>
     * Be careful using this operator as it will always consume the entire cursor and create objects
     * for each row, every time this observable emits a new query. On tables whose queries update
     * frequently or very large result sets this can result in the creation of many objects.
     * <p>
     * This method is equivalent to:
     * <pre>{@code
     * flatMap(q -> q.asRows(mapper).toList())
     * }</pre>
     * and a convenience operator for:
     * <pre>{@code
     * lift(Query.mapToList(mapper))
     * }</pre>
     * <p>
     * Consider using {@link Query#asRows} if you need to limit or filter in memory.
     *
     * @param mapper Maps the current {@link Cursor} row to {@code T}. May not return null.
     */



    @CheckResult
    @NonNull
    public final <T> Observable<List<T>> mapToList(@NonNull QueryToListOperator.MapperToList<T> mapper) {
        return  QueryToListOperator.fromMapper(this,mapper);
    }



    @CheckResult
    @NonNull
    public final <T> Observable<List<T>> mapToList(@NonNull QueryToListOperator.MapperToItem<T>  mapper) {
        return  QueryToListOperator.fromMapper(this,mapper);
    }


    @Override
    protected void subscribeActual(Observer<? super Query> observer) {
        source.subscribe(observer);
    }
}
