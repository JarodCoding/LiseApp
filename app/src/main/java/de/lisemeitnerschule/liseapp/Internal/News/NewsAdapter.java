package de.lisemeitnerschule.liseapp.Internal.News;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.style.JustifiedSpan;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;

import de.lisemeitnerschule.liseapp.Internal.InternalContract;
import de.lisemeitnerschule.liseapp.R;


public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder>  {

    public NewsAdapter(Activity parent) {
        //UI
            this.Context  = parent.getApplicationContext();
            this.Activity = parent;

        //DATA
            Cursor cursor = Cursor = Context.getContentResolver().query(InternalContract.News.CONTENT_URI,InternalContract.News.PROJECTION_ALL,"",null,InternalContract.News.SORT_ORDER_DEFAULT);
            DataValid = cursor != null;
            RowIdColumn = DataValid ? Cursor.getColumnIndex(InternalContract.News._ID) : -1;
            DataSetObserver = new NotifyingDataSetObserver();
            if (Cursor != null) {
                Cursor.registerDataSetObserver(DataSetObserver);
            }
    }

        @Override
        public int getItemCount() {
            if (DataValid && Cursor != null) {
                return Cursor.getCount();
            }
            DataValid = false;
            return 0;
            }
    @Override
    public long getItemId(int position) {
        if (DataValid && Cursor != null && Cursor.moveToPosition(position)) {
            return Cursor.getLong(RowIdColumn);
        }
        return 0;
    }
    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    //UI

        protected Context  Context;
        protected Activity Activity;

    //DATA

        public Cursor Cursor;

        private boolean DataValid;

        private int RowIdColumn;

        public DataSetObserver DataSetObserver;



            @Override
            public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_card, parent, false);
                Context = parent.getContext();
                return new NewsViewHolder(v);
            }
            public void changeCursor(Cursor cursor) {
                Cursor old = swapCursor(cursor);
                if (old != null) {
                    old.close();
                }
            }
        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor == Cursor) {
                return null;
            }
            final Cursor oldCursor = Cursor;
            if (oldCursor != null && DataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(DataSetObserver);
            }
            Cursor = newCursor;
            if (Cursor != null) {
                if (DataSetObserver != null) {
                    Cursor.registerDataSetObserver(DataSetObserver);
                }
                RowIdColumn = newCursor.getColumnIndexOrThrow("_id");
                DataValid = true;
                notifyDataSetChanged();
            } else {
                RowIdColumn = -1;
                DataValid = false;
                notifyDataSetChanged();
                //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            }
            return oldCursor;
        }
        private class NotifyingDataSetObserver extends DataSetObserver {
            @Override
            public void onChanged() {
                super.onChanged();
                DataValid = true;
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                DataValid = false;
                notifyDataSetChanged();
                //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            }
        }
    public static class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView Title;
        public ImageView Picture;
        public com.bluejamesbond.text.DocumentView Teaser;
        public int newsIndex;
        public NewsAdapter parent;
        public DocumentView Body;
        public NewsViewHolder(View itemView) {
            super(itemView);
            Title = (TextView) itemView.findViewById(R.id.NewsTitle);
            Picture = (ImageView)itemView.findViewById(R.id.NewsPicture);
            Teaser = (com.bluejamesbond.text.DocumentView) itemView.findViewById(R.id.NewsTeaser);
            Body = (com.bluejamesbond.text.DocumentView) itemView.findViewById(R.id.NewsBody);

            itemView.setOnClickListener(this);
        }
        public NewsDetails generateDetails(){
            NewsDetails res = new NewsDetails();
            res.title = this.Title.getText().toString();
            res.Teaser= this.Teaser.getText().toString();
            res.Image = this.Picture.getDrawable();
            parent.Cursor.moveToPosition(newsIndex);
            res.Date  = parent.Cursor.getLong(parent.Cursor.getColumnIndex(InternalContract.News.Date));
            res.Text  = (Spannable)Html.fromHtml(parent.Cursor.getString(parent.Cursor.getColumnIndex(InternalContract.News.Text)));
            res.Text.setSpan(new JustifiedSpan(),0,res.Text.length(),0);
            return res;
        }

        @Override
        public void onClick(View v) {
            News_Detail_Page detail_page = News_Detail_Page.newInstance(parent.Activity,generateDetails());
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
            public void onBindViewHolder(final NewsViewHolder holder, int position) {
                if (!DataValid) {
                    throw new IllegalStateException("this should only be called when the cursor is valid");
                }
                if (!Cursor.moveToPosition(position)) {
                    throw new IllegalStateException("couldn't move cursor to position " + position);
                }
                //image
                    final File image = new File(this.Context.getCacheDir(), "/images/"+Cursor.getString(Cursor.getColumnIndex(InternalContract.News.Image)));
                    System.err.println(image);
                    holder.Picture.getViewTreeObserver()
                            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                // Wait until layout to call Picasso
                                @Override
                                public void onGlobalLayout() {
                                    // Ensure we call this only once
                                    holder.Picture.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                    RequestCreator bitmap = Picasso.with(Context)
                                                                .load(image)
                                                                .resize(holder.Picture.getWidth(), 0);
                                    System.err.println(image);
                                    bitmap.into(holder.Picture);
                                }
                            });
                //title
                    String title = Cursor.getString(Cursor.getColumnIndex(InternalContract.News.Title));
                    System.err.println(title);
                    holder.Title.setText(title);

                //teaser
                    String teaser = Cursor.getString(Cursor.getColumnIndex(InternalContract.News.Teaser));
                    System.err.println(teaser);
                    holder.Teaser.setText(teaser);
                //Text
                    Spannable Text  = (Spannable)Html.fromHtml(Cursor.getString(Cursor.getColumnIndex(InternalContract.News.Text)));
                        Text.setSpan(new JustifiedSpan(),0,Text.length(),0);
                    holder.Body.setText(Text);

                //Data
                    holder.newsIndex = Cursor.getPosition();
                    holder.parent = this;

            }







}
