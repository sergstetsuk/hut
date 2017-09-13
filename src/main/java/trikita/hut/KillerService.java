package trikita.hut;

import android.app.Service;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashSet;
import android.util.Log;
import android.os.Process;
import android.content.Intent;
import android.content.Context;


public class KillerService extends Service {
	private final Context mContext = this;
	private final IBinder mBinder = new LocalBinder();
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;
	private BroadcastReceiver mReceiver;
	private HashSet<String> AllowedApps;

	public class LocalBinder extends Binder {
		KillerService getService() {
			return KillerService.this;
		}
	}

	@Override
	public void onCreate() {
		Log.d("trikita.hut","Service started");
		AllowedApps = new HashSet();
		AllowedApps.add("trikita.hut");
		AllowedApps.add("org.coolreader");
		AllowedApps.add("com.prodict.ukenf");
		AllowedApps.add("com.sa.uadict.uadictionary.free");
		AllowedApps.add("libro.ebook.pdf.reader");

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new ScreenReceiver();
		registerReceiver(mReceiver, filter);
		mTimer = new Timer();
		mTimerTask = new KillerTimer();
		mTimer.scheduleAtFixedRate(mTimerTask, 0, 500);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class KillerTimer extends TimerTask {
		@Override
		public void run() {
			Log.d("trikita.hut","Service start tick");
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
			Log.d("topActivity","Service start CURRENT Activity ::" + taskInfo.get(0).topActivity.getPackageName());
			/*if (   !taskInfo.get(0).topActivity.getPackageName().equalsIgnoreCase("trikita.hut")
			    && !taskInfo.get(0).topActivity.getPackageName().equalsIgnoreCase("org.coolreader")
			    && !taskInfo.get(0).topActivity.getPackageName().equalsIgnoreCase("com.prodict.ukenf")
			    && !taskInfo.get(0).topActivity.getPackageName().equalsIgnoreCase("com.sa.uadict.uadictionary.free")
			    && !taskInfo.get(0).topActivity.getPackageName().equalsIgnoreCase("libro.ebook.pdf.reader")) {
			*/
			if (!AllowedApps.contains(taskInfo.get(0).topActivity.getPackageName())) {
				Log.d("topActivity","Service start will kill activity");
				List<ActivityManager.RunningAppProcessInfo> procInfo = am.getRunningAppProcesses();
				for(int i = 0; i < procInfo.size(); i++){
					if(procInfo.get(i).processName.equalsIgnoreCase(taskInfo.get(0).topActivity.getPackageName())) {
						Log.d("topActivity","Service start killing activity");
						//android.os.Process.killProcess(procInfo.get(i).pid);
						Intent activityIntent = new Intent(mContext,LauncherActivity.class);
						activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						//activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(activityIntent);
					}
				}
			}

		}
	}
	public class ScreenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String strAction = intent.getAction();
			if (strAction.equals(Intent.ACTION_SCREEN_ON)){
				mTimer = new Timer();
				mTimerTask = new KillerTimer();
				mTimer.scheduleAtFixedRate(mTimerTask, 0, 500);
			}
			if (strAction.equals(Intent.ACTION_SCREEN_OFF)){
				mTimer.cancel();
			}
		}
	}

	public boolean isBlacklisted(long id) {
		//~ SharedPreferences sharedPref = PreferenceManager
				//~ .getDefaultSharedPreferences(this);
		//~ return mContext.getSharedPreferences(PREFS_BLACKLIST, 0).getBoolean(new Long(id).toString(), false);
		return false;
	}


}
