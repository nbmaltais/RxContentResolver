package ca.nbsoft.rxloader2.support;

import android.content.Context;
import android.support.v4.content.Loader;
import android.util.Log;

import ca.nbsoft.rxloader2.RxLoaderGetError;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


/**
 * Loader that uses an rxjava observable
 * Created by Nicolas on 2016-05-14.
 */
public class RxLoader<T> extends RxLoaderBase<T> {
    private static final String TAG = RxLoader.class.getSimpleName();
    private final Observable<T> mObservable;
    private final Scheduler mScheduler;
    private T mData;
    private Throwable mError;
    private boolean mCompleted=false;
    private CompositeDisposable mSubscriptions = new CompositeDisposable();
    private final static boolean ENABLELOG = false;

    public RxLoader(Context context, Observable<T> observable) {
        super(context);
        mObservable = observable;
        mScheduler= Schedulers.io();
    }
    public RxLoader(Context context, Observable<T> observable, Scheduler s) {
        super(context);
        mObservable = observable;
        mScheduler = s;

    }

    protected T getData()
    {
        return mData;
    }

    protected void setData(T d) {
        mData=d;
    }

    @Override
    protected void onStartLoading() {
        if(ENABLELOG) {
            Log.v(TAG, "onStartLoading, loaderid = " + getId());
        }

        if (mData != null) {
            if(ENABLELOG) {
                Log.v(TAG, "delivering cached data, loaderid = " + getId());
            }
            deliverResult(mData);
        }

        if(mCompleted)
            return;

        Observable<T> observable;
        if(mScheduler!=null)
        {
            observable = mObservable.subscribeOn(mScheduler);
        }
        else
        {
            observable = mObservable;
        }

        if(ENABLELOG) {
            Log.v(TAG, "connecting to observable, loaderid = " + getId());
        }

        Disposable subscription = observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<T>() {
                    @Override
                    public void onComplete() {
                        mCompleted = true;
                    }

                    @Override
                    public void onError(Throwable e) {
                        mData = null;
                        mError = e;
                        // Deliver the result
                        deliverResult(null);
                    }

                    @Override
                    public void onNext(T t) {
                        if (ENABLELOG) {
                            Log.v(TAG, "onNext, loaderid = " + getId());
                        }

                        // Deliver the result
                        deliverResult(storeData(t));
                    }
                });
        mSubscriptions.add(subscription);
    }

    protected T storeData(T d)
    {
        mData = d;
        return mData;
    }

    @Override
    protected void onForceLoad() {
        if(ENABLELOG) {
            Log.v(TAG, "onForceLoad, loaderid = " + getId());
        }
        // Resend the last known location if we have one
        if (mData != null) {
            deliverResult(mData);
        }

    }

    @Override
    protected void onStopLoading() {
        if(ENABLELOG) {
            Log.v(TAG,"onStopLoading, loaderid = " + getId());
        }
        super.onStopLoading();
        mSubscriptions.clear();
    }

    @Override
    protected void onReset() {
        if(ENABLELOG) {
            Log.v(TAG,"onReset, loaderid = " + getId());
        }
        super.onReset();
        mSubscriptions.clear();

        mCompleted=false;
        mData=null;
    }

    @Override
    public void deliverResult(T data) {
        if(ENABLELOG) {
            Log.v(TAG,"deliverResult, loaderid = " + getId());
        }
        super.deliverResult(data);
    }

    @Override
    public Throwable getError() {
        return mError;
    }
}
