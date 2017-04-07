RxContentResolver
-----------------

<b>ContentResolver meets rxjava2!</b>

This library contain classes extracted from the excellent [SqlBrite](https://github.com/square/sqlbrite) library from square.
The only reason this library exists is because I needed an rxjava2 version of the BriteContentReslover.

Please use the original library for a full featured reactive database implementation!

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and:

```gradle
dependencies {
    compile 'com.github.nbmaltais:rxcontentresolver:{latest version}'
}
```

## Usage

Create a query
```
QueryObservable queryObservable = RxContentResolver.from(getContext())
                                                .createQuery(uri, projection, selection, args, order, false);
```

Map the cursor to an single object:

```
Observable<Bean> beanObservale = queryObservable.mapToOne(new Function<Cursor, PersonBean>() {
                                    @Override
                                    public Bean apply(@NonNull Cursor cursor) throws Exception {
                                        return Bean.create(cursor);
                                    }
                                })
```

Map the cursor to a list:

```
Observable<List<Bean>> beanObservale = queryObservable..mapToList(new QueryToListOperator.MapperToList<List>() {
                    @Override
                    public List<Bean> apply(@NonNull Cursor cursor) throws Exception {
                        return beanListFromCursor(cursor);
                    }
                });
```
