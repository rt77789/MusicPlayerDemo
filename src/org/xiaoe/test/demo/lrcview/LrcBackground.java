package org.xiaoe.test.demo.lrcview;

import java.io.FileNotFoundException;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

public class LrcBackground {

	private int backColor;
	private LrcAnimater ball;
	private View view;

	public LrcBackground(View view) throws FileNotFoundException {
		backColor = Color.parseColor("black");
		this.view = view;
		ball = new LrcAnimater(view);
	}

	public void setStamps(Map<Integer, String> stamps) {
		ball.setStamps(stamps);
	}

	public void draw(Canvas canvas) {
		// this is the draw frame phase of the game
		if (canvas == null) {
			Log.d("xiaoe", "canvas == null");
		}
		canvas.drawColor(backColor);
		ball.draw(canvas);
	}

	public void setCurrentLine(int index) {
		ball.setCurrentLine(index);
	}

	public void resize(int width, int height) throws FileNotFoundException {
		// called when the game view is resized
		ball = new LrcAnimater(view);

	}

	public void touch(float x, float y) {
		// called when the screen is touched
		// paddle.touch(x, y);
	}
}
