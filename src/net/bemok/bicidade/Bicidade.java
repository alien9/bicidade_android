package net.bemok.bicidade;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
<<<<<<< HEAD
=======
import java.util.Locale;

import net.bemok.bicidade.DatabaseHandler;
import net.bemok.bicidade.MapTouchListener;
import net.bemok.bicidade.PolyLine;
import net.bemok.bicidade.myItemGestureListener;

>>>>>>> ff26a99b6a6e3e26bbb994d84e864ecd14a13a83
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Bicidade extends Activity {
	int zoom=14;

	boolean subida = false;

	boolean ciclorota = false;

	boolean contramao = false;

	double centerX = -46.6346139449423;

	double centerY = -23.5452421834264;

	boolean centering;

	MapTouchListener tu;

	OrientationEventListener oel;

	private ItemizedIconOverlay<OverlayItem> origem;

	private ItemizedIconOverlay<OverlayItem> destino;

	private ItemizedIconOverlay<OverlayItem> central;
	PolyLine pl;
	

    private final static String DEBUG_TAG = "FirstLifeLog";

    protected void onRestart() {
        super.onRestart();
        Log.e(DEBUG_TAG, "onRestart executes ...");
    }

    protected void onStart() {
        super.onStart();
        Log.e(DEBUG_TAG, "onStart executes ...");
    }


    protected void onStop() {
        super.onStop();
        Log.e(DEBUG_TAG, "onStop executes ...");
    }

	boolean ocupado=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_bicidade);
		MapView map = (MapView) this.findViewById(R.id.mapview);
		List<OverlayItem> pList = new ArrayList<OverlayItem>();
		OnItemGestureListener<OverlayItem> pOnItemGestureListener = new myItemGestureListener<OverlayItem>();

		ItemizedIconOverlay<OverlayItem> myOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(),
				pList, pOnItemGestureListener);

		//LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//LocationListener mlocListener = new MyLocationListener(this);

		//mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		pl=new PolyLine(this);
		pl.setMap(map);
		map.getOverlays().add(pl);

		origem = addItemized();
		destino = addItemized();
		central = addItemized();
		centering = false;
		tu = new MapTouchListener(this);
		map.getOverlays().add(tu);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		map.setTileSource(TileSourceFactory.CYCLEMAP);
		
		//ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //setProgressBarIndeterminateVisibility(true);
	}
	public void loadMapState(){
		MapView map = (MapView) this.findViewById(R.id.mapview);
		SQLiteDatabase db = (new DatabaseHandler(this.getApplicationContext())).getReadableDatabase();
		String[] args = {};
		Cursor cu = db.rawQuery("select name,value from settings", args);
		while (cu.moveToNext()) {
			String name = cu.getString(0);
			if(name.equals("settings")){
				try {
					JSONObject juke=new JSONObject(cu.getString(1));
					if(juke.has("points")) pl.setPoints(juke.getJSONArray("points"));
					if(juke.has("subida")) subida=juke.getString("subida").equals("true");
					if(juke.has("ciclorota")) ciclorota=juke.getString("ciclorota").equals("true");
					if(juke.has("contramao")) contramao=juke.getString("contramao").equals("true");
					if(juke.has("zoom")) zoom=juke.getInt("zoom");
					if(juke.has("x")) centerX=juke.getDouble("x");
					if(juke.has("y")) centerY=juke.getDouble("y");
					if(juke.has("zoom")) zoom=juke.getInt("zoom");
					if(juke.has("ox")&&juke.has("oy")) this.addMarker(new GeoPoint(juke.getDouble("oy"),juke.getDouble("ox")), R.drawable.origem, false);
					if(juke.has("dx")&&juke.has("dy")) this.addMarker(new GeoPoint(juke.getDouble("dy"),juke.getDouble("dx")), R.drawable.destino, false);
				} catch (JSONException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				}
				
			}
		}
		IMapController c = map.getController();
		ImageView image=(ImageView) findViewById(R.id.imageView1);
		image.setVisibility(ImageView.INVISIBLE);
		cu = db.rawQuery("select * from legend", args);
		while (cu.moveToNext()) {
			byte[] blob=cu.getBlob(0);
			BitmapFactory.Options options = new BitmapFactory.Options();
	        options = new BitmapFactory.Options();
	        options.inDither = false;
	        options.inPurgeable = true;
	        options.inInputShareable = true;
	        options.inTempStorage = new byte[1024 *32];
		    Bitmap bm =   BitmapFactory.decodeByteArray(blob , 0, blob.length, options);
		    image.setImageBitmap(bm);
			image.setVisibility(ImageView.VISIBLE);	
		}
		c.setZoom(zoom);
		c.setCenter(new GeoPoint(centerY, centerX));
	}

	private ItemizedIconOverlay<OverlayItem> addItemized() {
		ArrayList<OverlayItem> mItems = new ArrayList<OverlayItem>();
		DefaultResourceProxyImpl mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		ItemizedIconOverlay<OverlayItem> markers = new ItemizedIconOverlay<OverlayItem>(mItems,
				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
					@Override
					public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
						Toast.makeText(Bicidade.this, item.getTitle(), Toast.LENGTH_LONG).show();
						return true; // We 'handled' this event.
					}

					@Override
					public boolean onItemLongPress(final int index, final OverlayItem item) {
						Toast.makeText(Bicidade.this, item.getTitle(), Toast.LENGTH_LONG).show();
						return false;
					}
				}, mResourceProxy);

		((MapView) this.findViewById(R.id.mapview)).getOverlays().add(markers);
		return markers;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.bicidade, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		for(int i=0;i<menu.size();i++)menu.getItem(i).setEnabled(!ocupado);
		menu.findItem(R.id.inverter).setEnabled((origem.size()>0)&&(destino.size()>0));
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
			tu.setTouchListener(R.id.origem);
			Toast.makeText(getApplicationContext(), R.string.toque_para_origem, Toast.LENGTH_LONG).show();
			return true;
		case R.id.destino:
			tu.setTouchListener(R.id.destino);
			Toast.makeText(getApplicationContext(), R.string.toque_para_destino, Toast.LENGTH_LONG).show();;
			return true;
		case R.id.inverter:
			this.inverte();
			return true;
		case R.id.action_settings:
			item.getSubMenu().findItem(R.id.subida).setChecked(subida);
			item.getSubMenu().findItem(R.id.ciclorota).setChecked(ciclorota);
			item.getSubMenu().findItem(R.id.contramao).setChecked(contramao);
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
		case R.id.contramao:
			contramao = !item.isChecked();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			saveState();
			arrota();
			return false;
		case R.id.remove:
			
			cleanMap();
			
		}
		return false;
	}
	private void cleanMap() {
		origem.removeAllItems();
		destino.removeAllItems();
		((ImageView) findViewById(R.id.imageView1)).setVisibility(ImageView.INVISIBLE);
		pl.setPoints(new JSONArray());
		((MapView) this.findViewById(R.id.mapview)).postInvalidate();
	}

	private void inverte() {
		if((origem.size()==0)||(destino.size()==0)) return;
		GeoPoint a = (origem.getItem(0)).getPoint();
		GeoPoint b = (destino.getItem(0)).getPoint();
		cleanMap();
		addMarker(a,R.id.destino,true);
		addMarker(b,R.id.origem,true);
		arrota();
	}

	public void arrota(){
		if((origem.size()==0)||(destino.size()==0)) return;
		ocupado=true;
		GeoPoint a = (origem.getItem(0)).getPoint();
		GeoPoint b = (destino.getItem(0)).getPoint();
		String u="http://bicidade.net/route/?x0="+a.getLongitude()+"&y0="+a.getLatitude()+"&x1="+b.getLongitude()+"&y1="+b.getLatitude();
		//String u="http://192.168.0.2:8000/route/?x0="+a.getLongitude()+"&y0="+a.getLatitude()+"&x1="+b.getLongitude()+"&y1="+b.getLatitude();
		u+="&alt=1&crit="+((subida)?"subida,":"")+((ciclorota)?"ciclorota,":"")+((contramao)?"":"mao,");
		Brow bro = new Brow(this);
		bro.execute(u);
		setProgressBarIndeterminateVisibility(true);
	}

	public void addMarker(GeoPoint p, int which, boolean geocode) {
		ItemizedIconOverlay<OverlayItem> l = null;
		int t=R.drawable.center;
		switch (which) {
		case 0: // central
		case R.id.center:
			l = central;
			t=R.drawable.center;
			break;
		case 1:
		case R.id.origem:
			l = origem;
			t=R.drawable.origem;
			break;
		case 2:
		case R.id.destino:
			l = destino;
			t=R.drawable.destino;
			break;
		}
		l.removeAllItems();
		OverlayItem olItem = new OverlayItem("Ponto", "SampleDescription", p);
		Drawable newMarker = this.getResources().getDrawable(t);
		olItem.setMarker(newMarker);
		l.addItem(olItem);
		((MapView) this.findViewById(R.id.mapview)).postInvalidate();
		if (geocode) {
			arrota();
		}
	}

	public class MyLocationListener implements LocationListener {

		private Context context;

		public MyLocationListener(Context context) {
			this.context=context;
		}

		@Override
		public void onLocationChanged(Location loc)

		{
			String Text = "My current location is: Latitud = " + loc.getLatitude() + "Longitud = " + loc.getLongitude();
			Toast.makeText(context, Text, Toast.LENGTH_SHORT).show();
			/*
			 * this.mapController.setCenter(new GeoPoint(loc.getLatitude(), loc.getLongitude())); if
			 * (this.mapPoint.size() > 0) this.mapPoint.removeItem(0); Resources res = getResources(); Drawable marker =
			 * res.getDrawable(R.drawable.center); OverlayItem myItem = new OverlayItem("Marker",
			 * "Description of my Marker", new GeoPoint(loc.getLatitude(), loc.getLongitude()));
			 * myItem.setMarker(marker); this.mapPoint.addItem(myItem);
			 */
			((Bicidade) context).addMarker(new GeoPoint(loc.getLatitude(), loc.getLongitude()),
					R.drawable.center, false);
		}

		@Override
		public void onProviderDisabled(String provider)

		{

			//Toast.makeText(getApplicationContext(),

			//"Gps Disabled",

			//Toast.LENGTH_SHORT).show();

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
			JSONArray points;
			if(juca.has("coordinates")){
				points=juca.getJSONArray("coordinates");
				MapView map = (MapView) this.findViewById(R.id.mapview);
				pl.setPoints(points);
				map.postInvalidate();
			}
			if(juca.has("altimetrias")){
				grafico(juca.getJSONArray("altimetrias"),juca.getDouble("alt"),juca.getDouble("dist"),juca.getDouble("min"),juca.getDouble("max"));
			}
		}
		catch (JSONException e) {
			Toast.makeText(this, R.string.invalid_data, Toast.LENGTH_LONG).show();
		}
		ocupado=false;
		setProgressBarIndeterminateVisibility(false);
	}
	public void grafico(JSONArray pts,double alt, double dist, double min, double max) throws JSONException {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    float w = displayMetrics.heightPixels;
	    if(displayMetrics.heightPixels>displayMetrics.widthPixels) w=displayMetrics.widthPixels;
	    float h=w/3;
		Bitmap bi=Bitmap.createBitmap(Math.round(w), Math.round(h), Bitmap.Config.ARGB_8888);
		Canvas ca=new Canvas(bi);
		ca.drawColor(Color.WHITE);
		Paint po=new Paint();
		po.setColor(Color.GRAY);
		po.setAntiAlias(true);
		po.setDither(true);
		po.setStyle(Paint.Style.STROKE);
		po.setStrokeJoin(Paint.Join.ROUND);
		po.setStrokeCap(Paint.Cap.ROUND);
		po.setStrokeWidth(4);
		//multiplicador do X
		float rx=(float) ((w-20)/dist);
		// e da altura:
		float ry=(float) ((h-20)/(max-min));
		Path pati = new Path();
		float x=10;
		float y=(float)(h-ry*(pts.getJSONArray(0).getDouble(1)-min)-10);
		pati.moveTo(x, y); // primeiro ponto virá aqui
		
		for(int i=0;i<pts.length();i++){
			float x0=x;
			float y0=y;
			x+=rx*pts.getJSONArray(i).getDouble(0); // pega a distância
			y=(float) (h-ry*(pts.getJSONArray(i).getDouble(1)-min)-10);
			//pati.lineTo(x, y);
			//pati.quadTo(x0,y0,x, y);//, x0+(x-x0)/2, y0+(y-y0)/2);
			pati.quadTo((x+x0)/2, (y+y0)/2, x,y);
			//pati.quadTo(x, y, x0+(x-x0)/2, y0+(y-y0)/2);
			pati.moveTo(x, y);
			
		}
		ca.drawPath(pati, po);
		po.setStrokeWidth(0);
		po.setColor(Color.BLACK);
		po.setTextSize(14); 
		ca.drawText(String.format("%.1f", min+alt)+"m", 12, 20, po); 
		ca.drawText(String.format("%.1f", min)+"m", 12, h-10, po);
		ca.drawText(String.format("%.1f", dist/1000)+"km", x-40, h/2+5, po);
		ImageView im=(ImageView) findViewById(R.id.imageView1);
		im.setImageDrawable(new BitmapDrawable(getResources(), bi));
		im.setVisibility(ImageView.VISIBLE);
	}
	@Override
	public void onPause() {
		super.onPause(); // Always call the superclass method first

        Log.e(DEBUG_TAG, "onPause executes ...");
		saveState();
		MapView map = (MapView) this.findViewById(R.id.mapview);
		map.destroyDrawingCache();
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
		db.insert("position", null, values);
		db.execSQL("delete from settings");
		JSONObject joke=new JSONObject();
		try {
			joke.put("zoom",map.getZoomLevel());
			joke.put("y",center.getLatitude());
			joke.put("x",center.getLongitude());
			GeoPoint g;
			joke.put("points", pl.getPoints());
			if(origem.size()>0){
				g=origem.getItem(0).getPoint();
				joke.put("oy", g.getLatitude());
				joke.put("ox", g.getLongitude());
			}
			if(destino.size()>0){
				g=destino.getItem(0).getPoint();
				joke.put("dy", g.getLatitude());
				joke.put("dx", g.getLongitude());
			}
			db.execSQL("delete from settings");
			joke.put("subida", subida);
			joke.put("ciclorota", ciclorota);
			joke.put("contramao", contramao);
			values = new ContentValues();
			values.put("name", "settings");
			values.put("value", joke.toString());
			db.insert("settings", null, values);
			
			db.execSQL("delete from legend");
			if ((((ImageView) findViewById(R.id.imageView1)).getVisibility()) == ImageView.VISIBLE) {
				values = new ContentValues();
				Bitmap bi = (Bitmap) ((BitmapDrawable) ((ImageView) findViewById(R.id.imageView1))
						.getDrawable()).getBitmap();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bi.compress(Bitmap.CompressFormat.PNG, 100, bos);
				byte[] bArray = bos.toByteArray();
				values.put("image", bArray);
				db.insert("legend", null, values);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

        Log.e(DEBUG_TAG, "onDestroy executes ...");
		MapView map = (MapView) this.findViewById(R.id.mapview);
		map.destroyDrawingCache();
	}


	@Override
	public void onResume() {
		super.onResume();
		loadMapState();
        Log.e(DEBUG_TAG, "onResume executes ...");
	}
}
