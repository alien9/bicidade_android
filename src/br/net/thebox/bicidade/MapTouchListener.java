package br.net.thebox.bicidade;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.widget.Toast;

public class MapTouchListener extends Overlay {
	int touchlistener = 0;

	private Context context;

	public MapTouchListener(Context ctx) {
		super(ctx);
		this.context = ctx;
	}
	public void setTouchListener(int l){
		touchlistener=l;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView map) {

		if (touchlistener == 0)
			return false;
		GeoPoint p = null;
		if (event.getAction() == MotionEvent.ACTION_UP) {
			
			p = (GeoPoint) map.getProjection().fromPixels((int) event.getX(), (int) event.getY());
			((Bicidade) getBaseContext()).addMarker(p, touchlistener, true);
			touchlistener = 0;
			
		}
		return false;
	}

	private Context getBaseContext() {
		return this.context;
	}

	@Override
	protected void draw(Canvas arg0, MapView arg1, boolean arg2) {
		// TODO Auto-generated method stub

	}
}
