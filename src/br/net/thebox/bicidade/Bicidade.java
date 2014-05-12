package br.net.thebox.bicidade;

import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
public class Bicidade extends Activity{
	int zoom = 14;

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

//development
	 
	static String TWITTER_CONSUMER_KEY = "duNqceMeYKUm7bzrEtaKSbls8";
	static String TWITTER_CONSUMER_SECRET = "AO96wm8wk75WTm6hnP1AqWViYIEwWbH6HXQ3SM8STPZ6YRI6bQ";
	static final String DOMAIN="192.168.0.4:8000";
	
//production

//	static final String TWITTER_CONSUMER_KEY = "msu2BJQAQxMYoZy62punKMdex";
//	static final String TWITTER_CONSUMER_SECRET = "PWyq8kiK7KyLOGNKLqCDtPY6aBbzLX3Tib87nrOF3rCE1lbDjB";
//	static final String DOMAIN="bicidade.com.br";
	
	
	// Preference Constants
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	static final String TWITTER_CALLBACK_URL = "http://bicidade.com.br";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	private static final String TRACKME = "TrackMe";

	private static final String COOKIES = "Cookies";

	protected static final String TOKEN = "Token";

	protected static final String TWITTER_TOKEN = "Twitter";


	boolean ocupado = false;

	private ConnectivityManager connManager;

	private LocationManager mlocManager;

	private MyLocationListener mlocListener;
	
