package net.bemok.bicidade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
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
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.widget.TextView;
import android.widget.Toast;

public class Bicidade extends Activity {
	static int zoom = 14;
	static int pos = 3;
	static double centerX = -46.6346139449423;
	static double centerY = -23.5452421834264;
	static ArrayList<OverlayItem> mItems = new ArrayList<OverlayItem>();
	boolean centering;
	OrientationEventListener oel;

	private ItemizedIconOverlay<OverlayItem> mMyLocationOverlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bicidade);
		final MapView map = (MapView) this.findViewById(R.id.mapview);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		List<OverlayItem> pList = new ArrayList<OverlayItem>();
		OnItemGestureListener<OverlayItem> pOnItemGestureListener = new myItemGestureListener<OverlayItem>();

		ItemizedIconOverlay<OverlayItem> myOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(),
				pList, pOnItemGestureListener);

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener mlocListener = new MyLocationListener((MapController) map.getController(), myOverlay);

		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

		DefaultResourceProxyImpl mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

		this.mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(mItems,
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

		map.getOverlays().add(this.mMyLocationOverlay);
		final IMapController c = map.getController();
		centering = false;
		c.setCenter(new GeoPoint(centerY, centerX));
		c.setZoom(zoom);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bicidade, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.center:
			centering = true;
			MapView map = (MapView) this.findViewById(R.id.mapview);
			// map.getController().setZoom(14);
			GeoPoint center = new GeoPoint(centerY, centerX);
			map.getController().setCenter(center);
			OverlayItem olItem = new OverlayItem("Minha", "SampleDescription", center);
			Drawable newMarker = this.getResources().getDrawable(R.drawable.center);
			olItem.setMarker(newMarker);
			mItems.add(olItem);
			map.getOverlays().add(this.mMyLocationOverlay);

			return true;
		}
		return false;
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

			this.mapController.setCenter(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
			if(this.mapPoint.size()>0) this.mapPoint.removeItem(0);

			Resources res = getResources();
			Drawable marker = res.getDrawable(R.drawable.center);
			OverlayItem myItem = new OverlayItem("Marker", "Description of my Marker", new GeoPoint(loc.getLatitude(),
					loc.getLongitude()));
			myItem.setMarker(marker);

			this.mapPoint.addItem(myItem);
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
	@Override
	public void onPause() {
		/*
		 * GeoPoint center = (GeoPoint) map.getMapCenter(); centerX=center.getLongitude(); centerY=center.getLatitude();
		 * zoom = map.getZoomLevel();
		 */
		super.onPause(); // Always call the superclass method first

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MapView map=(MapView) this.findViewById(R.id.mapview);
		 GeoPoint center = (GeoPoint) map.getMapCenter(); centerX=center.getLongitude(); centerY=center.getLatitude();
		 zoom = map.getZoomLevel();
		 DatabaseHandler db=new DatabaseHandler(this.getApplicationContext());
	}

	@Override
	public void onResume() {
		super.onResume();
		MapController c = (MapController) ((MapView) this.findViewById(R.id.mapview)).getController();
		c.setCenter(new GeoPoint(centerY, centerX));
		c.setZoom(zoom);
	}
}
