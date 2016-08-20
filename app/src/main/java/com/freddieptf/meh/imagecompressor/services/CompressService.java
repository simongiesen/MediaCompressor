package com.freddieptf.meh.imagecompressor.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.freddieptf.meh.imagecompressor.R;
import com.freddieptf.meh.imagecompressor.utils.CompressUtils;
import com.freddieptf.meh.imagecompressor.utils.FileUtils;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by freddieptf on 20/07/16.
 */
public class CompressService extends Service implements FFmpegExecuteResponseHandler {

    public static final String EXTRA_WIDTH = "twidth";
    public static final String EXTRA_HEIGHT = "theight";
    public static final String EXTRA_IN_SAMPLE_SIZE = "sample_size";
    public static final String EXTRA_PIC_PATHS = "pic_paths";
    public static final String EXTRA_VID_CMD = "pic_paths";
    public static final String EXTRA_QUALITY = "pic_quality";
    public static final String PROGRESS_UPDATE = "progress_update";
    public static final String TASK_SUCCESS = "success";
    public static final String ACTION_COMPRESS_PIC = "compress_pic";
    public static final String ACTION_COMPRESS_VID = "compress_vid";
    public static final String NUM_TASKS = "num_tasks";
    public static final String VIDEO_DURATION = "vid_duration";
    public static final String CURRENT_PROGRESS = "current_progress";


    String[] paths;
    private static final String TAG = "CompressImgsService";
    public static final int NOTIFICATION_ID = 4546498;
    List<String[]> commands;
    Intent progressIntent;
    long videoDuration = 0;
    int previousProgress = 0;
    int numTasks = 0;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    @Override
    public void onCreate() {
        super.onCreate();
        commands = new ArrayList<>(2);
        progressIntent = new Intent(PROGRESS_UPDATE);
        notificationManager = (NotificationManager) getBaseContext().getSystemService(NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, intent.getAction());
        if(intent.getAction() != null) {
            switch (intent.getAction()) {

                case ACTION_COMPRESS_PIC:
                    Log.d(TAG, ACTION_COMPRESS_PIC);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            paths = intent.getStringArrayExtra(EXTRA_PIC_PATHS);
                            int sampleSize = intent.getIntExtra(EXTRA_IN_SAMPLE_SIZE, 0);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = sampleSize;
                            ArrayList<Uri> uris = new ArrayList<>(paths.length);
                            for (String path : paths) {
                                Uri uri = CompressUtils.compressPic(new File(path), options,
                                        intent.getIntExtra(EXTRA_QUALITY, 0),
                                        intent.getIntExtra(EXTRA_WIDTH, 0),
                                        intent.getIntExtra(EXTRA_HEIGHT, 0));
                                uris.add(uri);
                            }
                            Intent i = new Intent(PROGRESS_UPDATE);
                            i.putExtra("num_pics", paths.length);
                            i.putParcelableArrayListExtra("file_uris", uris);
                            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(i);
                            stopSelf();
                        }
                    });
                    thread.start();
                    break;

                case ACTION_COMPRESS_VID:
                    String[] command = intent.getStringArrayExtra(EXTRA_VID_CMD);
                    commands.add(command);
                    numTasks = commands.size();

                    progressIntent.putExtra(NUM_TASKS, commands.size());
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(progressIntent);

                    videoDuration += TimeUnit.MILLISECONDS.toSeconds(intent.getLongExtra(VIDEO_DURATION, 0));

                    if(commands.size() > 1) break;
                    createProgressNotification();

                    try {
                        CompressUtils.compressVid(getBaseContext(), command, this);
                    } catch (FFmpegCommandAlreadyRunningException e) {
                        e.printStackTrace();
                        FFmpeg.getInstance(getBaseContext()).killRunningProcesses();
                        Toast.makeText(getBaseContext(), "Stopped running process. Try again! ", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onSuccess(String s) {
        commands.remove(0);
        if(commands.isEmpty()) {
            Toast.makeText(getBaseContext(), "Video compression completed successfully!", Toast.LENGTH_LONG).show();
            progressIntent.putExtra(TASK_SUCCESS, true);
            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(progressIntent);
            builder.setOngoing(false)
                    .setProgress(0, 0, false)
                    .setContentText("Video compression completed successfully!");
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    public void onProgress(String s) {
        updateNotification(s);
        progressIntent.putExtra(PROGRESS_UPDATE, s);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(progressIntent);
    }

    @Override
    public void onFailure(String s) {
        Toast.makeText(getBaseContext(), "failure! ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onFinish() {
        Log.d(TAG, commands.size() + "");
        if(commands.size() > 0){
            try {
                CompressUtils.compressVid(getBaseContext(), commands.get(0), CompressService.this);
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        }else {
            FileUtils.deleteTempDir();
            stopSelf();
        }
    }

    private void createProgressNotification(){
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("starting compression tasks")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setProgress(100, 0, true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private int updateNotification(String s){
        long p = getTimeProcessed(s);
        if(p != -1){
            int progress = ((int) (((double) p / (double) videoDuration) * 100));
            if(progress != previousProgress){
                builder.setProgress(100, progress, false)
                        .setContentText("compressing video...");
                notificationManager.notify(NOTIFICATION_ID, builder.build());
                progressIntent.putExtra(CURRENT_PROGRESS, progress);
            }
            previousProgress = progress;
            return progress;
        }
        return previousProgress;
    }

    private long getTimeProcessed(String s){
        long duration = -1;
        if(s.startsWith("frame") && s.contains("time")) {
            String time = "";
            String[] strings = s.trim().split(" ");
            for(String sss : strings){
                if(sss.contains("time")) time = sss;
            }
            time = time.split("=")[1];
            String timeFormat = "hh:mm:ss";
            SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat);
            Calendar calendar = Calendar.getInstance();
            try {
                Date date = dateFormat.parse(time);
                calendar.setTime(date);
                duration = TimeUnit.HOURS.toSeconds(calendar.get(Calendar.HOUR)) +
                        TimeUnit.MINUTES.toSeconds(calendar.get(Calendar.MINUTE)) +
                        TimeUnit.SECONDS.toSeconds(calendar.get(Calendar.SECOND));
                return commands.size() == numTasks ? duration : duration + videoDuration/numTasks;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return duration;
    }


}
