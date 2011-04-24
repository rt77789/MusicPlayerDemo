package org.xiaoe.test.demo.music;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.xiaoe.test.demo.parser.LrcParser;
import org.xiaoe.test.demo.util.Pair;
import org.xiaoe.test.demo.util.Util;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicPlayer extends Activity {

	
	private MediaPlayer english = null;	// # MediaPlayer
	
	private SeekBar sb = null;	// # SeekBar for displaying process.

	private TextView currentTime = null;		// # TextView for displaying current process position.
	
	private TextView totalTime = null;	// # TextView for displaying total time of the mp3 file.

	private TextView lrcLine = null;

	private LrcParser lrc = null;

	private int flushPeriod = 100;	// # The period of flush.

	private myThread thread = null;

	private Map<Integer, String> stamps;
	
	private int[] timeSpots;
	
	private Map<Integer, TextView> lrcTextView;

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

					String sentence = locateStamp(msg.arg1);
					// Log.d("xiaoe", "msg.arg1: " + msg.arg1);
					// Log.d("xiaoe", "locateStamp returns: " + sentence);

					lrcLine.setText(sentence);

					TextView currentLine = locateTextView(msg.arg1);
					TextView prev = prevTextView(msg.arg1);
					TextView next = nextTextView(msg.arg1);

					currentLine.setBackgroundColor(Color.GRAY);
					if (prev != null) {
						prev.setBackgroundColor(Color.BLACK);

						// # Not the first Line, focus on next TextView.
						if (next != null && !next.requestFocus()) {
							Log.d("xiaoe", "next.requestFocus() fail.");
						}
					} else {

						// # First line, focus on current TextView.
						if (currentLine.requestFocus()) {
							Log.d("xiaoe", "currentLine.requestFocus() fail.");
						}
					}
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
			Log.d("initilaize(): ", Util.printStackTrace(e));
		}
		Log.d("xiaoe", "onCreate(): thread new.");
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

		ScrollView sView = (ScrollView) findViewById(R.id.scrollView1);
		if (sView != null) {
			sView.setVerticalScrollBarEnabled(false);
		} else {
			Log.d("xiaoe", "sView is null.");
		}

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

		stamps = new TreeMap<Integer, String>();
		lrcTextView = new TreeMap<Integer, TextView>();
		lrc = new LrcParser("/res/raw/apologize_lrc.lrc");

		LinearLayout lrcPanel = (LinearLayout) findViewById(R.id.linearLayout2);

		while (lrc.hasNext()) {
			Pair<Integer, String> line = lrc.next();
			if (line == null)
				continue;

			TextView tv = new TextView(lrcPanel.getContext());

			tv.setText(line.second);
			tv.setFocusable(true);
			tv.setFocusableInTouchMode(true);

			stamps.put(line.first, line.second);

			lrcTextView.put(line.first, tv);

			lrcPanel.addView(tv);
		}

		fillTimeSpots();
	}

	// # Destroy the current object and the super class.
	@Override
	protected void onDestroy() {
		if (english != null) {
			english.stop();
		}
		if (thread != null && thread.isAlive()) {

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
				thread.over();
				thread = null;
			} else {
				english.start();
				if (thread == null) {
					thread = new myThread();
					thread.start();
				} else if (thread.isAlive()) {
					thread.over();
					thread = new myThread();
					thread.start();
				}
			}
		}
	}

	class myThread extends Thread {
		private boolean flag = false;

		public void over() {
			this.flag = true;
		}

		@Override
		public void run() {
			for (int i = 0; i < Integer.MAX_VALUE; ++i) {
				if (flag) {
					break;
				}
				try {
					Thread.sleep(flushPeriod);
				} catch (InterruptedException e) {
					Log.d("xiaoe", "myThread->run: Thread.sleep exception.");
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

	/**
	 * Fill time spots.
	 */
	private void fillTimeSpots() {
		timeSpots = new int[stamps.size()];
		if (timeSpots == null) {
			Log
					.d("MusicPlayer->fillTimeSpots:",
							" timeSpots new returns null.");
		}
		int i = 0;
		for (Entry<Integer, String> p : stamps.entrySet()) {
			if (i > timeSpots.length) {
				Log.d("LrcParser->fillTimeSpots: ", "i > timeSpots.length.");
				break;
			}
			timeSpots[i++] = p.getKey();
		}
		// for (i = 0; i < timeSpots.length; ++i) {
		// Log.d("xiaoe", String.valueOf(timeSpots[i]));
		// }
	}

	/**
	 * Get the Lrc info corresponding to parameter time.
	 * 
	 * @param time
	 * @return
	 */
	public String locateStamp(int time) {
		int index = searchTimeSpots(time);
		String stamp = stamps.get(timeSpots[index]);
		return stamp;
	}

	public TextView locateTextView(int time) {
		int index = searchTimeSpots(time);
		TextView stamp = lrcTextView.get(timeSpots[index]);
		return stamp;
	}

	public TextView prevTextView(int time) {
		int index = searchTimeSpots(time) - 1;
		if (index < 0)
			return null;
		TextView stamp = lrcTextView.get(timeSpots[index]);
		return stamp;
	}

	public TextView nextTextView(int time) {
		int index = searchTimeSpots(time) + 1;
		if (index >= timeSpots.length)
			return null;
		TextView stamp = lrcTextView.get(timeSpots[index]);
		return stamp;
	}

	/**
	 * Search index of time spots array corresponding to parameter time. It's a
	 * binary search process.
	 * 
	 * @param time
	 * @return
	 */
	private int searchTimeSpots(int time) {

		int left = 0, right = timeSpots.length - 1;
		if (time >= timeSpots[right])
			return right;
		if (time <= timeSpots[left])
			return left;

		while (left <= right) {
			int mid = (left + right) / 2;
			if (time < timeSpots[mid]) {
				right = mid - 1;
			} else if (time > timeSpots[mid]) {
				left = mid + 1;
			} else {
				return mid;
			}
		}
		if (left >= timeSpots.length)
			left = timeSpots.length - 1;
		while (left >= 0) {
			if (timeSpots[left] <= time)
				break;
			--left;
		}

		return left;
	}
}