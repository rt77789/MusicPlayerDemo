package org.xiaoe.test.demo.music;

import org.xiaoe.test.demo.parser.LrcParser;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicPlayer extends Activity {

	private MediaPlayer english = null;
	private SeekBar sb = null;
	private TextView currentTime = null;
	private TextView totalTime = null;
	private TextView lrcLine = null;
	private LrcParser lrc = null;

	private myThread thread = null;

	private Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// super.handleMessage(msg);
			if (msg.what == MESSAGE) {
				if (currentTime == null) {
					Log.d("Debug:", "tv == null.");
				} else {
					int minutes = (msg.arg1 / 1000) / 60;
					int seconds = (msg.arg1 / 1000) % 60;

					currentTime.setText(minutes + ":" + seconds);

					String sentence = lrc.locateStamp(msg.arg1);

					lrcLine.setText(sentence);
				}

				if (sb == null) {
					Log.d("Debug:", "sb == null.");
				} else {
					int sMax = sb.getMax();
					sb.setProgress(msg.arg1 * sMax / msg.arg2);
				}
			}
		}
	};

	private final static int MESSAGE = 0x11;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		try {
			initialize();
		} catch (Exception e) {
			Log.d("initilaize(): ", e.getMessage());
		}
		thread = new myThread();
	}

	private void initialize() {
		sb = (SeekBar) findViewById(R.id.seekBar1);

		if (sb == null) {
			Log.d("Debug:", "get sb == null.");
		}

		currentTime = (TextView) findViewById(R.id.textView1);
		totalTime = (TextView) findViewById(R.id.textView2);
		lrcLine = (TextView) findViewById(R.id.textView3);
		english = MediaPlayer.create(this, R.raw.apologize);

		if (english == null) {
			Log.d("Debug:", "get english == null.");
		} else {
			english.seekTo(0);
		}

		if (currentTime == null) {
			Log.d("Debug:", "get current == null.");
		}

		if (lrcLine == null) {
			Log.d("Debug:", "get lrcLine == null.");
		}

		if (totalTime == null) {
			Log.d("Debug:", "get totalTime == null.");
		} else {
			if (english != null) {
				int minutes = (english.getDuration() / 1000) / 60;
				int seconds = (english.getDuration() / 1000) % 60;
				totalTime.setText(minutes + ":" + seconds);
			} else {
				totalTime.setText(0 + ":" + 0);
			}
		}

		lrc = new LrcParser("/res/raw/apologize_lrc.lrc");
	}

	// # Destroy the current object and the super class.
	@Override
	protected void onDestroy() {
		if (english != null) {
			english.stop();
		}
		if (thread.isAlive()) {
			thread.stop();
		}
		super.onDestroy();
	}

	// # The parameter is necessary.
	public void rockHandler(View view) {
		if (english == null) {
			Log.d("MusicPlayer->rockHandler:", "english == null.");
		} else {
			if (english.isPlaying()) {
				english.pause();
				if (thread.isAlive())
					thread.stop();
			} else {
				english.start();
				if (!thread.isAlive())
					thread.start();
			}
		}
	}

	class myThread extends Thread {
		@Override
		public void run() {
			for (int i = 0; i < Integer.MAX_VALUE; ++i) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Message msg = new Message();
				msg.what = MESSAGE;
				if (english != null) {
					msg.arg1 = english.getCurrentPosition();
					msg.arg2 = english.getDuration();
				} else {
					Log.d("myThread->run:", "english == null.");
					msg.arg1 = 0;
					msg.arg2 = 1;
				}
				mHandle.sendMessage(msg);
			}
		}
	}
}