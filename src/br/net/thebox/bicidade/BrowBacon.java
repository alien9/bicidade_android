package br.net.thebox.bicidade;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import android.content.Context;
import android.net.Uri;

public class BrowBacon extends Brow{

	

	public BrowBacon(Context context) {
		super(context);
	}

	@Override
	protected void onPostExecute(String result) {
		publishProgress(false);
		((User) caller).write(result);
		// Do something with result in your activity
	}
	public void setCookie(URI uri,HttpCookie coo){
		CookieStore c=HttpSupport.getCookieStoreInstance();
		c.add(uri, coo);
		
	}
	public static class HttpSupport {
		private static CookieStore _cookieStore;
	
		public static synchronized CookieStore getCookieStoreInstance() {
		    if (_cookieStore == null) {
		        _cookieStore = (CookieStore) new BasicCookieStore();
		    }
		    return (CookieStore) _cookieStore;
		}
	}
}