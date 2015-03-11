package de.lisemeitnerschule.liseapp.News;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.lisemeitnerschule.liseapp.R;



public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    public NewsAdapter(List<News> NewsList,Activity parent) {
        News.NewsList = NewsList;
        this.Context  = parent.getApplicationContext();
        this.Activity = parent;

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        try {
            News.parseAllNews(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Data

        @Override
        public int getItemCount() {
                return News.NewsList.size();
            }


    //UI

        protected Context  Context;
        protected Activity Activity;

        public static class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public TextView Title;
            public ImageView Picture;
            public com.bluejamesbond.text.DocumentView Teaser;
            public int newsIndex;
            public NewsAdapter parent;
            public NewsViewHolder(View itemView) {
                super(itemView);
                Title = (TextView) itemView.findViewById(R.id.NewsTitle);
                Picture = (ImageView)itemView.findViewById(R.id.NewsPicture);
                Teaser = (com.bluejamesbond.text.DocumentView) itemView.findViewById(R.id.NewsTeaser);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                News_Detail_Page detail_page = News_Detail_Page.newInstance(parent.Activity,News.NewsList.get(newsIndex));
                FragmentManager fragmentManager = ((ActionBarActivity)parent.Activity).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, detail_page)
                        .commit();
            }
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            protected Bundle setupAnimations(View v){
                return  ActivityOptions.makeSceneTransitionAnimation(parent.Activity,v,"card_view").toBundle();

            }
        }

            @Override
            public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_card, parent, false);
                Context = parent.getContext();
                return new NewsViewHolder(v);
            }

            @Override
            public void onBindViewHolder(NewsViewHolder holder, int i) {
                News news = News.NewsList.get(i);
                holder.Title.setText(news.title);
                holder.Picture.setImageDrawable(news.picture);
                holder.Teaser.setText(news.teaser);
                holder.newsIndex = i;
                holder.parent = this;

            }







}
