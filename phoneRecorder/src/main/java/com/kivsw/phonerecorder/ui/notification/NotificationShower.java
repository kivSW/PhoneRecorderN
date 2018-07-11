/** this class shows and hides Notofication
 *
 */
package com.kivsw.phonerecorder.ui.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.ui.main_activity.MainActivity;

import javax.inject.Inject;

import phonerecorder.kivsw.com.phonerecorder.R;

public class NotificationShower {

	int notificationId=0;
	protected Context context;
	ISettings settings;

	//---------------------------------------------------------------------------------
	@Inject
	NotificationShower(Context context, ISettings settings, int notificationId)
	{
		this.notificationId = notificationId;
		this.context = context;
		this.settings = settings;
	};


	/*public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}*/

	//---------------------------------------------------------------------------------
	public void finalize() throws Throwable
	{
		super.finalize();
		hide();
	}
	//---------------------------------------------------------------------------------
	// informs the user about this SW
	public void show(String text)
	{
		show(text, -1);
	};

	//---------------------------------------------------------------------------------
	public void show( String text, int percents)
	{
		if(settings.getHiddenMode()) return;

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setSmallIcon(R.drawable.ic_notification_small_horns);
		mBuilder.setLargeIcon( BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_phonerecorder) );
		mBuilder.setContentTitle(context.getText(R.string.app_name).toString());
		mBuilder.setContentText(text);

		if(percents>=0 && percents<=100)
			mBuilder.setProgress(100, percents, false);

		Intent intent=new Intent(Intent.ACTION_MAIN);
		intent.setClass(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mBuilder.setContentIntent(PendingIntent.getActivity(context, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT ));

		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// notificationId allows you to update the notification later on.
		mNotificationManager.notify(notificationId, mBuilder.build());

	}
	//---------------------------------------------------------------------------------
	public void hide()
	{
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(notificationId);
	}

}
