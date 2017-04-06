package ca.nbsoft.rxloader2;

import android.content.Context;
import android.support.v4.content.Loader;

/**
 * Created by Nicolas on 2017-03-15.
 */

public interface RxLoaderGetError<T> {

    Throwable getError();
}