	private int source_id;
	private int target_id;
	private double source_pos;
	private double target_pos;
	

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_bicidade);
		MapView map = (MapView) this.findViewById(R.id.mapview);
		List<OverlayItem> pList = new ArrayList<OverlayItem>();
		OnItemGestureListener<OverlayItem> pOnItemGestureListener = new myItemGestureListener<OverlayItem>();

		ItemizedIconOverlay<OverlayItem> myOverlay = new ItemizedIconOverlay<OverlayItem>(
				getApplicationContext(), pList, pOnItemGestureListener);

		connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		mlocManager = (LocationManager)	getSystemService(Context.LOCATION_SERVICE);
		mlocListener = new MyLocationListener(this);
		

		origem = addItemized();
		destino = addItemized();
		central = addItemized();
		centering = false;
		tu = new MapTouchListener(this);
		map.getOverlays().add(tu);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		map.setTileSource(TileSourceFactory.CYCLEMAP);


		SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences(
				"MyPref", 0);
		if(this.getIntent().hasExtra("provider"))
			prepareTwitter();
		trackMe();
		FrameLayout la=(FrameLayout) findViewById(R.id.grafic);
		la.setOnDragListener(new Drag(this));
		la.setOnTouchListener(new Touchy(this));
	}

	private void prepareTwitter() {
		String cookies = this.getIntent().getStringExtra("cookies");
		Pattern p = Pattern.compile("([^=]+)=([^\\;]*);?\\s?");
		Matcher m = p.matcher(cookies);
		AsyncHttpClient bowser = new AsyncHttpClient();
		PersistentCookieStore cs = new PersistentCookieStore(this.getBaseContext());
		BasicClientCookie coo = null;
		while(m.find()){
			coo = new BasicClientCookie(m.group(1), m.group(2));
			coo.setVersion(1);
			coo.setDomain(Bicidade.DOMAIN.replaceAll(":\\d*$", ""));
			coo.setPath("/");
			cs.addCookie(coo);
		}
		//WebView w = (WebView) findViewById(R.id.webView1);
		//w.loadData(c, "text/html", "utf-8");
		bowser.setCookieStore(cs);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Editor editor = prefs.edit();
		editor.putString(COOKIES, cookies);
		editor.commit();
		bowser.get("http://" + Bicidade.DOMAIN
				+ "/credentials", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String t) {
				try {
					@SuppressWarnings("unused")
					JSONObject j = new JSONObject(t);
					Editor editor = prefs.edit();
					editor.putString(TWITTER_TOKEN, j.getString("twitter"));
					editor.commit();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			@Override
	         public void onFailure(Throwable e, String response) {
	             Log.d("eventstaker", "onFailure method is run... :(");
	         }
		});

		
	}

	public void loadMapState() {
		MapView map = (MapView) this.findViewById(R.id.mapview);
		if(map==null) return;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		String pref=prefs.getString("settings", "");
		try {
			JSONObject ju=new JSONObject(pref);
			subida=ju.getBoolean("subida");
			ciclorota=ju.getBoolean("ciclorota");
			contramao=ju.getBoolean("contramao");
			source_id=ju.getInt("source_id");
			target_id=ju.getInt("target_id");
			source_pos=ju.getDouble("source_pos");
			target_pos=ju.getDouble("target_pos");
			
		}
		catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		pl = new PolyLine(this);
		pl.setMap(map);
		map.getOverlays().add(pl);
		
		
		
		SQLiteDatabase db = (new DatabaseHandler(getBaseContext())).getReadableDatabase();
		String[] args = {};
		Cursor cu = db.rawQuery("select name,value from settings", args);
		while (cu.moveToNext()) {
			String name = cu.getString(0);
			if (name.equals("settings")) {
				try {
					JSONObject juke = new JSONObject(cu.getString(1));
					if (juke.has("points"))
						pl.setPoints(juke.getJSONArray("points"));
					if (juke.has("subida"))
						subida = juke.getString("subida").equals("true");
					if (juke.has("ciclorota"))
						ciclorota = juke.getString("ciclorota").equals("true");
					if (juke.has("contramao"))
						contramao = juke.getString("contramao").equals("true");
					if (juke.has("zoom"))
						zoom = juke.getInt("zoom");
					if (juke.has("x"))
						centerX = juke.getDouble("x");
					if (juke.has("y"))
						centerY = juke.getDouble("y");
					if (juke.has("zoom"))
						zoom = juke.getInt("zoom");
					if (juke.has("ox") && juke.has("oy"))
						this.addMarker(
								new GeoPoint(juke.getDouble("oy"), juke
										.getDouble("ox")), R.id.origem,
								false);
					if (juke.has("dx") && juke.has("dy"))
						this.addMarker(
								new GeoPoint(juke.getDouble("dy"), juke
										.getDouble("dx")), R.id.destino,
								false);
				} catch (JSONException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}

			}
		}
		IMapController c = map.getController();
		ImageView image = (ImageView) findViewById(R.id.imageView1);
		image.setVisibility(ImageView.INVISIBLE);
		cu = db.rawQuery("select * from legend", args);
		while (cu.moveToNext()) {
			byte[] blob = cu.getBlob(0);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options = new BitmapFactory.Options();
			options.inDither = false;
			options.inPurgeable = true;
			options.inInputShareable = true;
			options.inTempStorage = new byte[1024 * 32];
			Bitmap bm = BitmapFactory.decodeByteArray(blob, 0, blob.length,
					options);
			image.setImageBitmap(bm);
			image.setVisibility(ImageView.VISIBLE);
			map.setBuiltInZoomControls(false);
		}
		c.setZoom(zoom);
		c.setCenter(new GeoPoint(centerY, centerX));
		/*if (android.os.Build.VERSION.SDK_INT > 3) {
	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
            .permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}*/
	}

	public void saveState() {
		MapView map = (MapView) this.findViewById(R.id.mapview);
		if(map==null) return;
		GeoPoint center = (GeoPoint) map.getMapCenter();
		SQLiteDatabase db = (new DatabaseHandler(getBaseContext()))
				.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("zoom", map.getZoomLevel());
		values.put("x", center.getLongitude());
		values.put("y", center.getLatitude());
		db.execSQL("delete from position");
		db.insert("position", null, values);
		db.execSQL("delete from settings");
		JSONObject joke = new JSONObject();
		try {
			joke.put("zoom", map.getZoomLevel());
			joke.put("y", center.getLatitude());
			joke.put("x", center.getLongitude());
			GeoPoint g;
			joke.put("points", pl.getPoints());
			if (origem.size() > 0) {
				g = origem.getItem(0).getPoint();
				joke.put("oy", g.getLatitude());
				joke.put("ox", g.getLongitude());
			}
			if (destino.size() > 0) {
				g = destino.getItem(0).getPoint();
				joke.put("dy", g.getLatitude());
				joke.put("dx", g.getLongitude());
			}
			db.execSQL("delete from settings");
			joke.put("subida", subida);
			joke.put("ciclorota", ciclorota);
			joke.put("contramao", contramao);
			joke.put("source_id",source_id);
			joke.put("target_id",target_id);
			joke.put("source_pos",source_pos);
			joke.put("target_pos",target_pos);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			prefs.edit().putString("settings",joke.toString()).commit();
			
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

	private ItemizedIconOverlay<OverlayItem> addItemized() {
		ArrayList<OverlayItem> mItems = new ArrayList<OverlayItem>();
		DefaultResourceProxyImpl mResourceProxy = new DefaultResourceProxyImpl(
				getApplicationContext());
		ItemizedIconOverlay<OverlayItem> markers = new ItemizedIconOverlay<OverlayItem>(
				mItems,
				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
					@Override
					public boolean onItemSingleTapUp(final int index,
							final OverlayItem item) {
						Toast.makeText(Bicidade.this, item.getTitle(),
								Toast.LENGTH_LONG).show();
						return true; // We 'handled' this event.
					}

					@Override
					public boolean onItemLongPress(final int index,
							final OverlayItem item) {
						Toast.makeText(Bicidade.this, item.getTitle(),
								Toast.LENGTH_LONG).show();
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
		for (int i = 0; i < menu.size(); i++)
			menu.getItem(i).setEnabled(!ocupado);
		menu.findItem(R.id.inverter).setEnabled(
				(origem.size() > 0) && (destino.size() > 0));
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
			OverlayItem olItem = new OverlayItem("Minha", "SampleDescription",
					center);
			Drawable newMarker = this.getResources().getDrawable(
					R.drawable.center);
			olItem.setMarker(newMarker);
			central.addItem(olItem);
			// mItems.add(olItem);
			// map.getOverlays().add(this.mMyLocationOverlay);

			return true;
		case R.id.origem:
			tu.setTouchListener(R.id.origem);
			Toast.makeText(getApplicationContext(), R.string.toque_para_origem,
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.destino:
			tu.setTouchListener(R.id.destino);
			Toast.makeText(getApplicationContext(),
					R.string.toque_para_destino, Toast.LENGTH_LONG).show();
			;
			return true;
		case R.id.inverter:
			this.inverte();
			return true;
		case R.id.action_settings:
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			item.getSubMenu().findItem(R.id.subida).setChecked(subida);
			item.getSubMenu().findItem(R.id.ciclorota).setChecked(ciclorota);
			item.getSubMenu().findItem(R.id.contramao).setChecked(contramao);
			item.getSubMenu().findItem(R.id.trackme).setChecked(prefs.getBoolean(TRACKME, true));
			return true;
		case R.id.subida:
			subida = !item.isChecked();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			saveState();
			arrota();
			return true;

		case R.id.ciclorota:
			ciclorota = !item.isChecked();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			saveState();
			arrota();
			return true;
		case R.id.contramao:
			contramao = !item.isChecked();
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			saveState();
			arrota();
			return true;
		case R.id.remove:

			cleanMap();
			return true;
		case R.id.signin:
			new CustomDialog(Bicidade.this,
					R.layout.signin);
			return true;
		case R.id.trackme:
			prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			Editor editor = prefs.edit();
			editor.putBoolean(TRACKME, !item.isChecked());
			editor.commit();
			trackMe();
			return true;
		}
		return true;
	}

	private void trackMe() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		if (prefs.getBoolean(TRACKME, true)) {
			mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
			Toast.makeText(getBaseContext(), "Ligando gps", Toast.LENGTH_LONG).show();
			uploadTracks();
		}else{
			mlocManager.removeUpdates(mlocListener);
			
		}
		
	}

	private void cleanMap() {
		source_id=0;
		target_id=0;
		origem.removeAllItems();
		destino.removeAllItems();
		((ImageView) findViewById(R.id.imageView1))
				.setVisibility(ImageView.INVISIBLE);
		pl.setPoints(new JSONArray());
		MapView map=((MapView) this.findViewById(R.id.mapview));
		map.postInvalidate();			
		map.setBuiltInZoomControls(true);
	}

	private void inverte() {
		if ((origem.size() == 0) || (destino.size() == 0))
			return;
		GeoPoint a = (origem.getItem(0)).getPoint();
		GeoPoint b = (destino.getItem(0)).getPoint();
		cleanMap();
		addMarker(a, R.id.destino, true);
		addMarker(b, R.id.origem, true);
	}

	public void arrota() {
		if ((origem.size() == 0) || (destino.size() == 0))
			return;
		if(!((source_id>0)&&(target_id>0)))return;
		ocupado = true;
		final GeoPoint a = (origem.getItem(0)).getPoint();
		final GeoPoint b = (destino.getItem(0)).getPoint();
		final MapView map = (MapView) findViewById(R.id.mapview);
		
		final double d=(((a.getLatitude()-b.getLatitude())>(a.getLongitude()-b.getLongitude()))? Math.abs((a.getLatitude()-b.getLatitude())) : Math.abs((a.getLongitude()-b.getLongitude())));
		Toast.makeText(this, "d valendo "+d+" e z valendo "+(int) ((int) 3+Math.abs(Math.log(360/d)/Math.log(2))), Toast.LENGTH_LONG).show();
		
		String u="http://"+DOMAIN+"/route/?w0="+source_id+"&w1="+target_id+"&p0="+source_pos+"&p1="+target_pos+"&x0="+a.getLongitude()+"&y0="+a.getLatitude()+"&x1="+b.getLongitude()+"&y1="+b.getLatitude();
		u += "&alt=1&crit=" + ((subida) ? "subida," : "")
				+ ((ciclorota) ? "ciclorota," : "")
				+ ((contramao) ? "" : "mao,");
		AsyncHttpClient bro = new AsyncHttpClient();
		final Bicidade bicidade = this;
		bro.get(u, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(String s) {
				bicidade.draw(s);
			    //map.getController().zoomToSpan(Math.abs(a.getLatitudeE6()-b.getLatitudeE6()),Math.abs(a.getLongitudeE6()-b.getLongitudeE6()));
			    //map.getController().animateTo(new GeoPoint((a.getLatitude()+b.getLatitude())/2.0000001,(a.getLongitude()+b.getLongitude())/2));
				setProgressBarIndeterminateVisibility(false);
			}
			@Override
	         public void onFailure(Throwable e, String response) {
	             Toast.makeText(getBaseContext(), R.string.server_error, Toast.LENGTH_LONG).show();
	             setProgressBarIndeterminateVisibility(false);
	             ocupado=false;
	         }
		});
		setProgressBarIndeterminateVisibility(true);
	}

	public void addMarker(GeoPoint p, int which, boolean reverse_geocode) {
		ItemizedIconOverlay<OverlayItem> l = null;
		int t = R.drawable.center;
		switch (which) {
		case 0: // central
		case R.id.center:
			l = central;
			t = R.drawable.center;
			break;
		case 1:
		case R.id.origem:
			l = origem;
			t = R.drawable.origem;
			break;
		case 2:
		case R.id.destino:
			l = destino;
			t = R.drawable.destino;
			break;
		}
		l.removeAllItems();
		OverlayItem olItem = new OverlayItem("Ponto", "SampleDescription", p);
		Drawable newMarker = this.getResources().getDrawable(t);
		olItem.setMarker(newMarker);
		l.addItem(olItem);
		((MapView) this.findViewById(R.id.mapview)).postInvalidate();
		if(reverse_geocode){
			final Bicidade bicidade = this;
			final int layer=which;
			AsyncHttpClient bowser = new AsyncHttpClient();
			bowser.get("http://"+DOMAIN+"/reverse_geocode/?x="+p.getLongitude()+"&y="+p.getLatitude(), new AsyncHttpResponseHandler(){
				@Override
				public void onSuccess(String ss) {
					try {
						JSONObject j = new JSONObject(ss);
						switch(layer){
						case R.id.origem:
							bicidade.source_id=j.getInt("id");
							bicidade.source_pos=j.getDouble("position");
							break;
						case R.id.destino:
							bicidade.target_id=j.getInt("id");
							bicidade.target_pos=j.getDouble("position");
							break;
						}
						bicidade.addMarker(new GeoPoint(j.getDouble("y"),j.getDouble("x")), layer, false);
					} catch (JSONException e) {
						Toast.makeText(getBaseContext(), R.string.server_error, Toast.LENGTH_LONG).show();
					}
				}
				@Override
		         public void onFailure(Throwable e, String response) {
		             Toast.makeText(getBaseContext(), R.string.not_found, Toast.LENGTH_LONG).show();
		         }
			});
		}else{
			arrota();
		}
	}

	/** End of Class MyLocationListener */

	public void draw(String t) {
		try {
			JSONObject juca = new JSONObject(t);
			JSONArray points;
			if (juca.has("coordinates")) {
				points = juca.getJSONArray("coordinates");
				MapView map = (MapView) this.findViewById(R.id.mapview);
				pl.setPoints(points);
				map.postInvalidate();
				map.setBuiltInZoomControls(false);
			}
			if (juca.has("altimetrias")) {
				Grafico gu=new Grafico(this,juca);
				gu.draw();
			}
		} catch (JSONException e) {
			Toast.makeText(this, R.string.invalid_data, Toast.LENGTH_LONG)
					.show();
		}
		ocupado = false;
		setProgressBarIndeterminateVisibility(false);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
<<<<<<< HEAD
	
=======
	public void grafico(JSONArray pts, double alt, double dist, double min,
			double max) throws JSONException {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		float w = displayMetrics.heightPixels;
		if (displayMetrics.heightPixels > displayMetrics.widthPixels)
			w = displayMetrics.widthPixels;
		float h = w / 3;
		Bitmap bi = Bitmap.createBitmap(Math.round(w), Math.round(h),
				Bitmap.Config.ARGB_8888);
		Canvas ca = new Canvas(bi);
		ca.drawColor(Color.WHITE);
		Paint po = new Paint(Paint.ANTI_ALIAS_FLAG);
		po.setColor(Color.LTGRAY);
		po.setAntiAlias(true);
		po.setDither(true);
		po.setStyle(Paint.Style.FILL_AND_STROKE);
		po.setStrokeJoin(Paint.Join.ROUND);
		po.setStrokeCap(Paint.Cap.ROUND);
		po.setStrokeWidth(4);
		// multiplicador do X
		float rx = (float) ((w - 20) / dist);
		// e da altura:
		float ry = (float) (0.5*(h - 20) / (max - min));
		Path pati = new Path();
		//pati.setFillType(Path.FillType.EVEN_ODD);
		float x = 10;
		pati.moveTo(x, h-10);
		
		float y = (float) (h - ry * (pts.getJSONArray(0).getDouble(1) - min) - 20);
		//pati.lineTo(x, y);
		float relx=0;
		for (int i = 0; i < pts.length(); i++) {
			float x0 = x;
			float y0 = y;
			if(i>0){
				double d=(pts.getJSONArray(i).getDouble(1)-pts.getJSONArray(i-1).getDouble(1))/(pts.getJSONArray(i).getDouble(0));
				if(d<0) d=0;
				if(d>0.08) d=0.08;
				po.setColor(Color.rgb((int) (255*d/0.08), (int) (255-255*d/0.08), (int) (100-100*d/0.08)));
			}
			relx+=pts.getJSONArray(i).getDouble(0);
			x = 10+rx * relx; // pega a distância
			y = (float) (h - ry * (pts.getJSONArray(i).getDouble(1) - min) - 20);
			pati.lineTo(x, y);
			pati.lineTo(x,h-10);
			pati.lineTo(x0, h-10);
			pati.lineTo(x0, y0);
			ca.drawPath(pati, po);
			pati = new Path();
			pati.moveTo(x, y);
			// pati.quadTo(x0,y0,x, y);//, x0+(x-x0)/2, y0+(y-y0)/2);
			//pati.quadTo((x + x0) / 2, (y + y0) / 2, x, y);
			// pati.quadTo(x, y, x0+(x-x0)/2, y0+(y-y0)/2);

		}
		po.setStrokeWidth(0);
		po.setColor(Color.BLACK);
		po.setTextSize(14);
		ca.drawText(String.format("%.1f", min + alt) + "m", 12, 20, po);
		ca.drawText(String.format("%.1f", min) + "m", 12, h - 10, po);
		ca.drawText(String.format("%.1f", dist / 1000) + "km", x - 40,
				h / 2 + 5, po);
		ImageView im = (ImageView) findViewById(R.id.imageView1);
		im.setImageDrawable(new BitmapDrawable(getResources(), bi));
		im.setVisibility(ImageView.VISIBLE);
	}
>>>>>>> 21624d66b6b5cde57eef45f80e7f97db742fb191

	@Override
	public void onPause() {
		super.onPause(); // Always call the superclass method first
		saveState();
		MapView map = (MapView) this.findViewById(R.id.mapview);
		if(map!=null) map.destroyDrawingCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MapView map = (MapView) this.findViewById(R.id.mapview);
		if(map != null) map.destroyDrawingCache();
	}

	@Override
	public void onResume() {
		super.onResume();
		loadMapState();
	}


	public class MyLocationListener implements LocationListener {
	
		private Context context;
	
		public MyLocationListener(Context context) {
			this.context = context;
		}
	
		@Override
		public void onLocationChanged(Location loc)
	
		{
		    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		    NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		    if(mWifi.isAvailable()){
		    	((Bicidade) context).uploadTracks();
		    	mlocManager.removeUpdates(mlocListener);
		    }

		    JSONObject p=new JSONObject();
		    try {
				p.put("x", loc.getLongitude());
				p.put("y", loc.getLatitude());
				p.put("z", loc.getAltitude());
				p.put("t", System.currentTimeMillis());
				p.put("s", loc.getSpeed());
				p.put("a", loc.getAccuracy());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		    
			Toast.makeText(context, p.toString(), Toast.LENGTH_LONG).show();
			
			SQLiteDatabase db = (new DatabaseHandler(getBaseContext())).getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("point", p.toString());
			db.insert("tracks", null, values);

//			((Bicidade) context).addMarker(new GeoPoint(loc.getLatitude(), loc.getLongitude()),	R.drawable.center, false);
		}
	

		@Override
		public void onProviderDisabled(String provider)
	
		{
	
			// Toast.makeText(getApplicationContext(),
	
			// "Gps Disabled",
	
			// Toast.LENGTH_SHORT).show();
	
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

	public void uploadTracks() {
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    if(!mWifi.isAvailable()){
	    	return;
	    }
		SQLiteDatabase db = (new DatabaseHandler(getBaseContext())).getReadableDatabase();
		String[] args = {};
		String res="";
		Cursor cu = db.rawQuery("select point from tracks", args);
		while (cu.moveToNext()) {
			res += cu.getString(0);
		}
		if(res.length()==0)return;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String cookies=prefs.getString(COOKIES, null);
		String twitter=prefs.getString(TWITTER_TOKEN, null);
		
		Pattern p = Pattern.compile("([^=]+)=([^\\;]*);?\\s?");
		AsyncHttpClient bowser = new AsyncHttpClient();
		if(cookies!=null){
			Matcher m = p.matcher(cookies);
			PersistentCookieStore cs = new PersistentCookieStore(this.getBaseContext());
			BasicClientCookie coo = null;
			while(m.find()){
				coo = new BasicClientCookie(m.group(1), m.group(2));
				coo.setVersion(1);
				coo.setDomain(Bicidade.DOMAIN.replaceAll(":\\d*$", ""));
				coo.setPath("/");
				cs.addCookie(coo);
			}
			bowser.setCookieStore(cs);
		}
		RequestParams params = new RequestParams();
		params.put("points", res);
		params.put("twitter", twitter);

		bowser.post("http://" + Bicidade.DOMAIN + "/upload/", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String t) {
				try {
					JSONObject j = new JSONObject(t);
					if(j.getBoolean("success")){
						Toast.makeText(getBaseContext(), "Upload concluido", Toast.LENGTH_LONG);
						SQLiteDatabase db = (new DatabaseHandler(getBaseContext())).getWritableDatabase();
						db.execSQL("delete from tracks");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			@Override
	         public void onFailure(Throwable e, String response) {
	             Log.d("eventstaker", "onFailure method is run... :(");
	         }
		});

		
		
	}
}
