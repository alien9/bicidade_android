package net.bemok.bicidade;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.widget.TextView;
import android.widget.Toast;

public class Bicidade extends Activity {
	IGeoPoint center;
	int zoom;
	int pos;
	boolean centering;
	OrientationEventListener oel;
	boolean portrait;
	MapView map;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bicidade);
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new MyLocationListener();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				5000, 10, locationListener);
		final MapView map = (MapView) this.findViewById(R.id.mapview);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		final IMapController c = map.getController();
		final TextView debug=(TextView) findViewById(R.id.editText1);
		center = new GeoPoint(-23.7, -46.5);
		zoom = 14;
		pos=getResources().getConfiguration().orientation;
		centering=false;
		//portrait=getResources().getConfiguration().orientation!=Configuration.ORIENTATION_LANDSCAPE;
		c.setCenter(center);
		c.setZoom(zoom);
		oel = new OrientationEventListener(this,
				SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int arg0) {
				int p=getResources().getConfiguration().orientation;
				//debug.setText("orient"+pos);
				
				if(pos!=p){
					debug.setText("diff"+pos+" "+p);
					
					return;
				};
				//debug.setText(debug.getText()+"orient");
				/*
				debug.setText(debug.getText()+"mudou "+position);
				position=getResources().getConfiguration().orientation;
				c.setCenter(center);
				c.setZoom(zoom);
				*/
			}
		};
		oel.enable();
		map.setMapListener(new DelayedMapListener(new MapListener() {
			public boolean onZoom(final ZoomEvent e) {
				center = map.getMapCenter();
				zoom = map.getZoomLevel();
				return true;
			}

			public boolean onScroll(final ScrollEvent e) {
				center = map.getMapCenter();
				zoom = map.getZoomLevel();
				return true;
			}
		}, 1000));
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
			centering=true;
			MapView map = (MapView) this.findViewById(R.id.mapview);
			map.getController().setZoom(14);
			return true;
		}
		return false;
	}

	private class MyLocationListener implements LocationListener {

		private static final String TAG = "peganingas";

		@Override
		public void onLocationChanged(Location loc) {
			// editLocation.setText("");
			// pb.setVisibility(View.INVISIBLE);
			Toast.makeText(
					getBaseContext(),
					"Location changed: Lat: " + loc.getLatitude() + " Lng: "
							+ loc.getLongitude(), Toast.LENGTH_SHORT).show();
			String longitude = "Longitude: " + loc.getLongitude();
			Log.v(TAG, longitude);
			String latitude = "Latitude: " + loc.getLatitude();
			Log.v(TAG, latitude);
			map.getController().setCenter(new GeoPoint(loc.getLatitude(),loc.getLongitude()));
			
			/*------- To get city name from coordinates -------- */
			String cityName = null;
			Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
			List<Address> addresses;
			try {
				addresses = gcd.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
				if (addresses.size() > 0)
					System.out.println(addresses.get(0).getLocality());
				cityName = addresses.get(0).getLocality();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
					+ cityName;
			// editLocation.setText(s);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

}