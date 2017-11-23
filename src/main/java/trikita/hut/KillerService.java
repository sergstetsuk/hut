package trikita.hut;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.text.format.DateFormat;
import android.util.Log;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class KillerService extends Service {
	private final Context mContext = this;
	private final IBinder mBinder = new LocalBinder();
	private long mTimeLimit = 0;
    private long mKills = 0;
	private int mLogTicker = 0;
    private long mRestartMoment = 0;
    private long TimerSkips = 0;

	private Timer mTimer = null;
	private TimerTask mTimerTask = null;
    private boolean TimerInUse = false;
	private BroadcastReceiver mReceiver;
	private HashSet<String> AllowedApps;
	private HashSet<String> ForbiddenApps;

	public class LocalBinder extends Binder {
		KillerService getService() {
			return KillerService.this;
		}
	}

	@Override
	public void onCreate() {
		Log.d("KillerService","Service started");
        Date datetime = new Date();
        mRestartMoment = datetime.getTime();
        Log.d("KillerService", "Restart: " + DateFormat.format("yyyy.MM.dd kk:mm:ss", mRestartMoment).toString());

		//~ PendingIntent pendingIntent = new PendingIntent();
        // Start foreground service to avoid unexpected kill
		Intent myIntent = new Intent(this, KillerService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity( this, 0,
				myIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		Notification mNotification = new Notification.Builder(this)
				.setContentTitle("Killer Service")
				.setContentText("") .setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pendingIntent).build();
		startForeground(1, mNotification);

		AllowedApps = new HashSet();
		AllowedApps.add("android");
		AllowedApps.add("trikita.hut");
		AllowedApps.add("org.coolreader");
		AllowedApps.add("com.prodict.ukenf");
		AllowedApps.add("com.sa.uadict.uadictionary.free");
		AllowedApps.add("libro.ebook.pdf.reader");
		AllowedApps.add("com.adobe.reader");

		ForbiddenApps = new HashSet();
		ForbiddenApps.add("com.android.settings");
		//ForbiddenApps.add("com.google.android.googlequicksearchbox");
		//ForbiddenApps.add("com.android.systemui");
		//ForbiddenApps.add("com.asus.quicktools");
		ForbiddenApps.add("com.android.packageinstaller");
		//ForbiddenApps.add("com.asus.floating.docking");

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new ScreenReceiver();
		registerReceiver(mReceiver, filter);
        if (!TimerInUse) {
            mTimer = new Timer();
            mTimerTask = new KillerTimer();
            mTimer.scheduleAtFixedRate(mTimerTask, 0, 100);
            TimerInUse = true;
        } else {
            TimerSkips++;
        }
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class KillerTimer extends TimerTask {
		@Override
		public void run() {
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
			Date datetime = new Date();
			int hour = Integer.parseInt(DateFormat.format("kk", datetime.getTime()).toString());
            if (mLogTicker++ % 50 == 0) {
                Log.d("KillerService","Top Activity is: " + taskInfo.get(0).topActivity.getPackageName()
                    + " mTimeLimit: " + mTimeLimit
                    + "Restart: " + DateFormat.format("yyyy.MM.dd kk:mm:ss",mRestartMoment).toString());
            }
            //Calculate needed values
            boolean isAllowedTime = hour > 18 && hour < 21;
            boolean isAllowedApp = AllowedApps.contains(taskInfo.get(0).topActivity.getPackageName());
            boolean isForbiddenApp = ForbiddenApps.contains(taskInfo.get(0).topActivity.getPackageName());
            boolean isDateChanged = !DateFormat.format("dd",mRestartMoment).toString()
                                .equals(DateFormat.format("dd",datetime.getTime()).toString());
            //Check if kill skipping needed
            //DateChanged condition if we didn't turn on screen between allowed periods
            if(mTimeLimit < 36000 && (!isAllowedTime || isDateChanged)) {
                mTimeLimit = 36000;
                mRestartMoment = datetime.getTime();
            }
			if (isAllowedApp) {
				return;
			}
            if(mTimeLimit % 10 == 0) {
                generateNotification();
            }
			if (isAllowedTime && !isForbiddenApp && mTimeLimit > 0) {
                mTimeLimit--;
                mKills = 0;
                return;
            }
            mKills++;
            //Will kill this app
			Log.d("KillerService","Kill CURRENT Top Activity ::" + taskInfo.get(0).topActivity.getPackageName());
			//~ List<ActivityManager.RunningAppProcessInfo> procInfo = am.getRunningAppProcesses();
			//~ for(int i = 0; i < procInfo.size(); i++){
				//~ if(procInfo.get(i).processName.equalsIgnoreCase(taskInfo.get(0).topActivity.getPackageName())) {
					//~ Log.d("topActivity","Service start killing activity");
					//android.os.Process.killProcess(procInfo.get(i).pid);
				//~ }
			//~ }
			//~ Intent activityIntent = new Intent(mContext,LauncherActivity.class);
			//~ activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//~ startActivity(activityIntent);
            Intent activityIntent = new Intent(mContext, LauncherActivity.class);
			activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            activityIntent.setAction(Intent.ACTION_MAIN);
            activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(activityIntent);

		}
	}

private void generateNotification() {
    Intent myIntent = new Intent(this, KillerService.class);
    PendingIntent pendingIntent = PendingIntent.getActivity( this, 0,
            myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Notification mNotification = new Notification.Builder(this)
				.setContentTitle("Killer Service")
                .setContentText(String.format("Time: %02d:%02d ",mTimeLimit/10/60,(mTimeLimit/10)%60)
                    + String.format("Kills: %d ", mKills)
                    + String.format("Skips: %d ", TimerSkips)
                    + "Restart: " + DateFormat.format("yyyy.MM.dd kk:mm:ss", mRestartMoment).toString())
                .setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pendingIntent).build();
    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    // notificationManager.notify(when, mNotification.build());
    notificationManager.notify(1, mNotification);
}

	public class ScreenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String strAction = intent.getAction();
			if (strAction.equals(Intent.ACTION_SCREEN_ON)){
                if(!TimerInUse) {
                    mTimer = new Timer();
                    mTimerTask = new KillerTimer();
                    mTimer.scheduleAtFixedRate(mTimerTask, 0, 100);
                    TimerInUse = true;
                } else {
                    TimerSkips++;                }
			}
			if (strAction.equals(Intent.ACTION_SCREEN_OFF)){
				mTimer.cancel();
                TimerInUse = false;
			}
		}
	}

}
