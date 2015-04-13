package de.lisemeitnerschule.liseapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

/**
 * Created by Beate on 07.04.2015.
 */
public abstract class BaseFragment extends Fragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }
    private View mainView;
    public void init(View mainView){
        this.mainView = mainView;
    }


    public boolean isPublic(){
        return true;
    }
    public boolean usesCustomSearch(){
        return false;
    }

    //Displays Global Search Overwrite to implement Local Search
    public Search displaySearch(boolean visible){
        if(usesCustomSearch())throw new IllegalStateException("This Fragment has specifyed to be implementing a custom Search but the displaySearch method is not overwritten");
        if(visible){
            mainView.setVisibility(View.GONE);
            RecyclerView resultView = new RecyclerView(getActivity());
            //TODO implement search
            return null;
        }
        mainView.setVisibility(View.VISIBLE);
        return null;
    }

}

class DefaultSearch implements Search{


    @Override
    public void search(String s) {
    }

}
class DefaultSearchAdapter extends RecyclerView.Adapter<DefaultSearchAdapter.DefaultSearchViewHolder> implements Filterable{

    @Override
    public DefaultSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(DefaultSearchViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    class DefaultSearchViewHolder extends RecyclerView.ViewHolder{

        public DefaultSearchViewHolder(View itemView) {
            super(itemView);
        }
    }
}