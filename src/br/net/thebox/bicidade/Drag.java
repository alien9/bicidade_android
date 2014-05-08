package br.net.thebox.bicidade;

import android.annotation.TargetApi;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Drag implements OnDragListener {
	Bicidade bicidade;
	public Drag(Bicidade b) {
		bicidade=b;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		Log.i("BIXCIDADE",""+event.getX());
		return false;
	}

}
