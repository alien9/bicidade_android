package net.bemok.bicidade;

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
			/*
			 * mapBackButton.setText(p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6() / 1E6 + "Action is : " +
			 * event.getAction());
			 */
			// return true;
			Toast.makeText(getBaseContext(),
					p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6() / 1E6 + " Action is : " + event.getAction()+" listener = "+touchlistener,
					Toast.LENGTH_SHORT).show();
			int t=(touchlistener==1)? R.drawable.origem : R.drawable.destino;
			((Bicidade) getBaseContext()).addMarker(p, t, true, touchlistener);
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
