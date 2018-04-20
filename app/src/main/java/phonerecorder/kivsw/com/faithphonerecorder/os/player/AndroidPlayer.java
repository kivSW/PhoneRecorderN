package phonerecorder.kivsw.com.faithphonerecorder.os.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import phonerecorder.kivsw.com.faithphonerecorder.R;

/**
 * Created by ivan on 4/19/18.
 */

public class AndroidPlayer implements IPlayer{

    public AndroidPlayer() {

    }

    @Override
    public void play(Context activity, String filePath) {
        Intent i= new Intent(Intent.ACTION_VIEW);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        Uri uri = Uri.parse("file://"+filePath);
        i.setDataAndType (uri, "audio/3gp");
        ComponentName ri=i.resolveActivity(activity.getPackageManager());
        if ( ri!= null)
            activity.startActivity(i);
    }

    @Override
    public void playItemWithChooser(Context activity, String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        Uri uri = Uri.parse("file://"+filePath);
        //i.setData(uri);
        i.setDataAndType (uri, "audio/3gp");

        // Always use string resources for UI text.
        // This says something like "Share this photo with"
        String title = activity.getResources().getString(R.string.chooser_title);
        // Create intent to show chooser
        Intent chooser = Intent.createChooser(i, title);
        activity.startActivity(chooser);//!!!
        // Verify the intent will resolve to at least one activity
		/*if (i.resolveActivity(context.getPackageManager()) != null)
			context.startActivity(i);*/
    }
}
