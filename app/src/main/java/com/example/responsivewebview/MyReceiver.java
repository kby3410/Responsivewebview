package com.example.responsivewebview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 * @author Administrator
 *	Auto start MyService to init your default settings when first boot.
 */
public class MyReceiver extends BroadcastReceiver {

	private final String TAG = "yu_MyReceiver";
	private final String SHARED_FILE_NAME = "boot_init";
	private final String KEY_BOOT_INIT_FINISH = "boot_init_finish";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		/*if (BuildConfig.ISSENSOR){

		}else {*/
			if(action.equals("android.intent.action.BOOT_COMPLETED")) {
			//	Intent it = new Intent(context, MainActivity.class);
			//	it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//	context.startActivity(it);
			}
		//}

	}


}
