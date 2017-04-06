package ca.nbsoft.rxloader2.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import ca.nbsoft.rxloader2.RxLoaderGetError;
import ca.nbsoft.rxloader2.RxLoaderManager;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;


/**
 * Created by Nicolas on 2016-12-09.
 */

public class RxLoaderManagerSupport extends RxLoaderManager {



    public interface LoaderFactory<T>
    {
        RxLoaderBase<T> createLoader(Context context);
    }


    private final FragmentActivity mActivity;

    public static RxLoaderManagerSupport from(FragmentActivity activity)
    {
        return new RxLoaderManagerSupport(activity);
    }

    public static RxLoaderManagerSupport from(Fragment fragment) {
        return new RxLoaderManagerSupport(fragment.getActivity());
    }

    public RxLoaderManagerSupport(FragmentActivity activity) {
        super(activity);
        mActivity=activity;
    }


    @Override
    protected <T> Observable<T>  initLoader(int loaderId, Bundle args, final ObservableFactory<T> factory) {
        BehaviorSubject<T> subject = BehaviorSubject.create();
        LoaderManager.LoaderCallbacks<T> callbacks = createCallbackWithSubject(mActivity,createLoaderFactory(factory), subject);
        mActivity.getSupportLoaderManager().initLoader(loaderId, args,callbacks);
        return subject;
    }

    @Override
    protected <T> Observable<T>  restartLoader(int loaderId, Bundle args, final ObservableFactory<T> factory) {
        BehaviorSubject<T> subject = BehaviorSubject.create();
        LoaderManager.LoaderCallbacks<T> callbacks = createCallbackWithSubject(mActivity,createLoaderFactory(factory), subject);
        mActivity.getSupportLoaderManager().restartLoader(loaderId, args,callbacks);
        return subject;
    }

    @Override
    protected boolean isLoaderCreated(int loaderId) {
        return mActivity.getSupportLoaderManager().getLoader(loaderId) !=null;
    }



    static private <T> LoaderFactory<T> createLoaderFactory(final ObservableFactory<T> factory) {
        return new LoaderFactory<T>() {
            @Override
            public RxLoaderBase<T> createLoader(Context context) {
                return new RxLoader<T>(context,factory.getObservable());
            }
        };
    }


    @NonNull
    static private <T> LoaderManager.LoaderCallbacks<T> createCallbackWithSubject(final Context context,
                                                                                  final LoaderFactory<T> loaderFactory,
                                                                                  final Subject<T> subject) {
        return new LoaderManager.LoaderCallbacks<T>() {
            @Override
            public Loader<T> onCreateLoader(int id, Bundle args) {
                return loaderFactory.createLoader(context);
            }

            @Override
            public void onLoadFinished(Loader<T> loader, T data) {
                RxLoaderGetError<T> errorProvider = (RxLoaderGetError<T>)loader;
                if(data==null)
                {
                    subject.onError(errorProvider.getError());
                }
                else
                {
                    subject.onNext(data);
                }
            }

            @Override
            public void onLoaderReset(Loader<T> loader) {

            }
        };
    }


}
