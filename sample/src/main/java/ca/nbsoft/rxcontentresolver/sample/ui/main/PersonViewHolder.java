package ca.nbsoft.rxcontentresolver.sample.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.nbsoft.rxcontentresolver.sample.R;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonBean;


/**
 * Created by Nicolas on 2017-04-04.
 */

public class PersonViewHolder extends RecyclerView.ViewHolder {

    public interface OnClickHandler
    {
        void onClick(PersonBean person);
    }

    @BindView(R.id.name)
    TextView nameView;

    private PersonBean person;
    private final OnClickHandler handler;

    public PersonViewHolder(View itemView, OnClickHandler handler) {
        super(itemView);
        this.handler=handler;
        ButterKnife.bind(this,itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonViewHolder.this.handler.onClick(person);
            }
        });
    }

    public void bind(PersonBean personBean) {
        person = personBean;
        nameView.setText(personBean.getFirstName() + " " + personBean.getLastName());

    }
}
