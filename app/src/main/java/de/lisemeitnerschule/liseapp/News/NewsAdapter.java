package de.lisemeitnerschule.liseapp.News;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluejamesbond.text.style.JustifiedSpan;

import java.util.List;

import de.lisemeitnerschule.liseapp.R;



public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    public NewsAdapter(List<News> NewsList,Context context) {
        this.NewsList = NewsList;
        this.Context = context;
    }



    //Data

    protected List<News> NewsList;

        @Override
        public int getItemCount() {
                return NewsList.size();
            }



    //UI

        protected Context Context;

        public static class NewsViewHolder extends RecyclerView.ViewHolder {
            public TextView Title;
            public ImageView Picture;
            public com.bluejamesbond.text.DocumentView Teaser;

            public NewsViewHolder(View itemView) {
                super(itemView);
                Title = (TextView) itemView.findViewById(R.id.NewsTitle);
                Picture = (ImageView)itemView.findViewById(R.id.NewsPicture);
                Teaser = (com.bluejamesbond.text.DocumentView) itemView.findViewById(R.id.NewsTeaser);
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
                News news = NewsList.get(i);
                holder.Title.setText(news.title);
                holder.Picture.setImageDrawable(news.picture);
                news.teaser.setSpan(new JustifiedSpan(), 0, news.teaser.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                holder.Teaser.setText(news.teaser);


            }






}
