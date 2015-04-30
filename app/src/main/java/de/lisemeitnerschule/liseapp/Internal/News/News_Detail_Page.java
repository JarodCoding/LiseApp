package de.lisemeitnerschule.liseapp.Internal.News;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.IDocumentLayout;

import de.lisemeitnerschule.liseapp.BaseFragment;
import de.lisemeitnerschule.liseapp.R;
import de.lisemeitnerschule.liseapp.Search;
import de.lisemeitnerschule.liseapp.Utils.CompPathInterpolator;

public class News_Detail_Page extends BaseFragment {
    public Activity activity;
    public static News_Detail_Page newInstance(Activity activity,NewsDetails details) {

        News_Detail_Page fragment = new News_Detail_Page();
        fragment.activity = activity;
        fragment.news     = details    ;
        return fragment;
    }
    private NewsDetails news;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //TODO: show News search and pop back
    @Override
    public boolean usesCustomSearch() {
        return super.usesCustomSearch();
    }

    @Override
    public Search displaySearch(boolean visible) {
        return super.displaySearch(visible);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView view = new ScrollView(container.getContext());
        DisplayMetrics displayMetrics = getActivity().getApplication().getResources().getDisplayMetrics();

        int paddingX = Math.round(8 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        int paddingY = Math.round(8 * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
        view.setPadding(paddingX,paddingY,paddingX,paddingY);
        View newView = LayoutInflater.from(container.getContext()).inflate(R.layout.news_card,container,false);
        TextView Title = (TextView) newView.findViewById(R.id.NewsTitle);
            Title.setText(news.title);
        if(news.Image != null) {
            ImageView Picture = (ImageView) newView.findViewById(R.id.NewsPicture);
            Picture.setImageDrawable(news.Image);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_LEFT, Picture.getId());
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, Picture.getId());
            Title.setLayoutParams(layoutParams);
            view.invalidate();
        }
        DocumentView Teaser = (DocumentView) newView.findViewById(R.id.NewsTeaser);
            Teaser.setText(news.Teaser);
        //DocumentView Body = (DocumentView) newView.findViewById(R.id.NewsBody);
            Body.setText(news.Text);
            Body.setVisibility(View.INVISIBLE);

        view.addView(newView);
        return view;

    }

    @Override
    public void onDestroyView() {


       animation = new ExpandCollapseAnimation(Body, 150, false, getActivity());
       Body.startAnimation(animation);
       super.onDestroyView();
    }
    private DocumentView Body;
    private Animation animation;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Body = (DocumentView) view.findViewById(R.id.NewsBody);
        Body.post(new Runnable() {

            @Override
            public void run() {
                animation = new ExpandCollapseAnimation(Body, 150, true, getActivity());

                Body.startAnimation(animation);
            }

        });

       // view.findViewById(R.id.NewsBody).setVisibility(View.VISIBLE);
    }
}


class NewsDetails{
    public String    title;
    public Long      Date;
    public Drawable  Image;
    public String    Teaser;
    public Spannable Text;
}

/**
 * Class for handling collapse and expand animations.
 * @author Esben Gaarsmand
 *
 */
class ExpandCollapseAnimation extends Animation {
    private View mAnimatedView;
    private int mEndHeight;
    private boolean mType;
    /**
     * This methode can be used to calculate the height and set it for views with wrap_content as height.
     * This should be done before ExpandCollapseAnimation is created.
     * @param activity
     * @param view
     */
    public static int getHeightForWrapContent(Activity activity, View view) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int screenWidth = metrics.widthPixels;

        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY);

        view.measure(widthMeasureSpec, heightMeasureSpec);
        return view.getMeasuredHeight();

    }

    public static int getHeightForWrapContent(Activity activity, DocumentView view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.getLayout().measure(new IDocumentLayout.IProgress<Float>() {
            @Override
            public void onUpdate(Float aFloat) {

            }
        }, new IDocumentLayout.ICancel<Boolean>() {
            @Override
            public Boolean isCancelled() {
                return false;
            }
        });
        return view.getLayout().getMeasuredHeight();
    }

    /**
     * Initializes expand collapse animation, has two types, collapse (1) and expand (0).
     * @param view The view to animate
     * @param duration
     * @param type The type of animation: 0 will expand from gone and 0 size to visible and layout size defined in xml.
     * 1 will collapse view and set to gone
     */
    public ExpandCollapseAnimation(View view, int duration, boolean type,Activity activity) {
        setDuration(duration);
        mAnimatedView = view;
        mEndHeight = getHeightForWrapContent(activity,view);
        setInterpolator(new CompPathInterpolator(0.4f,0.f,0.2f,1f));
        System.err.println(mEndHeight);
        mType = type;
        if(mType) {
            view.getLayoutParams().height = 0;
            view.setVisibility(View.VISIBLE);

        }
    }

    public ExpandCollapseAnimation(DocumentView view, int duration, boolean open,Activity activity) {
        setDuration(duration);
        mAnimatedView = view;
        mEndHeight = getHeightForWrapContent(activity, view);
        System.err.println(mEndHeight);
        setInterpolator(new CompPathInterpolator(0.4f,0.f,0.2f,1f));
        mType = open;
        if(mType) {
            view.getLayoutParams().height = 0;
            view.setVisibility(View.VISIBLE);
        }
    }
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (interpolatedTime < 1.0f) {
            if(mType) {
                mAnimatedView.getLayoutParams().height = (int) (mEndHeight * interpolatedTime);
            } else {
                mAnimatedView.getLayoutParams().height = mEndHeight - (int) (mEndHeight * interpolatedTime);
            }
            mAnimatedView.requestLayout();
        } else {
            if(mType) {
                mAnimatedView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mAnimatedView.requestLayout();
            } else {
                mAnimatedView.getLayoutParams().height = 0;
                mAnimatedView.setVisibility(View.GONE);
                mAnimatedView.requestLayout();
            }
        }
    }
}
