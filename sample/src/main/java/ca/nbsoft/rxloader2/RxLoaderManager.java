package ca.nbsoft.rxloader2;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;

import ca.nbsoft.rxloader2.support.RxLoaderManagerSupport;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by Nicolas on 2017-04-01.
 */

public abstract class RxLoaderManager {

    public interface ObservableFactory<T>
    {
        Observable<T> getObservable();
    }


    private final Activity mActivity;


    static public RxLoaderManager fromSupport(FragmentActivity activity)
    {
        return RxLoaderManagerSupport.from(activity);
    }

    static public RxLoaderManager fromSupport(android.support.v4.app.Fragment fragment)
    {
        return RxLoaderManagerSupport.from(fragment);
    }

    protected RxLoaderManager(Activity activity) {
        mActivity = activity;
    }


    public <T> Observable<T>  load(int loaderId, boolean forceReload, final ObservableFactory<T> factory)
    {
        if(forceReload)
        {
            if( isLoaderCreated(loaderId))
            {
                return restartLoader(loaderId,null,factory);
            }
            else
            {
                return initLoader(loaderId,null,factory);
            }
        }
        else
        {
            return initLoader(loaderId,null,factory);
        }
    }

    abstract protected <T> Observable<T>  initLoader(int loaderId, Bundle args, final ObservableFactory<T> factory) ;

    abstract protected <T> Observable<T>  restartLoader(int loaderId, Bundle args, final ObservableFactory<T> factory);

    abstract protected boolean isLoaderCreated(int loaderId);

    // TODO: this should not be here, but...
    public boolean isNetworkAvailable()
    {
        ConnectivityManager cm =
                (ConnectivityManager)mActivity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

}
