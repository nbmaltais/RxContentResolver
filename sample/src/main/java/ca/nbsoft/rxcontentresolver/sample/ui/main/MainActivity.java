package ca.nbsoft.rxcontentresolver.sample.ui.main;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.widget.EditText;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.nbsoft.rxcontentresolver.QueryToListOperator;
import ca.nbsoft.rxcontentresolver.RxContentResolver;
import ca.nbsoft.rxcontentresolver.sample.R;
import ca.nbsoft.rxcontentresolver.sample.provider.person.Gender;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonBean;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonColumns;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonContentValues;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonCursor;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonSelection;


import ca.nbsoft.rxcontentresolver.sample.ui.detail.DetailActivity;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    CompositeDisposable disposables = new CompositeDisposable();
    @BindView(R.id.recycleView)
    RecyclerView recyclerView;
    @BindView(R.id.last_name)
    EditText lastNameView;
    @BindView(R.id.first_name)
    EditText firstNameView;

    private Adapter adapter;


    static List<PersonBean> mapToPerson(PersonCursor cursor) {
        List<PersonBean> persons = new ArrayList<PersonBean>();

        while (cursor.moveToNext()) {
            persons.add(PersonBean.copy(cursor));
        }

        return persons;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(new PersonViewHolder.OnClickHandler() {
            @Override
            public void onClick(PersonBean person) {
                showPersonDetail(person);
            }
        });
        recyclerView.setAdapter(adapter);

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // callback for drag-n-drop, false to skip this feature
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // callback for swipe to dismiss, removing item from data and adapter
                PersonBean person = adapter.getPerson(viewHolder.getAdapterPosition());
                adapter.removePerson(viewHolder.getAdapterPosition());

                removePerson(person);

            }
        });
        swipeToDismissTouchHelper.attachToRecyclerView(recyclerView);


        loadPersons();

    }



    private void loadPersons() {
        final PersonSelection sel = getSelection();

        boolean useLoader = false;
        if (useLoader) {
            getLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public CursorLoader onCreateLoader(int id, Bundle args) {

                    CursorLoader loader = new CursorLoader(MainActivity.this, sel.uri(), PersonColumns.ALL_COLUMNS, sel.sel(), sel.args(), sel.order());

                    return loader;
                }

                @Override
                public void onLoadFinished(Loader loader, Cursor data) {

                    List<PersonBean> persons = mapToPerson(new PersonCursor(data));

                    adapter.setPersons(persons);
                }

                @Override
                public void onLoaderReset(Loader loader) {

                }
            });
        } else {

            DisposableObserver<List<PersonBean>> disposable = queryPerson(RxContentResolver.from(MainActivity.this), sel)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<List<PersonBean>>() {
                        @Override
                        public void onNext(List<PersonBean> persons) {
                            adapter.setPersons(persons);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
            disposables.add(disposable);
        }
    }

    private void showPersonDetail(PersonBean person) {
        DetailActivity.start(this,person.getId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    static Observable<List<PersonBean>> queryPerson(RxContentResolver contentResolver, PersonSelection sel) {

        return contentResolver.createQuery(sel.uri(), PersonColumns.ALL_COLUMNS, sel.sel(), sel.args(), sel.order(), true)
                .mapToList(new QueryToListOperator.MapperToList<PersonBean>() {
                    @Override
                    public List<PersonBean> apply(@NonNull Cursor cursor) throws Exception {
                        return mapToPerson(new PersonCursor(cursor));
                    }
                });

    }

    PersonSelection getSelection() {
        PersonSelection sel = new PersonSelection();

        return sel;
    }

    @OnClick(R.id.button_add)
    void addPerson() {
        final PersonContentValues person = new PersonContentValues();

        person.putFirstName(firstNameView.getText().toString());
        person.putLastName(lastNameView.getText().toString());
        person.putAge(33);
        person.putGender(Gender.MALE);

        Completable.fromRunnable(new Runnable() {
            @Override
            public void run() {
                person.insert(getContentResolver());
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        clearFields();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        clearFields();
                    }
                });
    }

    private void removePerson(PersonBean person) {
        final PersonSelection sel = new PersonSelection();
        sel.id(person.getId());

        Completable.fromRunnable(new Runnable() {
            @Override
            public void run() {
                sel.delete(getContentResolver());
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    void clearFields() {
        firstNameView.setText("");
        lastNameView.setText("");
    }
}
