package com.rudy.vdsimple;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class YoutubePlayerDialog extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{
	public static final String API_KEY = "AIzaSyB-vE_PNo2_1o65I2etL3aITlKRYYhzeFs";
	
	private String VIDEO_ID;
	private String VIDEO_TITLE;
	private String VIDEO_DOWNLOAD_URL;
	
	private final int RECOVERY_DIALOG_REQUEST = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.download_dialog);
	    Bundle bundle = getIntent().getExtras();
	    VIDEO_ID = bundle.getString("video_id");
	    VIDEO_TITLE = bundle.getString("video_title");
	    VIDEO_DOWNLOAD_URL = bundle.getString("video_download_url");
	    
	    YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
	    youTubeView.initialize(API_KEY, this);
	    
		// set the custom dialog components - text, image and button
		TextView text = (TextView) findViewById(R.id.tv_title);
		text.setText(VIDEO_TITLE);
		
		TextView btnDownloadMp3 = (TextView) findViewById(R.id.tv_download_mp3);
        btnDownloadMp3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadFile(VIDEO_DOWNLOAD_URL);
			}
		});
        
        TextView btnDownloadVideo = (TextView) findViewById(R.id.tv_download_video);
        btnDownloadVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadFile(VIDEO_DOWNLOAD_URL);
			}
		});
        
        TextView btnClose = (TextView) findViewById(R.id.tv_cancel);
        btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				setResult(Activity.RESULT_OK,returnIntent);
				finish();
			}
		});
	}
	
	public void downloadFile(String url) {
		DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Request request = new Request(
                Uri.parse(url));
        dm.enqueue(request);
	}
	
	@Override
	public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
		// TODO Auto-generated method stub
		if (errorReason.isUserRecoverableError()) {
		      errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
	    } else {
	      String errorMessage = String.format(getString(R.string.error_player), errorReason.toString());
	      Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RECOVERY_DIALOG_REQUEST) {
		    // Retry initialization if user performed a recovery action
			getYouTubePlayerProvider().initialize(API_KEY, this);
	    }
	}
	
	protected YouTubePlayer.Provider getYouTubePlayerProvider() {
		return (YouTubePlayerView) findViewById(R.id.youtube_view);
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
		if (!wasRestored) {
			player.loadVideo(VIDEO_ID);
     	}
	}
}
