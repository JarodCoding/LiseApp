package de.lisemeitnerschule.liseapp.Internal.News;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.style.TextAlignment;

import de.lisemeitnerschule.liseapp.R;

public class News_Detail_Page extends android.support.v4.app.Fragment {
    public Activity activity;
    public static News_Detail_Page newInstance(Activity activity,NewsDetails details) {

        News_Detail_Page fragment = new News_Detail_Page();
        fragment.setRetainInstance(true);
        fragment.activity = activity;
        fragment.news     = details    ;
        return fragment;
    }
    private NewsDetails news;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView view = new ScrollView(container.getContext());
        view.setPadding(16,16,16,16);
        View newView = LayoutInflater.from(container.getContext()).inflate(R.layout.news_card,container,false);
        TextView Title = (TextView) newView.findViewById(R.id.NewsTitle);
            Title.setText(news.title);
        ImageView Picture = (ImageView)newView.findViewById(R.id.NewsPicture);
            Picture.setImageDrawable(news.Image);
        DocumentView Teaser = (DocumentView) newView.findViewById(R.id.NewsTeaser);
            Teaser.setText(news.Teaser);
        view.addView(newView);
        return view;

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.card_view).findViewById(R.id.linerLayout);

        DocumentView text = new DocumentView(view.getContext(), DocumentView.FORMATTED_TEXT);  // Support spanned text
            text.getDocumentLayoutParams().setTextAlignment(TextAlignment.JUSTIFIED);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.MATCH_PARENT);
            text.setLayoutParams(params);
            text.getDocumentLayoutParams().setAntialias(true);
            text.getDocumentLayoutParams().setHyphen("-");
            text.getDocumentLayoutParams().setTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
            text.setCacheConfig(DocumentView.CacheConfig.AUTO_QUALITY);
            text.getDocumentLayoutParams().setTextSize(14);
            text.getDocumentLayoutParams().setTextSubPixel(true);
            text.setSmoothScrollingEnabled(true);
            text.setPadding(16,16,16,16);
            text.setText(news.Text);
        layout.addView(text);




    }
}
class NewsDetails{
    public String    title;
    public Long      Date;
    public Drawable  Image;
    public String    Teaser;
    public Spannable Text;
}

