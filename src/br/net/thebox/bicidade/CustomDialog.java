package br.net.thebox.bicidade;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
public class CustomDialog extends Dialog{
	public CustomDialog(Context context, int theme) {
		super(context, theme);
		final Context c=context;
		setContentView(theme);
		switch(theme){
		case R.layout.signin:
			this.setTitle(R.string.signin);

		show();
		((com.google.android.gms.common.SignInButton) findViewById(R.id.sign_in_button)).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				((Bicidade) c).googleConnect();
				dismiss();
			}
		});
		

		((ImageButton) findViewById(R.id.facebook)).setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(arg1.getAction()==MotionEvent.ACTION_UP) return false;
				Intent i = new Intent(c, User.class);
				i.getExtras().putString("provider", "facebook");
				c.startActivity(i);
				dismiss();
				return false;
			}});
		((ImageButton) findViewById(R.id.twitter)).setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(arg1.getAction()==MotionEvent.ACTION_UP) return false;
				Intent i = new Intent(c, User.class);
				i.putExtra("provider", "twitter");
				c.startActivity(i);
				dismiss();
				return false;
			}});
		}
	}
}
