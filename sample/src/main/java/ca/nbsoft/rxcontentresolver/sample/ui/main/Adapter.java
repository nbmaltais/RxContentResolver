package ca.nbsoft.rxcontentresolver.sample.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ca.nbsoft.rxcontentresolver.sample.R;
import ca.nbsoft.rxcontentresolver.sample.provider.person.PersonBean;


/**
 * Created by Nicolas on 2017-04-04.
 */

public class Adapter extends RecyclerView.Adapter<PersonViewHolder> {

    private List<PersonBean> persons = new ArrayList<>();
    private final PersonViewHolder.OnClickHandler handler;

    public Adapter(PersonViewHolder.OnClickHandler handler) {
        this.handler = handler;
    }


    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PersonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person,parent,false),handler);
    }

    @Override
    public void onBindViewHolder(PersonViewHolder holder, int position) {
        holder.bind(persons.get(position));
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }

    public void setPersons(List<PersonBean> persons) {
        this.persons = persons;
        notifyDataSetChanged();
    }

    public PersonBean getPerson(int pos) {
        return persons.get(pos);
    }

    public void removePerson(int pos) {
        persons.remove(pos);
        notifyItemRemoved(pos);
    }
}
