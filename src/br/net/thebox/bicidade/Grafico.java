package br.net.thebox.bicidade;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class Grafico {
	private Bicidade bicidade;
	private JSONObject juca;
	private double alt;
	private double dist;
	private double min;
	private double max;
	public Grafico(Bicidade b, JSONObject j){
		bicidade=b;
		juca=j;
	}
	private float getWidth(){
		DisplayMetrics displayMetrics = bicidade.getResources().getDisplayMetrics();
		float w = displayMetrics.heightPixels;
		if (displayMetrics.heightPixels > displayMetrics.widthPixels)
		w = displayMetrics.widthPixels;
		return w;
	}
	

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void draw() throws JSONException {
		JSONArray pts = juca.getJSONArray("altimetrias");
		alt = juca.getDouble("alt");
		dist = juca.getDouble("dist");
		min = juca.getDouble("min");
		max = juca.getDouble("max");
		float w = this.getWidth();
		float h = w / 3;
		ImageView curse = (ImageView) bicidade.findViewById(R.id.imageView2);
		curse.setVisibility(View.INVISIBLE);
		curse.setMinimumHeight((int) h);
		
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
		pati.setFillType(Path.FillType.EVEN_ODD);
		//pati.setFillType(Path.FillType.EVEN_ODD);
		float x = 10;
		pati.moveTo(x, h-10);
		
		float y = (float) (h - ry * (pts.getJSONArray(0).getDouble(1) - min) - 20);
		pati.lineTo(x, y);
		float relx=0;
		for (int i = 0; i < pts.length(); i++) {
			float x0 = x;
			float y0 = y;
			relx+=pts.getJSONArray(i).getDouble(0);
			x = 10+rx * relx; // pega a distância e transforma em coordenadas
			y = (float) (h - ry * (pts.getJSONArray(i).getDouble(1) - min) - 20);
			pati.lineTo(x, y);
			// pati.quadTo(x0,y0,x, y);//, x0+(x-x0)/2, y0+(y-y0)/2);
			//pati.quadTo((x + x0) / 2, (y + y0) / 2, x, y);
			// pati.quadTo(x, y, x0+(x-x0)/2, y0+(y-y0)/2);
		}
		pati.lineTo(x, h-10);
		pati.lineTo(10, h-10);
		pati.close();
		ca.drawPath(pati, po);
		po.setStrokeWidth(0);
		po.setColor(Color.BLACK);
		po.setTextSize(14);
		ca.drawText(String.format("%.1f", min + alt) + "m", 12, 20, po);
		ca.drawText(String.format("%.1f", min) + "m", 12, h - 10, po);
		ca.drawText(String.format("%.1f", dist / 1000) + "km", x - 40, h / 2 + 5, po);
		ImageView im = (ImageView) bicidade.findViewById(R.id.imageView1);
		im.setImageDrawable(new BitmapDrawable(bicidade.getResources(), bi));
		im.setVisibility(ImageView.VISIBLE);
		//im.setOnDragListener(new Drag());
		FrameLayout la=(FrameLayout) bicidade.findViewById(R.id.grafic);
		la.setOnTouchListener(new Touchy());
	}
	private boolean rua(float relx) {
		// TODO Auto-generated method stub
		return false;
	}
	private class Touchy implements OnTouchListener {
		private GeoPoint y(float x){
			float y;
			float w = getWidth();
			float h = w / 3;
			Log.i("BICIDADE", "posição "+(x/(w-20)));
			try {
				JSONArray pts = juca.getJSONArray("coordinates");
				JSONArray pss = juca.getJSONArray("altimetrias");
				float pos=(float) ((x/(w-20))*dist);
				int j=0;
				float metros=0;
				TextView nome_da_rua=(TextView) bicidade.findViewById(R.id.textView1);
				while(j<pss.length()){
					metros+=pss.getJSONArray(j).getDouble(0);
					if(pos<=metros){
						String rua=pss.getJSONArray(j).getString(3);
						if(rua.equals("")){
							nome_da_rua.setText("");
							nome_da_rua.setVisibility(View.INVISIBLE);
						}else{
							nome_da_rua.setText(rua);
							nome_da_rua.setVisibility(View.VISIBLE);
							
						}

						j=pss.length();
					}
					j++;
				}
				
				
				
				for(int i=0;i<pts.length();i++){
					if(pos<=pts.getJSONArray(i).getDouble(3)) {
						return new GeoPoint(pts.getJSONArray(i).getDouble(1),pts.getJSONArray(i).getDouble(0));
					}
				}
				
				return new GeoPoint(pts.getJSONArray(pts.length()-1).getDouble(1),pts.getJSONArray(pts.length()-1).getDouble(0));
								
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			float x = arg1.getX();
			float w = getWidth();
			ImageView curse = (ImageView) bicidade.findViewById(R.id.imageView2);
			curse.setTranslationX(((x<10)?10:((x>w-20)?w-20:x))-curse.getWidth()/2);
			curse.setVisibility(View.VISIBLE);
			GeoPoint g=y(x);
			if(g!=null)	bicidade.moveCenter(g);
			return true;
		}

	}
}
