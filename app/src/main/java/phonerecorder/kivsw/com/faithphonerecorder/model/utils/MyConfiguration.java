package phonerecorder.kivsw.com.faithphonerecorder.model.utils;

/**
 *
 */

public class MyConfiguration {
       static boolean debug=false;
   //static boolean debug=true;
   public static void waitForDebugger()
   {
	   if(debug)
	       android.os.Debug.waitForDebugger();  // for debugging process
	   return ;
   }

   public static void log(String tag, String msg)
   {
	   if(!debug) return;

	   android.util.Log.d(tag, msg);
   }
   public static void log(String tag, String msg, Exception ex)
   {
	   if(!debug) return;

	   android.util.Log.d(tag, msg, ex);
   }
}
