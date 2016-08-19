package com.freddieptf.meh.imagecompressor.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by freddieptf on 18/08/16.
 */
public class FileUtils {

    static String APP_FOLDER_NAME = "MediaCompress-uh";

    private static String getAppPicDir(){
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + APP_FOLDER_NAME + File.separator + "Pictures");
        if(!file.exists()) file.mkdirs(); // oooooh
        return file.getAbsolutePath();
    }

    private static String getAppVidDir(){
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + APP_FOLDER_NAME + File.separator + "Videos");
        if(!file.exists()) file.mkdirs(); // oooooooh
        return file.getAbsolutePath();
    }

    private static String getTempVidDir(){
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + APP_FOLDER_NAME + File.separator + "Videos" + File.separator + "temp");
        if(!file.exists()) file.mkdirs(); // oooooooooooh
        return file.getAbsolutePath();
    }

    public static String getTempVideoPath(String videoName){
        return getTempVidDir() + File.separator + videoName;
    }

    public static void deleteTempDir(){
        File tempDir = new File(getTempVidDir());
        if(tempDir.listFiles().length > 0){
            for(File file : tempDir.listFiles()){
                file.delete();
            }
            tempDir.delete();
        }
    }

    public static String getOutPutPicPath(String picName){
        return getAppPicDir() + File.separator + picName;
    }

    public static String getOutPutVideoPath(String videoName){
        return getAppVidDir() + File.separator + videoName;
    }


}
