package br.net.thebox.bicidade;

import android.content.Context;
import android.webkit.WebView;

public class AuthBrow extends Brow{
	public AuthBrow(Context context) {
		super(context);
	}
	@Override
	protected void onPostExecute(String result) {
		publishProgress(false);
		((Bicidade) caller).setContentView(R.layout.twitter);
		//((WebView)(((Bicidade) caller).findViewById(R.id.webView1))).loadData(result);
	}
}
