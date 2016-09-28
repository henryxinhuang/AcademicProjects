package com.EECE381.launchpad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;
import com.EECE381.launchpad.R;

public class MainActivity extends Activity {
	public Queue<MediaPlayer> soundQueue;
	public long lastConnectionTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		soundQueue = new LinkedList<MediaPlayer>();
		// This call will result in better error messages if you
		// try to do things in the wrong thread.

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads().detectDiskWrites().detectNetwork()
		.penaltyLog().build());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//		EditText et = (EditText) findViewById(R.id.RecvdMessage);
		//		et.setKeyListener(null); // makes RecvdMessage field uneditable
		//		et = (EditText) findViewById(R.id.error_message_box);
		//		et.setKeyListener(null); // makes RecvdMessage field uneditable

		// Set up a timer task. We will use the timer to check the
		// input queue every 1 ms

		TCPReadTimerTask tcp_task = new TCPReadTimerTask();
		Timer tcp_timer = new Timer();
		tcp_timer.schedule(tcp_task, 3000, 50);

		for (int i = 1; i <= 16; i++) {
			String strID = "button" + i;
			int resID = getResources().getIdentifier(strID, "id", getPackageName());
			final Button b = (Button)findViewById(resID);
			b.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN ) {
						sendMessage(v);
						b.setBackgroundResource(R.drawable.btn_default_pressed_holo_dark);
						return true;
					}
					if (event.getAction() == MotionEvent.ACTION_UP ) {
						b.setBackgroundResource(R.drawable.btn_default_holo_dark);
						return true;
					}
					return false;
				}
			});
		}
	}

	public void playSound(View v) {
		int songid;

		if (v == findViewById(R.id.button1)) {
			songid = R.raw.s1;
		} else if (v == findViewById(R.id.button2)) {
			songid = R.raw.s2;
		} else if (v == findViewById(R.id.button3)) {
			songid = R.raw.s3;
		} else if (v == findViewById(R.id.button4)) {
			songid = R.raw.s4;
		} else if (v == findViewById(R.id.button5)) {
			songid = R.raw.s5;
		} else if (v == findViewById(R.id.button6)) {
			songid = R.raw.s6;
		} else if (v == findViewById(R.id.button7)) {
			songid = R.raw.s7;
		} else if (v == findViewById(R.id.button8)) {
			songid = R.raw.s8;
		} else if (v == findViewById(R.id.button9)) {
			songid = R.raw.s9;
		} else if (v == findViewById(R.id.button10)) {
			songid = R.raw.s10;
		} else if (v == findViewById(R.id.button11)) {
			songid = R.raw.s11;
		} else if (v == findViewById(R.id.button12)) {
			songid = R.raw.s12;
		} else if (v == findViewById(R.id.button13)) {
			songid = R.raw.s13;
		} else if (v == findViewById(R.id.button14)) {
			songid = R.raw.s14;
		} else if (v == findViewById(R.id.button15)) {
			songid = R.raw.s15;
		} else /* (v == findViewById(R.id.button16)) */{
			songid = R.raw.s16;
		}
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean isMute = getPrefs.getBoolean("mute", false);
		if (!isMute) {
			MediaPlayer sound = MediaPlayer.create(MainActivity.this, songid);
			if (soundQueue.size() > 10) {
				MediaPlayer tempSound = soundQueue.remove();
				tempSound.release();
			}
			soundQueue.add(sound);
			sound.start();
		}
	}

	public void playSound1(int songid) {
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean isMute = getPrefs.getBoolean("mute", false);
		if (!isMute) {
			MediaPlayer sound = MediaPlayer.create(MainActivity.this, songid);
			if (soundQueue.size() > 10) {
				MediaPlayer tempSound = soundQueue.remove();
				tempSound.release();
			}
			soundQueue.add(sound);
			sound.start();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private boolean isRecording = false;
	private boolean isPlayingRecorded = false;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			return true;
		} else if (item.getItemId() == R.id.connect_settings) {
			View v = null;
			openSocket(v);
		} else if (item.getItemId() == R.id.disconnect_settings) {
			View v = null;
			closeSocket(v);
		} else if (item.getItemId() == R.id.mutede2_settings) {
			sendMessage("y");
		} else if (item.getItemId() == R.id.unmutede2_settings) {
			sendMessage("x");
		} else if (item.getItemId() == R.id.RecordStop_settings) {
			if (!isRecording) {
				AudioRecordTest();
				onRecord(true);
			} else {
				onRecord(false);
			}
			isRecording = !isRecording;
		} else if (item.getItemId() == R.id.PlayStopRecorded_settings) {
			if (!isPlayingRecorded) {
				onPlay(true);
			} else {
				onPlay(false);
			}
			isPlayingRecorded = !isPlayingRecorded;
		}
		return false;
	}



	// Route called when the user presses "connect"

	public void openSocket(View v) {
		MyApplication app = (MyApplication) getApplication();
		//		TextView msgbox = (TextView) findViewById(R.id.error_message_box);

		// Make sure the socket is not already opened
		if (app.sock != null && app.sock.isConnected() && !app.sock.isClosed()) {
			Context context = getApplicationContext();
			CharSequence text = "You are already connected!";
			Log.i("Debug", "You are already connected!");
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			return;
		}

		// open the socket. SocketConnect is a new subclass
		// (defined below). This creates an instance of the subclass
		// and executes the code in it.

		new SocketConnect().execute((Void) null);
	}

	// Called when the user wants to send a message
	public void sendMessage(String s) {
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		MyApplication app = (MyApplication) getApplication();

		String playMode = getPrefs.getString("playOptions", "1");
		if (playMode.contentEquals("1")) {
			return;
		}
		if (app.sock == null && playMode.contentEquals("2")) {
			Log.i("Debug", "You are in Play Together mode and you are not connected!");
			Context context = getApplicationContext();
			CharSequence text = "You are in Play Together mode and you are not connected!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			return;

		}

		Time now = new Time();
		now.setToNow();
		if (now.toMillis(true) > lastConnectionTime + 5000 && playMode.contentEquals("2")) {
			Context context = getApplicationContext();
			CharSequence text = "Reconnecting...";
			Log.i("Debug", "Reconnecting... TimeNow: " + now.toMillis(true));
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}

		// Create an array of bytes. First byte will be the
		// message length, and the next ones will be the message

		byte buf[] = new byte[s.length() + 1];
		buf[0] = (byte) s.length();
		System.arraycopy(s.getBytes(), 0, buf, 1, s.length());

		// Now send through the output stream of the socket

		OutputStream out;
		try {
			out = app.sock.getOutputStream();
			try {
				out.write(buf, 0, s.length() + 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void sendMessage(View view) {
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		MyApplication app = (MyApplication) getApplication();

		String playMode = getPrefs.getString("playOptions", "1");
		if (playMode.contentEquals("1")) {
			playSound(view);
			return;
		}

		// Get the message from the box

		// EditText et = (EditText) findViewById(R.id.MessageText);
		// String msg = et.getText().toString();
		String msg;// = Integer.toString(view.getId());
		if (view.getId() == R.id.button1) {
			msg = "a";
		} else if (view.getId() == R.id.button2) {
			msg = "b";
		} else if (view.getId() == R.id.button3) {
			msg = "c";
		} else if (view.getId() == R.id.button4) {
			msg = "d";
		} else if (view.getId() == R.id.button5) {
			msg = "e";
		} else if (view.getId() == R.id.button6) {
			msg = "f";
		} else if (view.getId() == R.id.button7) {
			msg = "g";
		} else if(view.getId()== R.id.button8) {
			msg = "h";
		} else if(view.getId()== R.id.button9) {
			msg = "i";
		} else if(view.getId()== R.id.button10) {
			msg = "j";
		} else if(view.getId()== R.id.button11) {
			msg = "k";
		} else if(view.getId()== R.id.button12) {
			msg = "l";
		} else if(view.getId()== R.id.button13) {
			msg = "m";
		} else if(view.getId()== R.id.button14) {
			msg = "n";
		} else if(view.getId()== R.id.button15) {
			msg = "o";
		} else {
			msg = "p";
		}


		if (app.sock == null && playMode.contentEquals("2")) {
			Log.i("Debug", "You are in Play Together mode and you are not connected!");
			Context context = getApplicationContext();
			CharSequence text = "You are in Play Together mode and you are not connected!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			return;

		}

		Time now = new Time();
		now.setToNow();
		if (now.toMillis(true) > lastConnectionTime + 5000 && playMode.contentEquals("2")) {
			Context context = getApplicationContext();
			CharSequence text = "Reconnecting...";
			Log.i("Debug", "Reconnecting... TimeNow: " + now.toMillis(true));
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}

		// Create an array of bytes. First byte will be the
		// message length, and the next ones will be the message

		byte buf[] = new byte[msg.length() + 1];
		buf[0] = (byte) msg.length();
		System.arraycopy(msg.getBytes(), 0, buf, 1, msg.length());

		// Now send through the output stream of the socket

		OutputStream out;
		try {
			out = app.sock.getOutputStream();
			try {
				out.write(buf, 0, msg.length() + 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Called when the user closes a socket

	public void closeSocket(View view) {
		Log.i("Debug", "closeSocket");
		MyApplication app = (MyApplication) getApplication();

		if (!(app.sock != null && app.sock.isConnected() && !app.sock.isClosed())) {
			Context context = getApplicationContext();
			CharSequence text = "You are trying to disconnect when you are not connected!";
			Log.i("Debug", "You are trying to disconnect when you are not connected!");
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			return;
		}

		Socket s = app.sock;
		try {
			s.getOutputStream().close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Construct an IP address from the four boxes

	public String getConnectToIP() {

		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String addr = getPrefs.getString("ip", "192.168.0.147");
		return addr;
	}

	// Gets the Port from the appropriate field.

	public Integer getConnectToPort() {
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		int port = Integer.parseInt(getPrefs.getString("port", "50002"));
		return port;
	}

	// This is the Socket Connect asynchronous thread. Opening a socket
	// has to be done in an Asynchronous thread in Android. Be sure you
	// have done the Asynchronous Tread tutorial before trying to understand
	// this code.

	public class SocketConnect extends AsyncTask<Void, Void, Socket> {

		// The main parcel of work for this thread. Opens a socket
		// to connect to the specified IP.

		protected Socket doInBackground(Void... voids) {
			Log.i("Debug", "openSocket");
			Socket s = null;
			String ip = getConnectToIP();
			Integer port = getConnectToPort();

			try {
				s = new Socket(ip, port);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return s;
		}

		// After executing the doInBackground method, this is
		// automatically called, in the UI (main) thread to store
		// the socket in this app's persistent storage

		protected void onPostExecute(Socket s) {
			MyApplication myApp = (MyApplication) MainActivity.this
					.getApplication();
			myApp.sock = s;
		}
	}

	// This is a timer Task. Be sure to work through the tutorials
	// on Timer Tasks before trying to understand this code.

	public class TCPReadTimerTask extends TimerTask {
		public void run() {
			MyApplication app = (MyApplication) getApplication();
			SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String playMode = getPrefs.getString("playOptions", "1");
			boolean automaticallyConenct = getPrefs.getBoolean("automaticallyConnect", false);
			Time now = new Time();
			now.setToNow();
			if (automaticallyConenct) {
				if (playMode.contentEquals("2")) {
					if (app.sock == null || 
							!(app.sock.isConnected() && !app.sock.isClosed()) &&
							now.toMillis(true) > lastConnectionTime + 5000) {
						//					if (!(app.sock != null && app.sock.isConnected()
						//							&& !app.sock.isClosed())) {
						View v = null;
						openSocket(v);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						lastConnectionTime = now.toMillis(true);
					}
				}
			}


			if (app.sock != null && app.sock.isConnected()
					&& !app.sock.isClosed()) {

				try {
					InputStream in = app.sock.getInputStream();
					if (automaticallyConenct) {
						if (now.toMillis(true) > lastConnectionTime + 5000) {
							View v = null;
							closeSocket(v);
							return;
						}
					}
					// See if any bytes are available from the Middleman

					int bytes_avail = in.available();
					if (bytes_avail > 0) {

						lastConnectionTime = now.toMillis(true);
						//Log.i("Debug", "lastConnectionTime: " + lastConnectionTime);

						// If so, read them in and create a sring

						byte buf[] = new byte[bytes_avail];
						in.read(buf);

						final String sr = new String(buf, 0, bytes_avail,
								"US-ASCII");

						// As explained in the tutorials, the GUI can not be
						// updated in an asyncrhonous task. So, update the GUI
						// using the UI thread.

						runOnUiThread(new Runnable() {
							public void run() {
								//								EditText et = (EditText) findViewById(R.id.RecvdMessage);
								//								et.setText(s);
								SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
								String playMode = getPrefs.getString("playOptions", "1");
								if (playMode.contentEquals("1")) {
									return;
								}
								for(int i = 0; i < sr.length(); i++) {
									String s = Character.toString(sr.charAt(i));
									//Log.i("Debug", s);
									if (s.compareTo("a") == 0) {
										playSound(findViewById(R.id.button1));
									} else if (s.compareTo("b") == 0) {
										playSound(findViewById(R.id.button2));
									} else if (s.compareTo("c") == 0) {
										playSound(findViewById(R.id.button3));
									} else if (s.compareTo("d") == 0) {
										playSound(findViewById(R.id.button4));
									} else if (s.compareTo("e") == 0) {
										playSound(findViewById(R.id.button5));
									} else if (s.compareTo("f") == 0) {
										playSound(findViewById(R.id.button6));
									} else if (s.compareTo("g") == 0) {
										playSound(findViewById(R.id.button7));
									} else if (s.compareTo("h") == 0) {
										playSound(findViewById(R.id.button8));
									} else if (s.compareTo("i") == 0) {
										playSound(findViewById(R.id.button9));
									} else if (s.compareTo("j") == 0) {
										playSound(findViewById(R.id.button10));
									} else if (s.compareTo("k") == 0) {
										playSound(findViewById(R.id.button11));
									} else if (s.compareTo("l") == 0) {
										playSound(findViewById(R.id.button12));
									} else if (s.compareTo("m") == 0) {
										playSound(findViewById(R.id.button13));
									} else if (s.compareTo("n") == 0) {
										playSound(findViewById(R.id.button14));
									} else if (s.compareTo("o") == 0) {
										playSound(findViewById(R.id.button15));
									} else if (s.compareTo("p") == 0){
										playSound(findViewById(R.id.button16));
									} else if (s.compareTo("q") == 0){
										playSound1(R.raw.s17);
									} else if (s.compareTo("r") == 0){
										playSound1(R.raw.s18);
									} else if (s.compareTo("s") == 0){
										playSound1(R.raw.s19);
									} else if (s.compareTo("t") == 0){
										playSound1(R.raw.s20);
									} else if (s.compareTo("u") == 0){
										playSound1(R.raw.s21);
									} else if (s.compareTo("v") == 0){
										playSound1(R.raw.s22);
									} else if (s.compareTo("w") == 0){
										playSound1(R.raw.s23);
									}
								}
							}
						});

					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static final String LOG_TAG = "AudioRecordTest";
	private static String mFileName = "/test.mp3";

//	private RecordButton mRecordButton = null;
	private MediaRecorder mRecorder = null;

	private PlayButton   mPlayButton = null;
	private MediaPlayer   mPlayer = null;

	private void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	private void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}

	private void startPlaying() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	private void stopPlaying() {
		mPlayer.release();
		mPlayer = null;
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

//	class RecordButton extends Button {
//		boolean mStartRecording = true;
//
//		OnClickListener clicker = new OnClickListener() {
//			public void onClick(View v) {
//				onRecord(mStartRecording);
//				if (mStartRecording) {
//					setText("Stop recording");
//				} else {
//					setText("Start recording");
//				}
//				mStartRecording = !mStartRecording;
//			}
//		};
//
//		public RecordButton(Context ctx) {
//			super(ctx);
//			setText("Start recording");
//			setOnClickListener(clicker);
//		}
//	}

	class PlayButton extends Button {
		boolean mStartPlaying = true;

		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				onPlay(mStartPlaying);
				if (mStartPlaying) {
					setText("Stop playing");
				} else {
					setText("Start playing");
				}
				mStartPlaying = !mStartPlaying;
			}
		};

		public PlayButton(Context ctx) {
			super(ctx);
			setText("Start playing");
			setOnClickListener(clicker);
		}
	}

	public void AudioRecordTest() {
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/audiorecordtest.mp3";
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}
