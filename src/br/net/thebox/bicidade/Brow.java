package br.net.thebox.bicidade;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

public class Brow extends AsyncTask<String/* Param */, Boolean /* Progress */, String /* Result */> {

	private HttpClient bro = new DefaultHttpClient();

	String t;
	protected Context caller;
    public Brow (Context context){
         caller = context;
    }
	@Override
	protected String doInBackground(String... params) {
		publishProgress(true);
		t = "";
		try {
			HttpPost httppost = new HttpPost(params[0]);

			List nameValuePairs = new ArrayList();
			nameValuePairs.add(new BasicNameValuePair("data", "Atul Yadav"));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

			// Execute HTTP Post Request
			HttpResponse response = bro.execute(httppost);

			InputStream is = response.getEntity().getContent();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer baf = new ByteArrayBuffer(20);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			// TextView lbl = (TextView) findViewById(R.id.ShowResponce);
			t = new String(baf.toByteArray());
		}
		catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			t = e.getMessage();

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			t = e.getMessage();

		}
		return t;
	}

	@Override
	protected void onProgressUpdate(Boolean... progress) {
		// line below coupled with
		// getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
		// before setContentView
		// will show the wait animation on the top-right corner
		// MyActivity.this.setProgressBarIndeterminateVisibility(progress[0]);
	}

	@Override
	protected void onPostExecute(String result) {
		publishProgress(false);
		((Bicidade) caller).draw(result);
		// Do something with result in your activity
	}

}
