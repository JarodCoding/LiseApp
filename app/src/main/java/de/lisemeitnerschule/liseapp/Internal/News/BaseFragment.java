package de.lisemeitnerschule.liseapp.Internal.News;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * Created by Beate on 07.04.2015.
 */
public abstract class BaseFragment extends Fragment{
    public ActionBarActivity parent;
    protected void init(ActionBarActivity parent){
        this.parent = parent;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        //TODO add check for public website area: if(
    }

    public abstract boolean isPublic();


    public void search(String s) {
        //TODO: implement search
    }

    public boolean hasSearchQuery() {
        //TODO: implement search
        return false;
    }
}
