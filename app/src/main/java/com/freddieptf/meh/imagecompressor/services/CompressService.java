package com.freddieptf.meh.imagecompressor.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.freddieptf.meh.imagecompressor.utils.CompressUtils;
import com.freddieptf.meh.imagecompressor.utils.FileUtils;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by freddieptf on 20/07/16.
 */
public class CompressService extends Service implements FFmpegExecuteResponseHandler {

    public static final String EXTRA_WIDTH          = "twidth";
    public static final String EXTRA_HEIGHT         = "theight";
    public static final String EXTRA_IN_SAMPLE_SIZE = "sample_size";
    public static final String EXTRA_PIC_PATHS      = "pic_paths";
    public static final String EXTRA_VID_CMD        = "pic_paths";
    public static final String EXTRA_QUALITY        = "pic_quality";
    public static final String PROGRESS_UPDATE      = "progress_update";
    public static final String TASK_SUCCESS         = "success";
    public static final String ACTION_COMPRESS_PIC  = "compress_pic";
    public static final String ACTION_COMPRESS_VID  = "compress_vid";


    String[] paths;
    private static final String TAG = "CompressImgsService";
    List<String[]> commands;
    Intent progressIntent;

    public CompressService() {
        commands = new ArrayList<>(2);
        progressIntent = new Intent(PROGRESS_UPDATE);
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
                    if(commands.size() > 1) break;
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
        Toast.makeText(getBaseContext(), "Success", Toast.LENGTH_LONG).show();
        progressIntent.putExtra(TASK_SUCCESS, true);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(progressIntent);
    }

    @Override
    public void onProgress(String s) {
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
        commands.remove(0);
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
}
