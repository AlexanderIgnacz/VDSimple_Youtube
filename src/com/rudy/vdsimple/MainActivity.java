package com.rudy.vdsimple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends YouTubeBaseActivity implements OnClickListener, OnItemClickListener, YouTubePlayer.OnInitializedListener {
	static class VideoDownloadOption {
		String videoId;
		String type;
		
		String date;
		String channelId;
		String channelTitle;
		
		String title;
		String description;
		
		String duration;
		
		String live;
		
		public String getDownloadUrl() {
			return "http://api.soundcloud.com/tracks/159723640/stream?client_id=40ccfee680a844780a41fbe23ea89934";
		}
		
		public void setISO8601Duration(String ptdur) {
			String result = ptdur.replace("PT","").replace("H",":").replace("M",":").replace("S","");
	        String arr[] = result.split(":");
	        if(arr.length > 2) {
	        	duration = String.format("%d:%02d:%02d", Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
	        }
	        else if(arr.length == 2) {
	        	duration = String.format("%02d:%02d", Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
	        }
	        else if(arr.length == 1) {
	        	duration = String.format("00:%02d", Integer.parseInt(arr[0]));
	        }
	        
	        //Log.v("ISO", videoId + ": " + ptdur + " --> " + duration);
		}
		
		public String getDurationString() {
			if(live.equals("none")) {
				return duration;
			}
			else {
				return live;
			}
		}
	}
	
	ImageView mBtnSearch;
	ImageView mBtnCancel;
	
	boolean m_isEmpty = false;
	EditText mETSearch;
	
	TextView mTVTitle;
	
	ListView mLVVideos;
	
	ProgressDialog mLoadingDialog = null;
	
	ArrayList<VideoDownloadOption> mVideos = new ArrayList<VideoDownloadOption>();
	VideoAdapter mAdapter;
	
	private final int RECOVERY_DIALOG_REQUEST = 1;
	private final String strYoutubeKey = "AIzaSyB-vE_PNo2_1o65I2etL3aITlKRYYhzeFs";
	
	VideoDownloadOption current_video;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
	    youTubeView.initialize(strYoutubeKey, this);
		
		mBtnSearch = (ImageView)findViewById(R.id.btn_search);
		mBtnCancel = (ImageView)findViewById(R.id.btn_cancel);
		mETSearch = (EditText)findViewById(R.id.txt_search);
		mLVVideos = (ListView)findViewById(R.id.lv_videos);
		mTVTitle = (TextView)findViewById(R.id.txt_search_title);
		
		mBtnSearch.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		mLVVideos.setOnItemClickListener(this);
		
		mETSearch.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					if (mETSearch.getText().toString().length() == 0){
                		return true;
                	}
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mETSearch.getWindowToken(), 0);
                    findViewById(R.id.header_lay2).setVisibility(View.GONE);
                    findViewById(R.id.header_lay1).setVisibility(View.VISIBLE);
                    
                    String keyword = mETSearch.getText().toString(); 
                    search(keyword);	
                    mTVTitle.setText("Result for " + keyword);
                    mETSearch.setText("");
                    return true;
				}
				return false;
			}
		});
		
		mETSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mETSearch.length() != 0){
					mBtnCancel.setAlpha(255);
            		m_isEmpty = false;
				}
            	else if (m_isEmpty == false)
            		mBtnCancel.setAlpha(70);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
	}

	@Override
	public void onClick(View v) {
		if(v == mBtnSearch) {
			findViewById(R.id.header_lay1).setVisibility(View.GONE);
            findViewById(R.id.header_lay2).setVisibility(View.VISIBLE);
            mBtnCancel.setAlpha(70);
            
            mETSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mETSearch, InputMethodManager.SHOW_IMPLICIT);
		}
		else if(v == mBtnCancel) {
			if (mETSearch.length() != 0){
        		m_isEmpty = true;
        		mETSearch.setText("");
        		return;
        	}
        	
        	findViewById(R.id.header_lay2).setVisibility(View.GONE);
            findViewById(R.id.header_lay1).setVisibility(View.VISIBLE);
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mETSearch.getWindowToken(), 0);
		}
	}
	
	String mKeyword = "";
	String nextPageToken = "";
	public void search(String keyword) {
		mKeyword = keyword;
		nextPageToken = "";
		mLVVideos.setSelection(0);
		(new SearchVideos()).execute(keyword, "");
	}
	
	public void loadMore() {
		if(mLoadingDialog != null) {
			return;
		}
		
		if(!nextPageToken.isEmpty()) {
			(new SearchVideos()).execute(mKeyword, nextPageToken);
		}
	}
	
	public void onSearched(ArrayList<VideoDownloadOption> videos, String pageToken) {
		if(nextPageToken.isEmpty()) {
			mVideos.clear();
		}
		else if(videos.size() == 0) {
			return;
		}
		
		mVideos.addAll(videos);
		
		if(mAdapter == null) {
			mAdapter = new VideoAdapter(this, mVideos);
			mLVVideos.setAdapter(mAdapter);
		}
		
		mAdapter.notifyDataSetChanged();
		nextPageToken = pageToken; 
	}
	
	
	public void showVideo() {
		final Dialog dialog = new Dialog(this);
    		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.download_dialog);
		
		// set the custom dialog components - text, image and button
		TextView text = (TextView) dialog.findViewById(R.id.tv_title);
		text.setText(current_video.title);
		
		TextView btnDownloadMp3 = (TextView) dialog.findViewById(R.id.tv_download_mp3);
        btnDownloadMp3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadFile(current_video.getDownloadUrl());
			}
		});
        
        TextView btnDownloadVideo = (TextView) dialog.findViewById(R.id.tv_download_video);
        btnDownloadVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadFile(current_video.getDownloadUrl());
			}
		});
        
        TextView btnClose = (TextView) dialog.findViewById(R.id.tv_cancel);
        btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
	    
		dialog.show();
		dialog.getWindow().setAttributes(lp);
	}
	
	public void downloadFile(String url) {
		DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Request request = new Request(
                Uri.parse(url));
        dm.enqueue(request);
	}
	
	public class SearchVideos extends AsyncTask<Object, Void, Boolean>
    {
		ArrayList<VideoDownloadOption> listVideos;
        
        String search_text = "";
        String pageToken = "";
        String nextPageToken = "";
        int offset = 0, limit = 0;
        
        private String getStringFromURL(String strURL) {
        		StringBuffer chaine = new StringBuffer("");
            try{
                URL url = new URL(strURL);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    chaine.append(line);
                }
            }
            catch (IOException e) {
                // Writing exception to log
                e.printStackTrace();
                return null;
            }
            return chaine.toString();
        }
        
        protected Boolean doInBackground(Object... as)
        {
	        	String keyword = as[0].toString();
	        	String pageToken = as[1].toString();
	        	listVideos = new ArrayList<VideoDownloadOption>();
	        	
	        	String strURL = "https://www.googleapis.com/youtube/v3/search?q=" + keyword + "&part=snippet&key=AIzaSyB-vE_PNo2_1o65I2etL3aITlKRYYhzeFs&maxResults=20";
	        	if(!pageToken.isEmpty()) {
	        		strURL = strURL + "&pageToken=" + pageToken;
	        	}
	        	this.pageToken = pageToken;
	        	
	        	String chaine = getStringFromURL(strURL);
	        	String videoIds = "";
            try {
	            	JSONObject jObject = new JSONObject(chaine);
	            	if(jObject.has("nextPageToken")) {
	            		nextPageToken = jObject.getString("nextPageToken");
	            	}
	            	
	            	if(jObject.has("items")) {
	            		JSONArray array = jObject.getJSONArray("items");
	            		for(int i = 0; i < array.length(); i++) {
	            			try {
	            				VideoDownloadOption video = new VideoDownloadOption();
	            				
	            				JSONObject item = array.getJSONObject(i);
	            				
	            				JSONObject id = item.getJSONObject("id");
	            				video.type = id.getString("kind");
	            				if(id.has("videoId")) {
	            					video.videoId = id.getString("videoId");
	            				}
	            				else if(id.has("channelId")) {
	            					//video.videoId = id.getString("channelId");
	            					//let's don't show channels
	            					continue;
	            				}
	
	            				JSONObject snippet = item.getJSONObject("snippet");
	            				video.date = snippet.getString("publishedAt");
	            				if(snippet.has("channelId")) {
	            					video.channelId = snippet.getString("channelId");
	            				}
	            				video.title = snippet.getString("title");
	            				video.description = snippet.getString("description");
	            				video.channelTitle = snippet.getString("channelTitle");
	            				video.live = snippet.getString("liveBroadcastContent");
	            				
	            				video.duration="??:??";
	            				
	            				listVideos.add(video);
	            				
	            				videoIds = videoIds + (videoIds.isEmpty() ? "" : ",") + video.videoId;
	            				
	            			} catch(Exception e) {
	            				e.printStackTrace();
	            			}
	            		}
	            	}
	            	
	            	//get durations
	            strURL = "https://www.googleapis.com/youtube/v3/videos?id=" + videoIds + "&part=contentDetails&key=AIzaSyB-vE_PNo2_1o65I2etL3aITlKRYYhzeFs";
	            String contentDetails = getStringFromURL(strURL);
	            
	            jObject = new JSONObject(contentDetails);
	            if(jObject.has("items")) {
	            		JSONArray array = jObject.getJSONArray("items");
	            		for(int i = 0; i < array.length(); i++) {
	            			JSONObject item = array.getJSONObject(i);
	        				
	            			try {
	            				JSONObject detail = item.getJSONObject("contentDetails");
	            				listVideos.get(i).setISO8601Duration(detail.getString("duration"));
	            			}
	            			catch(Exception e) {
	            				e.printStackTrace();
	            			}
	            		}
	            	}
	        } catch(Exception e) {
            		e.printStackTrace();
            		return false;
            }            
            return true;
        }

        protected void onPostExecute(Boolean result)
        {
	        	super.onPostExecute(result);
	        	if(mLoadingDialog != null) {
	        		mLoadingDialog.dismiss();
	        		mLoadingDialog = null;
	        	}
	        	if(result.booleanValue()) {
	        		//result
	        		onSearched(listVideos, nextPageToken);
	        	}
        }

        protected void onPreExecute()
        {
            super.onPreExecute();
            mLoadingDialog = ProgressDialog.show(MainActivity.this, "",  "Searching. Please wait...", true);
            mLoadingDialog.setCancelable(false);
        }
    }
	
	public class VideoAdapter extends BaseAdapter{
		private List<VideoDownloadOption> array;
		private Context mContext;
		int selectedPosition = 0;

		public VideoAdapter(Context context, List<VideoDownloadOption> str) {
			// TODO Auto-generated constructor stub
			mContext = context;
			array = str;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return array.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return array.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public VideoDownloadOption GetDownload(){
			return array.get(selectedPosition);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.download_list_item, null);
            }
            TextView tvTitle = (TextView)v.findViewById(R.id.tv_title);
            TextView tvAuthor = (TextView)v.findViewById(R.id.tv_uploader);
            TextView tvDuaration = (TextView)v.findViewById(R.id.tv_length);
            
            VideoDownloadOption info = array.get(position);
            tvTitle.setText(info.title);
            tvAuthor.setText(info.channelTitle);
            tvDuaration.setText(info.getDurationString());
            
            if(position > array.size() - 2) {
            	loadMore();
            }
            return v;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		if(position < mVideos.size()) {
			current_video = mVideos.get(position);
			showVideo();
		}
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
			getYouTubePlayerProvider().initialize(strYoutubeKey, this);
	    }
	}
	
	protected YouTubePlayer.Provider getYouTubePlayerProvider() {
		return (YouTubePlayerView) findViewById(R.id.youtube_view);
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
		if (!wasRestored) {
			player.cueVideo(current_video.videoId);
     	}
	}
}
