package ca.nbsoft.rxloader2.support;

import android.content.Context;
import android.support.v4.content.Loader;

import ca.nbsoft.rxloader2.RxLoaderGetError;

/**
 * Created by Nicolas on 2017-04-01.
 */

abstract class  RxLoaderBase<T> extends Loader<T> implements RxLoaderGetError<T> {

    public RxLoaderBase(Context context) {
        super(context);
    }
}
