package org.xiaoe.test.demo.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class LrcParser {
	Matcher match;
	Pattern pattern;
	Map<Integer, String> stamps;
	int[] timeSpots;
	List<String> header;

	public LrcParser(String filename) {
		// / [03:55.11]
		pattern = Pattern.compile("^\\[(\\d{2}):(\\d{2})\\.(\\d{2})\\]");
		stamps = new TreeMap<Integer, String>();
		header = new ArrayList<String>();

		loadFile(filename);
		fillTimeSpots();
	}

	/**
	 * Fill time spots.
	 */
	private void fillTimeSpots() {
		timeSpots = new int[stamps.size()];
		int i = 0;
		for (Entry<Integer, String> p : stamps.entrySet()) {
			if (i > timeSpots.length) {
				Log.d("LrcParser->fillTimeSpots: ", "i > timeSpots.length.");
				break;
			}
			timeSpots[i++] = p.getKey();
		}
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

	/**
	 * Load the Lrc info file with the filename.
	 * 
	 * @param filename
	 */
	public void loadFile(String filename) {

		Scanner scan = null;

		InputStream in = this.getClass().getResourceAsStream(filename);

		scan = new Scanner(in);
		while (scan.hasNext()) {
			String line = scan.nextLine();
			if (isValidFormat(line)) {
				seperateLine(line);
			} else {
				header.add(line);
			}
		}
	}

	/**
	 * Check parameter line whether is valid format.
	 * 
	 * @param line
	 * @return
	 */
	boolean isValidFormat(String line) {
		match = pattern.matcher(line);
		if (match.find()) {
			return true;
		}
		return false;
	}

	/**
	 * Parsing a line of Lrc file, and fill stamps, used regular expression.
	 * 
	 * @param line
	 */
	public void seperateLine(String line) {
		match = pattern.matcher(line);
		if (match.find()) {
			int m = Integer.valueOf(match.group(1));
			int s = Integer.valueOf(match.group(2));
			int ms = Integer.valueOf(match.group(3));

			// / ºÁÃë
			int stamp = ms * 10 + (s * 1000) + (m * 60 * 1000);
			String rem = line.substring(match.end(3) + 1).trim();
			stamps.put(stamp, rem);
		}
	}

	/**
	 * Display the parsing result.
	 */
	public void display() {
		for (String head : header) {
			System.out.println(head);
		}
		for (Entry<Integer, String> p : stamps.entrySet()) {
			System.out.println(p.getKey() + " " + p.getValue());
		}
	}

	public static void main(String[] args) {
		LrcParser lrc = new LrcParser("risis.lrc");
		lrc.display();
		for (int i = 0; i <= 235120; i += 1000) {
			int time = i;
			System.out.println(i + "@" + lrc.locateStamp(time));
		}
	}

}
