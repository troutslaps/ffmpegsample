package com.troutslaps.ffmpegsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    File output = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_REQUEST);
        }

        urls = Arrays.asList(
                "http://i.imgur.com/hYeR3Xp.jpg",
                "http://i.imgur.com/IIbEefR.jpg",
                "http://i.imgur.com/F5XvbNY.jpg");

        logos = Arrays.asList(
                "https://sdl-stickershop.line.naver" + ".jp/products/0/0/1/1050431/android/stickers/2099490.png;compress=true",
                "https://sdl-stickershop.line.naver" + ".jp/products/0/0/1/1050431/android/stickers/2099499.png;compress=true");

        savedImageFiles = new ArrayList<>();
        savedLogos = new ArrayList<>();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == WRITE_STORAGE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        } else {

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private File createOutputFile(String tag) {
        File mediaFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/videotests/");

        if (!mediaFile.exists()) {
            mediaFile.mkdir();
        }

        File output = new File(mediaFile, tag + "-" + new Date().getTime() + ".mp4");
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

                    output = createOutputFile("ffmpeg");
                    final String[] command = {
                            "-loop", "1", "-t", "2", "-i", savedImageFiles.get(0),
                            "-loop", "1", "-t", "2", "-i", savedImageFiles.get(1),
                            "-loop", "1", "-t", "2", "-i", savedImageFiles.get(2),
                            "-filter_complex",
                            "[1:v][0:v]blend=all_expr='A*(if(gte(T,0.5),1,T/0.5))+B*(1-(if(gte(T,0.5),1,T/0.5)))'[b1v];" +
                            "[2:v][1:v]blend=all_expr='A*(if(gte(T,0.5),1,T/0.5))+B*(1-(if(gte(T,0.5),1,T/0.5)))'[b2v];" +
                            "[0:v][b1v][1:v][b2v][2:v]concat=n=5:v=1:a=0,format=yuv420p[v]",
                            "-map", "[v]",
                            "-preset", "ultrafast",
                            "-c:v", "libx264",
                            "-pix_fmt", "yuv420p",
                            "-s", "640x640",
                            "-aspect", "1:1",
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
                                Log.d(TAG, "succeeded executing ffmpeg command = " + message + ", " + output.getAbsolutePath());
                            }

                            @Override
                            public void onFinish() {
                                long endDate = new Date().getTime();
                                Log.d(TAG, "finished executing ffmpeg command = " + output.getAbsolutePath() + ", at endDate = " + endDate);
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
                public void onResourceReady(GlideBitmapDrawable resource, GlideAnimation<? super GlideBitmapDrawable> glideAnimation) {
                    String path = save(resource.getBitmap(), String.format("%04d.jpg", index));
                    if (path != null) {
                        savedImageFiles.add(path);
                        if (savedImageFiles.size() >= urls.size()) {
                            downloadLogos();
                        }
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
                public void onResourceReady(GlideBitmapDrawable resource, GlideAnimation<? super GlideBitmapDrawable> glideAnimation) {
                    String path = save(resource.getBitmap(), String.format("logo%04d.jpg", index));
                    if (path != null) {
                        savedLogos.add(path);
                        if ((savedLogos.size() >= logos.size()) && (output == null)) {
                            createVideo();
                        }
                    }
                }
            });
        }
    }

    @Nullable
    private String save(Bitmap bitmap, String filename) {
        File mediaFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/videotests/");

        if (!mediaFile.exists()) {
            mediaFile.mkdir();
        }

        File file = new File(mediaFile.getPath() + File.separator + filename);
        try {
            OutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);
            os.flush();
            os.close();
            Log.d(TAG, "saved file  = " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
