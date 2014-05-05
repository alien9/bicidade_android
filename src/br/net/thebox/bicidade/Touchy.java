package br.net.thebox.bicidade;

import org.json.JSONArray;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class Touchy implements OnTouchListener {
	JSONArray pts;
	private float rx;
	private float ry;
	
	public Touchy(JSONArray p, float x, float y) {
		pts=p;
		rx=x;
		ry=y;
		
	}
	private float[] y(float x){
		x/=rx;
		Log.i("BICIDADE",""+x);
		float kilometers=0;
		//for(int i=0;i<pts.length();i++){
			//Log.i("BICIDADE",""+i);
		//}
		
		return null;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.i("BICIDADE",""+arg1.getX());
		y(arg1.getX());
		return true;
	}

}
