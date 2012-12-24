package net.somethingdreadful.MAL;

import org.apache.commons.lang3.text.WordUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class DetailView extends SherlockFragmentActivity implements DetailsBasicFragment.IDetailsBasicAnimeFragment,
EpisodesPickerDialogFragment.DialogDismissedListener, MangaProgressDialogFragment.MangaDialogDismissedListener {

    MALManager mManager;
    PrefManager pManager;
    Context context;
    int recordID;
    ActionBar actionBar;
    AnimeRecord mAr;
    MangaRecord mMr;
    String recordType;

    DetailsBasicFragment bfrag;
    GenericCardFragment SynopsisFragment;
    GenericCardFragment ProgressFragment;
    GenericCardFragment StatusFragment;
    GenericCardFragment ScoreFragment;
    FragmentManager fm;
    EpisodesPickerDialogFragment epd;
    MangaProgressDialogFragment mpdf;

    TextView SynopsisView;
    TextView RecordTypeView;
    TextView RecordStatusView;
    TextView MyStatusView;
    TextView ProgressCurrentView;
    TextView ProgressTotalView;
    ImageView CoverImageView;
    TextView MyScoreView;
    TextView MemberScoreView;
    RatingBar MALScoreBar;
    RatingBar MyScoreBar;


    Spanned SynopsisText;
    String ProgressText;
    String TotalProgressText;
    String RecordStatusText;
    String RecordTypeText;
    String MyStatusText;
    String MyScoreText;
    String MemberScoreText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        fm = getSupportFragmentManager();

        bfrag = (DetailsBasicFragment) fm.findFragmentById(R.id.DetailsFragment);

        SynopsisFragment = (GenericCardFragment) fm.findFragmentById(R.id.SynopsisFragment);
        SynopsisFragment.setArgsSensibly("SYNOPSIS", R.layout.card_layout_content_synopsis, GenericCardFragment.CONTENT_TYPE_SYNOPSIS, false);
        SynopsisFragment.inflateContentStub();

        //        SynopsisFragment.getView().setOnClickListener(new OnClickListener(){
        //
        //            @Override
        //            public void onClick(View v) {
        //                //Code for doing something when the card is clicked goes here
        //                //Probably going to be expand collapse in portrait, but disabled and use a scrollview in landscape since cards will scroll horizontally
        //            }
        //
        //        });

        ProgressFragment = (GenericCardFragment) fm.findFragmentById(R.id.ProgressFragment);
        ProgressFragment.setArgsSensibly("PROGRESS", R.layout.card_layout_progress, GenericCardFragment.CONTENT_TYPE_PROGRESS, true);
        ProgressFragment.inflateContentStub();

        ProgressFragment.getView().setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                //TODO: Added some kind of clicking feedback, like highlight it or something
                showProgressDialog();
            }

        });

        StatusFragment = (GenericCardFragment) fm.findFragmentById(R.id.StatusFragment);
        StatusFragment.setArgsSensibly("MAL STATS", R.layout.card_layout_status, GenericCardFragment.CONTENT_TYPE_INFO, false);
        StatusFragment.inflateContentStub();

        ScoreFragment = (GenericCardFragment) fm.findFragmentById(R.id.ScoreFragment);
        ScoreFragment.setArgsSensibly("SCORE", R.layout.card_layout_score, GenericCardFragment.CONTENT_TYPE_SCORE, false);
        ScoreFragment.inflateContentStub();


        context = getApplicationContext();
        mManager = new MALManager(context);
        pManager = new PrefManager(context);

        //Get the recordID, passed in from the calling activity
        recordID = getIntent().getIntExtra("net.somethingdreadful.MAL.recordID", 1);

        //Get the recordType, also passed from calling activity
        //Record type will determine how the detail view lays out itself
        recordType = getIntent().getStringExtra("net.somethingdreadful.MAL.recordType");

        // Set up the action bar.
        actionBar = getSupportActionBar();
        //        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //        final FrameLayout layout = (FrameLayout) bfrag.getView().findViewById(R.id.backgroundContainer);
        //
        //
        //    	ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
        //    	if (viewTreeObserver.isAlive()) {
        //    	  viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
        //    	    public void onGlobalLayout() {
        //    	      layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        //
        //    	      int synopsisOffset = layout.getHeight();
        //    	      synopsisOffset -= layout.findViewById(R.id.SynopsisLabel).getHeight();
        //    	      System.out.println(synopsisOffset);
        //
        //
        //    	      LayoutParams params = (LayoutParams) layout.findViewById(R.id.SynopsisLabel).getLayoutParams();
        //    	      params.setMargins(0, synopsisOffset, 0, 0);
        //    	      layout.findViewById(R.id.SynopsisLabel).setLayoutParams(params);
        //
        //
        //
        //    	    }
        //    	  });
        //    	}


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_detail_view, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_Share:
                Share();
                break;
            case R.id.SetStatus_InProgress:
                setStatus(1);
                break;
            case R.id.SetStatus_Complete:
                setStatus(2);
                break;
            case R.id.SetStatus_OnHold:
                setStatus(3);
                break;
            case R.id.SetStatus_Dropped:
                setStatus(4);
                break;
            case R.id.SetStatus_Planned:
                setStatus(5);
                break;
        }

        return true;
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onPause()
    {
        super.onPause();

        try
        {
            if("anime".equals(recordType))
            {
                if (mAr.getDirty() == 1)
                {
                    writeDetails(mAr);
                }
            }
            else
            {
                if (mMr.getDirty() == 1)
                {
                    writeDetails(mMr);
                }
            }
        }
        catch (NullPointerException npe)
        {

        }
    }

    //Called after the basic fragment is finished it's setup, populate data into it
    @Override
    public void basicFragmentReady() {

        CoverImageView = (ImageView) bfrag.getView().findViewById(R.id.detailCoverImage);

        getDetails(recordID);
    }

    public void getDetails(int id)
    {
        new getDetailsTask().execute();
    }

    public void showProgressDialog() // Just a function to keep logic out of the switch statement
    {
        if ("anime".equals(recordType))
        {
            showEpisodesWatchedDialog();
        }
        else
        {
            showMangaProgressDialog();
        }
    }

    public void showEpisodesWatchedDialog()
    {
        //Standard code for setting up a dialog fragment
        //Note we use setStyle to change the theme, the default light styled dialog didn't look good so we use the dark dialog
        epd = new EpisodesPickerDialogFragment();

        if (Build.VERSION.SDK_INT >= 11)
        {
            epd.setStyle(SherlockDialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        }
        else
        {
            epd.setStyle(SherlockDialogFragment.STYLE_NORMAL, 0);
        }
        epd.show(fm, "fragment_EditEpisodesWatchedDialog");
    }

    public void showMangaProgressDialog()
    {
        //Standard code for setting up a dialog fragment
        //        Toast.makeText(context, "TODO: Make a MangaProgressFragment", Toast.LENGTH_SHORT).show();
        mpdf = new MangaProgressDialogFragment();
        if (Build.VERSION.SDK_INT >= 11)
        {
            mpdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        }
        else
        {
            mpdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, 0);
        }
        mpdf.show(fm, "fragment_EditMangaProgressDialog");
    }

    public class getDetailsTask extends AsyncTask<Void, Boolean, GenericMALRecord>
    {

        int mID;
        MALManager mmManager;
        ActionBar bar;
        ImageDownloader imageDownloader = new ImageDownloader(context);
        String internalType;

        @Override
        protected void onPreExecute()
        {
            mID = recordID;
            mmManager = mManager;
            internalType = recordType;
        }

        @Override
        protected GenericMALRecord doInBackground(Void... arg0) {

            if ("anime".equals(internalType))
            {
                mAr = mmManager.getAnimeRecordFromDB(mID);

                //Basically I just use publishProgress as an easy way to display info we already have loaded sooner
                //This way, I can let the database work happen on the background thread and then immediately display it while
                //the synopsis loads if it hasn't previously been downloaded.
                publishProgress(true);

                if ((mAr.getSynopsis() == null) || (mAr.getMemberScore() == null))
                {
                    mAr = mmManager.updateWithDetails(mID, mAr);
                }

                return mAr;
            }
            else
            {
                mMr = mmManager.getMangaRecordFromDB(mID);

                //Basically I just use publishProgress as an easy way to display info we already have loaded sooner
                //This way, I can let the database work happen on the background thread and then immediately display it while
                //the synopsis loads if it hasn't previously been downloaded.
                publishProgress(true);

                if ((mMr.getSynopsis() == null) || (mMr.getMemberScore() == null))
                {
                    mMr = mmManager.updateWithDetails(mID, mMr);
                }

                return mMr;
            }

        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);

            if ("anime".equals(internalType)) {
                actionBar.setTitle(mAr.getName());

                CoverImageView.setImageDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mAr.getImageUrl())));
                //                RecordStatusView.setText(WordUtils.capitalize(mAr.getRecordStatus()));
                //                RecordTypeView.setText(mAr.getRecordType());
                //                MyStatusView.setText(WordUtils.capitalize(mAr.getMyStatus()));

                ProgressText = Integer.toString(mAr.getPersonalProgress());
                TotalProgressText = mAr.getTotal();
                MyStatusText = WordUtils.capitalize(mAr.getMyStatus());
                MyScoreText = mAr.getFormattedScore();

                ProgressCurrentView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountCurrent);
                ProgressTotalView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountTotal);
                MyStatusView = (TextView) ProgressFragment.getView().findViewById(R.id.myStatus);



                if(ProgressTotalView != null)
                {
                    ProgressCurrentView.setText(ProgressText);
                    ProgressTotalView.setText("/" + TotalProgressText);
                    MyStatusView.setText(MyStatusText);

                }

                RecordStatusText = WordUtils.capitalize(mAr.getRecordStatus());
                RecordTypeText = WordUtils.capitalize(mAr.getRecordType());
                MemberScoreText = mAr.getMemberScore();

                RecordTypeView = (TextView) StatusFragment.getView().findViewById(R.id.mediaType);
                RecordStatusView = (TextView) StatusFragment.getView().findViewById(R.id.mediaStatus);

                if (RecordStatusView != null)
                {
                    RecordTypeView.setText(RecordTypeText);
                    RecordStatusView.setText(RecordStatusText);
                }

                MALScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MALScoreBar);
                MyScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MyScoreBar);

                if (MALScoreBar != null)
                {
                    MALScoreBar.setRating(Float.parseFloat(MemberScoreText) / 2);
                    MyScoreBar.setRating(Float.parseFloat(MyScoreText) / 2);
                }

            }
            else {
                actionBar.setTitle(mMr.getName());

                CoverImageView.setImageDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mMr.getImageUrl())));
                //                RecordStatusView.setText(WordUtils.capitalize(mMr.getRecordStatus()));
                //                RecordTypeView.setText(mMr.getRecordType());
                //                MyStatusView.setText(WordUtils.capitalize(mMr.getMyStatus()));

                ProgressText = Integer.toString(mMr.getPersonalProgress());
                TotalProgressText = mMr.getTotal();
                MyStatusText = WordUtils.capitalize(mMr.getMyStatus());
                MyScoreText = mMr.getFormattedScore();


                ProgressCurrentView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountCurrent);
                ProgressTotalView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountTotal);
                MyStatusView = (TextView) ProgressFragment.getView().findViewById(R.id.myStatus);



                if(ProgressTotalView != null)
                {
                    ProgressCurrentView.setText(ProgressText);
                    ProgressTotalView.setText("/" + TotalProgressText);
                    MyStatusView.setText(MyStatusText);

                }

                RecordStatusText = WordUtils.capitalize(mMr.getRecordStatus());
                RecordTypeText = WordUtils.capitalize(mMr.getRecordType());
                MemberScoreText = mMr.getMemberScore();

                RecordTypeView = (TextView) StatusFragment.getView().findViewById(R.id.mediaType);
                RecordStatusView = (TextView) StatusFragment.getView().findViewById(R.id.mediaStatus);
                if (RecordStatusView != null)
                {
                    RecordTypeView.setText(RecordTypeText);
                    RecordStatusView.setText(RecordStatusText);
                }

                MALScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MALScoreBar);
                MyScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MyScoreBar);

                if (MALScoreBar != null)
                {
                    MALScoreBar.setRating(Float.parseFloat(MemberScoreText));
                    MyScoreBar.setRating(Float.parseFloat(MyScoreText) / 2);
                }
            }
        }

        @Override
        protected void onPostExecute(GenericMALRecord gr) {
            SynopsisView = (TextView) SynopsisFragment.getView().findViewById(R.id.SynopsisContent);
            MemberScoreView = (TextView) StatusFragment.getView().findViewById(R.id.memberScore);
            if (SynopsisView != null)
            {
                SynopsisView.setText(gr.getSpannedSynopsis(), TextView.BufferType.SPANNABLE);
                SynopsisText = gr.getSpannedSynopsis();
                MemberScoreText = gr.getMemberScore();
                MemberScoreView.setText(MemberScoreText);

            }
            else
            {
                SynopsisText = gr.getSpannedSynopsis();
                MemberScoreText = gr.getMemberScore();
            }
        }
    }

    public class writeDetailsTask extends AsyncTask<GenericMALRecord, Void, Boolean> {
        MALManager internalManager;
        GenericMALRecord internalGr;
        String internalType;

        @Override
        protected void onPreExecute()
        {
            internalManager = mManager;
            internalType = recordType;

        }


        @Override
        protected Boolean doInBackground(GenericMALRecord... gr) {

            boolean result;


            if ("anime".equals(internalType))
            {
                internalManager.saveItem((AnimeRecord) gr[0], false);
                result = internalManager.writeDetailsToMAL(gr[0], internalManager.TYPE_ANIME);
            }
            else
            {
                internalManager.saveItem((MangaRecord) gr[0], false);
                result = internalManager.writeDetailsToMAL(gr[0], internalManager.TYPE_MANGA);
            }


            if (result == true)
            {
                gr[0].setDirty(gr[0].CLEAN);

                if ("anime".equals(internalType))
                {
                    internalManager.saveItem((AnimeRecord) gr[0], false);
                }
                else
                {
                    internalManager.saveItem((MangaRecord) gr[0], false);
                }
            }

            return result;


        }

    }

    //Dialog returns new value, do something with it
    @Override
    public void onDialogDismissed(int newValue) {
        if ("anime".equals(recordType))
        {
            if (newValue == mAr.getPersonalProgress())
            {

            }
            else
            {
                if (Integer.parseInt(mAr.getTotal()) != 0)
                {
                    if (newValue == Integer.parseInt(mAr.getTotal()))
                    {
                        mAr.setMyStatus(mAr.STATUS_COMPLETED);
                    }
                    if (newValue == 0)
                    {
                        mAr.setMyStatus(mAr.STATUS_PLANTOWATCH);
                    }

                }

                mAr.setEpisodesWatched(newValue);
                mAr.setDirty(mAr.DIRTY);


                ProgressCurrentView.setText(Integer.toString(newValue));

            }
        }

    }

    //Create new write task and run it
    public void writeDetails(GenericMALRecord gr)
    {
        new writeDetailsTask().execute(gr);
    }

    public void setStatus(int pickValue)
    {
        if ("anime".equals(recordType))
        {
            switch (pickValue)
            {
                case 1:
                    setAnimeStatus(mAr.STATUS_WATCHING);
                    break;
                case 2:
                    setAnimeStatus(mAr.STATUS_COMPLETED);
                    break;
                case 3:
                    setAnimeStatus(mAr.STATUS_ONHOLD);
                    break;
                case 4:
                    setAnimeStatus(mAr.STATUS_DROPPED);
                    break;
                case 5:
                    setAnimeStatus(mAr.STATUS_PLANTOWATCH);
                    break;
            }

        }
        else
        {
            switch (pickValue)
            {
                case 1:
                    setMangaStatus(mMr.STATUS_WATCHING);
                    break;
                case 2:
                    setMangaStatus(mMr.STATUS_COMPLETED);
                    break;
                case 3:
                    setMangaStatus(mMr.STATUS_ONHOLD);
                    break;
                case 4:
                    setMangaStatus(mMr.STATUS_DROPPED);
                    break;
                case 5:
                    setMangaStatus(mMr.STATUS_PLANTOWATCH);
                    break;
            }
        }
    }

    public void setAnimeStatus(String status)
    {
        mAr.setMyStatus(status);
        mAr.setDirty(mAr.DIRTY);

        //        MyStatusView.setText(WordUtils.capitalize(status));
    }

    public void setMangaStatus(String status)
    {
        mMr.setMyStatus(status);
        mMr.setDirty(mAr.DIRTY);

        //        MyStatusView.setText(WordUtils.capitalize(status));
    }

    @Override
    public void onMangaDialogDismissed(int newChapterValue, int newVolumeValue) {

        if ("manga".equals(recordType))
        {

            if (newChapterValue == mMr.getPersonalProgress())
            {

            }
            else
            {
                if (Integer.parseInt(mMr.getTotal()) != 0)
                {
                    if (newChapterValue == Integer.parseInt(mMr.getTotal()))
                    {
                        mMr.setMyStatus(mMr.STATUS_COMPLETED);
                    }
                    if (newChapterValue == 0)
                    {
                        mMr.setMyStatus(mMr.STATUS_PLANTOWATCH);
                    }

                }

                mMr.setPersonalProgress(newChapterValue);
                mMr.setDirty(mMr.DIRTY);

                ProgressCurrentView.setText(Integer.toString(newChapterValue));


            }

            if (newVolumeValue == mMr.getVolumeProgress())
            {

            }
            else
            {
                mMr.setVolumesRead(newVolumeValue);
                mMr.setDirty(mMr.DIRTY);
            }
        }



    }

    public void contentInflated(int contentType) {
        switch (contentType) {
            case GenericCardFragment.CONTENT_TYPE_SYNOPSIS:
                SynopsisView = (TextView) SynopsisFragment.getView().findViewById(R.id.SynopsisContent);
                if (SynopsisText != null)
                {
                    SynopsisView.setText(SynopsisText, TextView.BufferType.SPANNABLE);
                }
                break;
            case GenericCardFragment.CONTENT_TYPE_PROGRESS:
                ProgressCurrentView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountCurrent);
                ProgressTotalView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountTotal);
                MyStatusView = (TextView) ProgressFragment.getView().findViewById(R.id.myStatus);
                if(ProgressText != null)
                {
                    ProgressCurrentView.setText(ProgressText);
                    ProgressTotalView.setText("/" + TotalProgressText);
                    MyStatusView.setText(MyStatusText);
                }
                break;
            case GenericCardFragment.CONTENT_TYPE_INFO:
                RecordTypeView = (TextView) StatusFragment.getView().findViewById(R.id.mediaType);
                RecordStatusView = (TextView) StatusFragment.getView().findViewById(R.id.mediaStatus);
                MemberScoreView = (TextView) StatusFragment.getView().findViewById(R.id.memberScore);
                if (RecordStatusText != null)
                {
                    RecordTypeView.setText(RecordTypeText);
                    RecordStatusView.setText(RecordStatusText);
                    MemberScoreView.setText(MemberScoreText);
                }

                break;

            case GenericCardFragment.CONTENT_TYPE_SCORE:
                MALScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MALScoreBar);
                MyScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MyScoreBar);
                if (MemberScoreText != null)
                {
                    MALScoreBar.setRating(Float.parseFloat(MemberScoreText) / 2);
                    MyScoreBar.setRating(Float.parseFloat(MyScoreText) / 2);
                }

                break;
        }

    }

    public void Share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

        sharingIntent.setType("text/plain");
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        sharingIntent.putExtra(Intent.EXTRA_TEXT, makeShareText());

        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public String makeShareText() {
        String shareText = pManager.getCustomShareText();

        shareText = shareText.replace("$title;", actionBar.getTitle());
        shareText = shareText.replace("$link;", "http://myanimelist.net/" + recordType + "/" + Integer.toString(recordID));

        shareText = shareText + getResources().getString(R.string.customShareText_fromAtarashii);

        return shareText;
    }
}