package ca.nbsoft.rxcontentresolver.sample.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.nbsoft.rxcontentresolver.RxContentResolver;
import ca.nbsoft.rxcontentresolver.sample.R;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonBean;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonColumns;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonCursor;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonSelection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();

    public static void start(Context context, long personId)
    {
        Intent intent = new Intent(context,DetailActivity.class);
        intent.putExtra("PersonId",personId);
        context.startActivity(intent);
    }

    @BindView(R.id.first_name)
    TextView firstNameView;

    @BindView(R.id.last_name)
    TextView lastNameView;

    @BindView(R.id.gender)
    TextView genderView;

    @BindView(R.id.age)
    TextView ageView;

    CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        long personId = getIntent().getLongExtra("PersonId",-1);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadPerson(personId);
    }

    private void loadPerson(long personId) {
        PersonSelection sel = new PersonSelection();
        sel.id(personId);

        DisposableObserver<PersonBean> disposable = RxContentResolver.from(this)
                .createQuery(sel.uri(), PersonColumns.ALL_COLUMNS, sel.sel(), sel.args(), sel.order(), false)
                .mapToOne(new Function<Cursor, PersonBean>() {
                    @Override
                    public PersonBean apply(@NonNull Cursor cursor) throws Exception {
                        return PersonBean.copy(new PersonCursor(cursor));
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<PersonBean>() {
                    @Override
                    public void onNext(PersonBean personBean) {
                        bindPerson(personBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG,"Failed to load",e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        disposables.add(disposable);
    }

    private void bindPerson(PersonBean person) {
        firstNameView.setText(person.getFirstName());
        lastNameView.setText(person.getLastName());
        genderView.setText(person.getGender().toString());
        ageView.setText(Integer.toString(person.getAge()));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }
}
