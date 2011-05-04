package org.xiaoe.test.demo.music;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Select the file in SD card.
 * 
 * @author aliguagua.zhengy
 * 
 */
public class FileSelector extends Activity {

	private final String MAIN_PATH = "/sdcard";
	private Map<String, String> fileMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initialize();
	}

	private void initialize() {
		fileMap = new TreeMap<String, String>();

		LinearLayout fileSelectorPanel = (LinearLayout) findViewById(R.id.linearLayoutf);
		
		if (fileSelectorPanel == null) {
			Log.d("xiaoe", "fileSelectorPanel is null.");
			return;
		}

		findFile(MAIN_PATH, MAIN_PATH);

		for (Entry<String, String> entry : fileMap.entrySet()) {
			TextView tv = new TextView(this);

			tv.setText(entry.getValue());
			tv.setContentDescription(entry.getKey());
			tv.setClickable(true);
			tv.setOnClickListener(new TextClickListener());

			fileSelectorPanel.addView(tv);
		}
	}

	class TextClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// # Get the mp3 file directory.
			String filePath = v.getContentDescription().toString();

			// # Create a new Intent object.
			Intent intent = new Intent();
			
			intent.putExtra("mp3Dir", filePath);
			
			// Jump from FileSelector to MusicPlayer.
			intent.setClass(FileSelector.this, MusicPlayer.class);
			// Start the new intent.
			FileSelector.this.startActivity(intent);
		}

	}

	/**
	 * Find all files which has a suffix ".mp3".
	 */
	private void findFile(String dir, String fileName) {
		File file = new File(dir);

		Log.d("xiaoe", "dir:" + dir);
		Log.d("xiaoe", "fileName:" + fileName);
		// Log.d("xiaoe", "seperator:" + File.separator);

		if (!file.exists()) {
			Log.d("xiaoe:", "FileSelector->findFile: " + dir + "isn't exists.");
			return;
		}
		// # This dir(file) is a directory or a file or else.
		if (file.isDirectory()) {

			//Log.d("xiaoe", dir + " is a directory.");

			String[] lists = file.list();
			if (lists != null) {
				for (String nextFile : lists) {
					findFile(dir + File.separator + nextFile, nextFile);
				}
			}
		} else if (file.isFile()) {
			//Log.d("xiaoe", dir + " is a file.");

			if (checkFileType(dir)) {
				fileMap.put(dir, fileName);
			}
		} else {
			//Log.d("xiaoe", dir + " is else.");
		}
	}

	/**
	 * Check file name whether end with a suffix ".mp3".
	 * 
	 * @return
	 */
	private boolean checkFileType(String fileName) {
		return fileName.endsWith(".mp3");
	}
}
