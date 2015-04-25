/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package de.lisemeitnerschule.liseapp.Internal.News;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import de.lisemeitnerschule.liseapp.BaseFragment;
import de.lisemeitnerschule.liseapp.Internal.InternalContract;
import de.lisemeitnerschule.liseapp.R;
import de.lisemeitnerschule.liseapp.Search;


public class NewsFragment extends BaseFragment {


    public static NewsFragment newInstance(ActionBarActivity activity) {

        NewsFragment fragment = new NewsFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public boolean usesCustomSearch() {
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false);

    }
    private LinearLayout searchContainer;
    private EditText toolbarSearchView;
    private ImageView searchClearButton;

    @Override
    public Search displaySearch(boolean visible) {
        if(visible)return new NewsSearch(adapter,getActivity().getApplicationContext());
        Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(InternalContract.News.CONTENT_URI, InternalContract.News.PROJECTION_ALL, "", null, InternalContract.News.SORT_ORDER_DEFAULT);
        adapter.swapCursor(cursor);
        return null;
    }
    private SwipeRefreshLayout refreshLayout;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recList = (RecyclerView) view.findViewById(R.id.cardList);
        recList.setHasFixedSize(false);
        recList.setEnabled(true);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        adapter = new NewsAdapter(getActivity());
        recList.setAdapter(adapter);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                NewsSyncAdapter.syncManually(NewsFragment.this.getActivity(),refreshLayout);
            }
        });
        refreshLayout.setEnabled(true);

    }
    private NewsAdapter adapter;
}
class NewsSearch implements Search{
    public NewsSearch(NewsAdapter adapter,Context context){
        this.NewsAdapter = adapter;
        this.context     = context;
    }
    private NewsAdapter NewsAdapter;
    private Context context;
    @Override
    public void search(String s) {
        if(!s.isEmpty()) {
            Cursor cursor = context.getContentResolver().query(InternalContract.News.CONTENT_URI, InternalContract.News.PROJECTION_ALL, InternalContract.News.Title + " LIKE '%" + s + "%' OR " + InternalContract.News.Teaser + " LIKE '%" + s + "%'", null, InternalContract.News.SORT_ORDER_DEFAULT);
            NewsAdapter.swapCursor(cursor);
        }
    }


}
