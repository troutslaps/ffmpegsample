package com.troutslaps.ffmpegsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.jcodec.api.android.SequenceEncoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final static int WRITE_STORAGE_REQUEST = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();
    List<String> urls;
    List<String> logos;
    ArrayList<String> savedImageFiles;
    ArrayList<String> savedLogos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_REQUEST);
        }
        urls = Arrays.asList("http://i.imgur.com/hYeR3Xp.jpg", "http://i.imgur.com/IIbEefR.jpg",
                "http://i.imgur.com/F5XvbNY.jpg");
        logos = Arrays.asList("https://sdl-stickershop.line.naver" +
                        ".jp/products/0/0/1/1050431/android/stickers/2099490.png;compress=true",
                "https://sdl-stickershop.line.naver" +
                        ".jp/products/0/0/1/1050431/android/stickers/2099499.png;compress=true");

        savedImageFiles = new ArrayList<>();
        savedLogos = new ArrayList<>();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == WRITE_STORAGE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        } else {

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    private File createOutputFile(String tag) {
        File directory = new File(Environment.getExternalStorageDirectory() + "/" +
                Environment
                        .DIRECTORY_PICTURES + "/videotests/");
        if (!directory.exists()) {
            directory.mkdir();
        }
        File output = new File(directory, tag + "-" + new Date().getTime() + ".mp4");
        return output;
    }



    public void startFfmpegEncoding(View v) {
        downloadImages();
    }

    private void createVideo() {
        final FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d(TAG, "starting to load ffmpeg");
                }

                @Override
                public void onFailure() {
                    Log.d(TAG, "failed to load ffmpeg");
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "succeeded in loading ffmpeg");

                    File directory = new File(Environment.getExternalStorageDirectory() + "/" +
                            Environment
                                    .DIRECTORY_PICTURES + "/videotests/");
                    if (!directory.exists()) {
                        directory.mkdir();
                    }

                    final File output = createOutputFile("ffmpeg");
                    final String[] command = {
                            "-i",
                            savedImageFiles.get(0),
                            "-i",
                            savedImageFiles.get(1),
                            "-i",
                            savedImageFiles.get(2),
                            "-i",
                            savedLogos.get(0),
                            "-i",
                            savedLogos.get(1),
                            "-filter_complex",
                            "[0:v]format=yuva420p,scale=-2:10*ih,zoompan=z='min(max(zoom,pzoom)" +
                                    "+0.0015,1.5)':d=75:s=640x640:x='iw/2-(iw/zoom/2)':y='ih/2-" +
                                    "(ih/zoom/2)',fifo[first];[1:v]format=yuva420p," +
                                    "scale=-2:10*ih,zoompan=z='min(max(zoom,pzoom)+0.0015,1.5)" +
                                    "':d=75:s=640x640:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)'," +
                                    "fade=t=in:st=0:d=1:alpha=1,trim=0:4,setpts=PTS+3/TB," +
                                    "fifo[second];[2:v]format=yuva420p,scale=-2:10*ih," +
                                    "zoompan=z='min(max(zoom,pzoom)+0.0015,1.5)" +
                                    "':d=90:s=640x640:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)'," +
                                    "fade=t=in:st=0:d=1:alpha=1,trim=0:4,setpts=PTS+6/TB," +
                                    "fifo[third];[2:v]format=yuva420p,scale=-2:10*ih," +
                                    "zoompan=z='min(max(zoom,pzoom)+0.0015,1.5)" +
                                    "':d=150:s=640x640:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)'," +
                                    "boxblur=20:1,fifo[blur];[blur][4:v]overlay,fifo[end];" +
                                    "[end]trim=4:7,fade=t=in:st=4:d=1:alpha=1,setpts=PTS+6/TB," +
                                    "fifo[end2];[first][second]overlay,fifo[pair1];" +
                                    "[pair1][third]overlay,fifo[slideshow];" +
                                    "[slideshow][3:v]overlay,fifo[withinfo];" +
                                    "[withinfo][end2]overlay,fifo",
                            "-preset",
                            "ultrafast",
                            "-r",
                            "15",
                            "-crf",
                            "35",
                            "-c:v",
                            "h264_nvenc",
                            "-vcodec",
                            "mpeg2video",
                            output.getAbsolutePath()};
                    Log.d(TAG, "now attempting command");

                    try {
                        // to execute "ffmpeg -version" command you just need to pass "-version"
                        ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                            @Override
                            public void onStart() {
                                long startDate = new Date().getTime();
                                Log.d(TAG, "starting ffmpeg command at startDate = " + startDate);
                            }

                            @Override
                            public void onProgress(String message) {
                                Log.d(TAG, "progress = " + message);
                            }

                            @Override
                            public void onFailure(String message) {
                                Log.d(TAG, "failed executing ffmpeg command = " + message);
                            }

                            @Override
                            public void onSuccess(String message) {
                                Log.d(TAG, "succeeded executing ffmpeg command = " + message + "," +
                                        " " +
                                        output.getAbsolutePath());
                            }

                            @Override
                            public void onFinish() {
                                long endDate = new Date().getTime();
                                Log.d(TAG, "finished executing ffmpeg command = " + output
                                        .getAbsolutePath() + ", at endDate = " + endDate);
                            }
                        });
                    } catch (FFmpegCommandAlreadyRunningException e) {
                        // Handle if FFmpeg is already running
                    }
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "finished loading ffmpeg");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            Log.d(TAG, "failed to load ffmpeg " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void downloadImages() {

        for (int i = 0; i < urls.size(); i++) {
            final int index = i;
            DrawableRequestBuilder builder = Glide.with(this).load(urls.get(i)).centerCrop();
            builder.into(new SimpleTarget<GlideBitmapDrawable>(640, 640) {

                @Override
                public void onResourceReady(GlideBitmapDrawable resource, GlideAnimation<? super
                        GlideBitmapDrawable> glideAnimation) {
                    File directory = new File(Environment.getExternalStorageDirectory() + "/" +
                            Environment.DIRECTORY_PICTURES + "/videotests/");
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                    Bitmap bitmap = resource.getBitmap();
                    File file = new File(directory, String.format("%04d.png", index));
                    try {
                        OutputStream os = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
                        os.flush();
                        os.close();
                        Log.d(TAG, "downloaded = " + file.getAbsolutePath());
                        savedImageFiles.add(file.getAbsolutePath());
                        if (savedImageFiles.size() >= urls.size()) {
                            downloadLogos();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }


    }

    private void downloadLogos() {
        for (int i = 0; i < logos.size(); i++) {
            final int index = i;
            DrawableRequestBuilder builder = Glide.with(this).load(logos.get(i)).centerCrop();
            builder.into(new SimpleTarget<GlideBitmapDrawable>() {

                @Override
                public void onResourceReady(GlideBitmapDrawable resource, GlideAnimation<? super
                        GlideBitmapDrawable> glideAnimation) {
                    File directory = new File(Environment.getExternalStorageDirectory() + "/" +
                            Environment.DIRECTORY_PICTURES + "/videotests/");
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                    Bitmap bitmap = resource.getBitmap();
                    File file = new File(directory, String.format("logo%04d.png", index));
                    try {
                        OutputStream os = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
                        os.flush();
                        os.close();
                        Log.d(TAG, "downloaded = " + file.getAbsolutePath());
                        savedLogos.add(file.getAbsolutePath());
                        if (savedLogos.size() >= logos.size()) {
                            createVideo();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }
}
