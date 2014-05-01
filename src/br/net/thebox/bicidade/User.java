package br.net.thebox.bicidade;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.ResponseHandlerInterface;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.graphics.Bitmap;

public class User extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter);
		Intent i = getIntent();
		login(i.getExtras().getString("provider"));
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void login(String p) {
		final Context context=this.getBaseContext();
		final String provider = p;
		WebView w = (WebView) findViewById(R.id.webView1);
		w.getSettings().setJavaScriptEnabled(true);
        w.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		w.setWebViewClient(new WebViewClient() {
			boolean redirect = false;
			boolean loadingFinished = true;
			private ProgressDialog pDialog;

			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (!loadingFinished) {
					redirect = true;
				}

				loadingFinished = false;
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				loadingFinished = false;
				pDialog = ProgressDialog.show(view.getContext(), "",
						"Connecting to " + provider + " server", false);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (!redirect) {
					loadingFinished = true;
				}

				if (loadingFinished && !redirect) {
				} else {
					redirect = false;
				}
				if(redirect) return;
				pDialog.dismiss();
				if(!url.contains("/accounts/profile")) return;
				//csrftoken=94TEpAYFcT2qvyUOeNMEac64sTRp5d6L; sessionid=1h8l3i4szoh0kwe6r1xm1tirnfnfo7l2
				String cookies = CookieManager.getInstance().getCookie(url);
				if(cookies==null) return;
				if(cookies.length()<1)return;
				
				Intent i=new Intent(context, Bicidade.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("cookies", cookies);
				i.putExtra("provider", provider);
				context.startActivity(i);
			}
		});
		w.loadUrl("http://"+Bicidade.DOMAIN+"/associate/" + provider);
	}

	public void write(String result) {
		WebView w=(WebView) findViewById(R.id.webView1);
		w.loadData(result, "text/html", "utf-8");
	}

}
