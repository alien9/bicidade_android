package br.net.thebox.bicidade;

import br.net.thebox.bicidade.R;
import br.net.thebox.bicidade.R.string;

import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.widget.Toast;

public class PolyLine extends Overlay {
	JSONArray points=new JSONArray();

	Context context;

	private MapView map;

	public PolyLine(Context ctx) {
		super(ctx);
		context = ctx;
	}

	public void setPoints(JSONArray points) {
		this.points = points;
	}
	public JSONArray getPoints(){
		return this.points;
	}

	public void setMap(MapView map) {
		this.map = map;
	}

	@Override
	protected void draw(Canvas canvas, MapView arg1, boolean arg2) {
		canvas.drawColor(Color.TRANSPARENT);
		if(points.length()==0)return;
		Projection p = this.map.getProjection();
		Point topLeftPoint = new Point();
		p.toPixels((GeoPoint) p.fromPixels(0, 0), topLeftPoint);

		Paint pa = new Paint();
		Path pts = new Path();
		try {
			Point pt = new Point();
			JSONArray pg = this.points.getJSONArray(0);
			p.toPixels(new GeoPoint(pg.getDouble(1), pg.getDouble(0)), pt);
			pts.moveTo(pt.x, pt.y);
			String bicycle="zero";
			pa.setAntiAlias(true);
			pa.setDither(true);
			pa.setStyle(Paint.Style.STROKE);
			pa.setStrokeJoin(Paint.Join.ROUND);
			pa.setStrokeCap(Paint.Cap.ROUND);
			pa.setStrokeWidth(10);
			pa.setColor(Color.argb(75, 0, 0, 0));
			for (int i = 0; i < this.points.length(); i++) {
				pg = this.points.getJSONArray(i);
				p.toPixels(new GeoPoint(pg.getDouble(1), pg.getDouble(0)), pt);
				String ju=pg.getString(2);
				//if(!bicycle.equals(ju)||(i==this.points.length()-1)){
				if(!bicycle.equals(ju)){
					if(bicycle.equals(""))
						pa.setColor(Color.argb(75, 0, 0, 0));
					if(bicycle.equals("path"))
						pa.setColor(Color.argb(50, 0, 255, 0));
					if(bicycle.equals("yes"))
						pa.setColor(Color.argb(50, 40, 50, 255));
					if(bicycle.equals("ponte"))
						pa.setColor(Color.argb(50, 255, 0, 0));
					
					canvas.drawPath(pts, pa);
					pts.rewind();
					pts.moveTo(pt.x, pt.y);
					//pts = new Path();			
					bicycle=ju;
				}
				pts.lineTo(pt.x, pt.y);
				pts.moveTo(pt.x, pt.y);
			}
			pa.setColor(Color.argb(75, 0, 0, 0));
			canvas.drawPath(pts, pa);
			pts.reset();
		}
		catch (JSONException e) {
			Toast.makeText(this.context, R.string.invalid_data, Toast.LENGTH_LONG).show();
		}
		
	}

}
