package phonerecorder.kivsw.com.faithphonerecorder.os.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

import phonerecorder.kivsw.com.faithphonerecorder.R;

/**
 * play a record with another app
 */
// https://developer.android.com/reference/android/support/v4/content/FileProvider.html
public class AndroidPlayer implements IPlayer{

    public AndroidPlayer() {

    }

    private Uri createURI(Context context, String filePath)
    {
        File file=new File(filePath);
        //Uri photoURI = Uri.fromFile(file);
//Uri uri = Uri.parse("file://"+filePath);
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        return uri;
    }
    @Override
    public void play(Context activity, String filePath) {
        Intent i= new Intent(Intent.ACTION_VIEW);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        Uri uri = createURI(activity, filePath);//Uri.parse("file://"+filePath);
        i.setDataAndType (uri, "audio/3gp");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ComponentName ri=i.resolveActivity(activity.getPackageManager());
        if ( ri!= null)
            activity.startActivity(i);
    }

    @Override
    public void playItemWithChooser(Context activity, String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        Uri uri = createURI(activity, filePath);//Uri.parse("file://"+filePath);
        //i.setData(uri);
        i.setDataAndType (uri, "audio/3gp");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
