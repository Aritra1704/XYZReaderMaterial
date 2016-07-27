package com.arpaul.xyzreadermaterial.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.arpaul.utilitieslib.ColorUtils;
import com.arpaul.utilitieslib.LogUtils;
import com.arpaul.xyzreadermaterial.R;
import com.arpaul.xyzreadermaterial.data.ArticleLoader;

/**
 * Created by Aritra on 25-07-2016.
 */
public class ArticleDetailFragmentNew extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AppBarLayout.OnOffsetChangedListener {

    private Cursor mCursor;
    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArticleDetailFragmentN";

    private long mItemId;
    private int mStatusBarFullOpacityBottom;
    private View mRootView;
    private boolean mIsCard = false;
    private AppBarLayout app_bar;
    private CollapsingToolbarLayout toolbar_layout;
    private ImageView photo;
    private LinearLayout meta_bar;
    private TextView article_title, article_byline, tvtitle, article_body;
    private Toolbar toolbar;
    private FloatingActionButton share_fab;
    private int mMutedColor = 0xFF333333;
    private ImageView mPhotoView;
    private Context context;

    public static ArticleDetailFragmentNew newInstance(long itemId){
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragmentNew fragment = new ArticleDetailFragmentNew();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail_new, container, false);

        app_bar             = (AppBarLayout)mRootView.findViewById(R.id.app_bar);
        toolbar_layout      = (CollapsingToolbarLayout)mRootView.findViewById(R.id.toolbar_layout);
        photo               = (ImageView)mRootView.findViewById(R.id.photo);
        meta_bar            = (LinearLayout)mRootView.findViewById(R.id.meta_bar);
        article_title       = (TextView)mRootView.findViewById(R.id.article_title);
        article_byline      = (TextView)mRootView.findViewById(R.id.article_byline);
        tvtitle             = (TextView)mRootView.findViewById(R.id.tvtitle);
        article_body        = (TextView)mRootView.findViewById(R.id.article_body);
        toolbar             = (Toolbar)mRootView.findViewById(R.id.toolbar);
        share_fab           = (FloatingActionButton)mRootView.findViewById(R.id.share_fab);


        context = getActivity();

        bindViews();
        app_bar.addOnOffsetChangedListener(this);

//        toolbar.inflateMenu(R.menu.menu_main);
        startAlphaAnimation(tvtitle, 0, View.INVISIBLE);

//        updateStatusBar();

        share_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        return mRootView;
    }

    private void bindViews(){
        article_body.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            article_title.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            tvtitle.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            article_byline.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            article_body.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                /*Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);*/
                                photo.setImageBitmap(imageContainer.getBitmap());
//                                meta_bar.setBackgroundColor(mMutedColor);
//                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        }  else {
            article_title.setText("N/A");
            tvtitle.setText("N/A");
            article_byline.setText("N/A" );
            article_body.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        bindViews();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
        LogUtils.debug("percentage_show",""+percentage);
    }

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;
    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if(!mIsTheTitleVisible) {
                startAlphaAnimation(tvtitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
                toolbar.setBackgroundColor(ColorUtils.getColor(context,R.color.dark));
            }
        } else {
            if (mIsTheTitleVisible) {
                startAlphaAnimation(tvtitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
                toolbar.setBackgroundColor(ColorUtils.getColor(context,R.color.transparent));
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(meta_bar, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(meta_bar, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public void startAlphaAnimation (View v, long duration, int visibility) {

//        if(visibility == View.VISIBLE)
//            toolbar.setBackgroundColor(ColorUtils.getColor(context,R.color.dark));
//        else
//            toolbar.setBackgroundColor(ColorUtils.getColor(context,R.color.transparent));

        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    /*public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }*/
}
