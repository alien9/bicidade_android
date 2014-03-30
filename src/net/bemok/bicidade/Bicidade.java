package net.bemok.bicidade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.Marker;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

public class Bicidade extends Activity {
	static int zoom = 14;

	boolean subida = false;

	boolean ciclorota = false;

	static int pos = 3;

	static double centerX = -46.6346139449423;

	static double centerY = -23.5452421834264;

	boolean centering;

	MapTouchListener tu;

	OrientationEventListener oel;

	private ItemizedIconOverlay<OverlayItem> origem;

	private ItemizedIconOverlay<OverlayItem> destino;

	private ItemizedIconOverlay<OverlayItem> central;
	PolyLine pl;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bicidade);
		final MapView map = (MapView) this.findViewById(R.id.mapview);
		List<OverlayItem> pList = new ArrayList<OverlayItem>();
		OnItemGestureListener<OverlayItem> pOnItemGestureListener = new myItemGestureListener<OverlayItem>();

		ItemizedIconOverlay<OverlayItem> myOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(),
				pList, pOnItemGestureListener);

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener mlocListener = new MyLocationListener((MapController) map.getController(), myOverlay);

		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

		origem = addItemized();
		destino = addItemized();
		central = addItemized();
		final IMapController c = map.getController();
		centering = false;
		c.setCenter(new GeoPoint(centerY, centerX));
		c.setZoom(zoom);
		tu = new MapTouchListener(this);
		map.getOverlays().add(tu);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		map.setTileSource(TileSourceFactory.CYCLEMAP);
		SQLiteDatabase db = (new DatabaseHandler(this.getApplicationContext())).getReadableDatabase();
		String[] args = {};
		Cursor cu = db.rawQuery("select * from settings", args);
		while (cu.moveToNext()) {
			String name = cu.getString(0);
			if (name.equals("subida"))
				subida = cu.getString(1).equals("true");
			if (name.equals("ciclorota"))
				ciclorota = cu.getString(1).equals("true");
		}
	}

	private ItemizedIconOverlay<OverlayItem> addItemized() {
		ArrayList<OverlayItem> mItems = new ArrayList<OverlayItem>();
		DefaultResourceProxyImpl mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		ItemizedIconOverlay<OverlayItem> markers = new ItemizedIconOverlay<OverlayItem>(mItems,
				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
					@Override
					public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
						Toast.makeText(Bicidade.this, "Item '" + item.getTitle(), Toast.LENGTH_LONG).show();
						return true; // We 'handled' this event.
					}

					@Override
					public boolean onItemLongPress(final int index, final OverlayItem item) {
						Toast.makeText(Bicidade.this, "Item '" + item.getTitle(), Toast.LENGTH_LONG).show();
						return false;
					}
				}, mResourceProxy);

		((MapView) this.findViewById(R.id.mapview)).getOverlays().add(markers);
		return markers;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bicidade, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.subida).setChecked(subida);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.center:
			centering = true;
			MapView map = (MapView) this.findViewById(R.id.mapview);
			GeoPoint center = new GeoPoint(centerY, centerX);
			map.getController().setCenter(center);
			OverlayItem olItem = new OverlayItem("Minha", "SampleDescription", center);
			Drawable newMarker = this.getResources().getDrawable(R.drawable.center);
			olItem.setMarker(newMarker);
			central.addItem(olItem);
			//mItems.add(olItem);
			//map.getOverlays().add(this.mMyLocationOverlay);

			return true;
		case R.id.origem:
			tu.setTouchListener(1);
			Toast.makeText(getApplicationContext(), R.string.toque_para_origem, Toast.LENGTH_LONG).show();
			return true;
		case R.id.destino:
			tu.setTouchListener(2);
			Toast.makeText(getApplicationContext(), R.string.toque_para_destino, Toast.LENGTH_LONG).show();;
			return true;
		case R.id.action_settings:
		
			return true;
		case R.id.subida:
			subida = !item.isChecked();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			saveState();
			arrota();
			return false;

		case R.id.ciclorota:
			ciclorota = !item.isChecked();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			saveState();
			arrota();
			return false;
		case R.id.remove:
			origem.removeAllItems();
			destino.removeAllItems();
			// remover também a rota
			((MapView) this.findViewById(R.id.mapview)).postInvalidate();
		}
		return false;
	}
	public void arrota(){
		if((origem.size()==0)||(destino.size()==0)) return;
		GeoPoint a = (origem.getItem(0)).getPoint();
		GeoPoint b = (destino.getItem(0)).getPoint();
		String u="http://alien9.net/route/?x0="+a.getLongitude()+"&y0="+a.getLatitude()+"&x1="+b.getLongitude()+"&y1="+b.getLatitude();
		u+="&crit="+((subida)?"subida,":"")+((ciclorota)?"ciclorota,":"");
		Brow bro = new Brow(this);
		bro.execute(u);		
	}

	public void addMarker(GeoPoint p, int t, boolean geocode, int which) {
		ItemizedIconOverlay<OverlayItem> l = null;
		switch (which) {
		case 0: // central
			l = central;
			break;
		case 1:
			l = origem;
			break;
		case 2:
			l = destino;
			break;
		}
		l.removeAllItems();
		OverlayItem olItem = new OverlayItem("Minha", "SampleDescription", p);
		Drawable newMarker = this.getResources().getDrawable(t);
		olItem.setMarker(newMarker);
		l.addItem(olItem);
		((MapView) this.findViewById(R.id.mapview)).postInvalidate();
		if (geocode) {
			arrota();
			//Brow b = new Brow(getApplicationContext());
			//b.execute("http://alien9.net/route/?y0=-23.598207199999997&x0=-46.6240683&y1=-23.598284349761034&x1=-46.6342806816101&crit=");
		}
	}

	public class MyLocationListener implements LocationListener {
		private MapController mapController;

		private ItemizedIconOverlay<OverlayItem> mapPoint;

		public MyLocationListener(MapController controller, ItemizedIconOverlay<OverlayItem> myOverlay) {
			this.mapController = controller;
			this.mapPoint = myOverlay;
		}

		@Override
		public void onLocationChanged(Location loc)

		{
			String Text = "My current location is: Latitud = " + loc.getLatitude() + "Longitud = " + loc.getLongitude();
			Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
			/*
			 * this.mapController.setCenter(new GeoPoint(loc.getLatitude(), loc.getLongitude())); if
			 * (this.mapPoint.size() > 0) this.mapPoint.removeItem(0); Resources res = getResources(); Drawable marker =
			 * res.getDrawable(R.drawable.center); OverlayItem myItem = new OverlayItem("Marker",
			 * "Description of my Marker", new GeoPoint(loc.getLatitude(), loc.getLongitude()));
			 * myItem.setMarker(marker); this.mapPoint.addItem(myItem);
			 */
			((Bicidade) getApplicationContext()).addMarker(new GeoPoint(loc.getLatitude(), loc.getLongitude()),
					R.drawable.center, false, 0);
		}

		@Override
		public void onProviderDisabled(String provider)

		{

			Toast.makeText(getApplicationContext(),

			"Gps Disabled",

			Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onProviderEnabled(String provider)

		{

			Toast.makeText(getApplicationContext(),

			"Gps Enabled",

			Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)

		{

		}

	}

	/** End of Class MyLocationListener */

	public void draw(String t) {
		try {
			JSONObject juca=new JSONObject(t);
			if(juca.has("coordinates")){
				JSONArray points=juca.getJSONArray("coordinates");
				MapView map = (MapView) this.findViewById(R.id.mapview);
				map.getOverlays().remove(pl);
				pl=new PolyLine(this);
				pl.setPoints(points);
				pl.setMap(map);
				map.getOverlays().add(pl);
				map.postInvalidate();
			}
		}
		catch (JSONException e) {
			Toast.makeText(this, R.string.invalid_data, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onPause() {
		/*
		 * GeoPoint center = (GeoPoint) map.getMapCenter(); centerX=center.getLongitude(); centerY=center.getLatitude();
		 * zoom = map.getZoomLevel();
		 */
		super.onPause(); // Always call the superclass method first

	}

	public void saveState() {
		MapView map = (MapView) this.findViewById(R.id.mapview);
		GeoPoint center = (GeoPoint) map.getMapCenter();
		SQLiteDatabase db = (new DatabaseHandler(this.getApplicationContext())).getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("zoom", map.getZoomLevel());
		values.put("x", center.getLongitude());
		values.put("y", center.getLatitude());
		db.execSQL("delete from position");
		db.execSQL("delete from settings");
		db.insert("position", null, values);
		values = new ContentValues();
		values.put("name", "subida");
		values.put("value", subida);
		db.insert("settings", null, values);
		values = new ContentValues();
		values.put("name", "ciclorota");
		values.put("value", ciclorota);
		db.insert("settings", null, values);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MapView map = (MapView) this.findViewById(R.id.mapview);
		saveState();
		map.destroyDrawingCache();
	}

	@Override
	public void onResume() {
		super.onResume();
		MapController c = (MapController) ((MapView) this.findViewById(R.id.mapview)).getController();
		c.setCenter(new GeoPoint(centerY, centerX));
		c.setZoom(zoom);
	}
}
