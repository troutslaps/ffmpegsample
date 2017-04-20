package com.troutslaps.ffmpegsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity {

    public final static int WRITE_STORAGE_REQUEST = 1000;

    private static final String TAG = MainActivity.class.getSimpleName();

    List<String> urls;
    List<String> logos;
    ArrayList<String> savedImageFiles;
    ArrayList<String> savedLogos;
    File output = null;

    String test = "abcdefghijklmnopqrstuvwxyz";
    private final static int FPS = 16;
    private final static double SECS_PER_IMAGE = 3;
    private final static int IMAGE_SIZE = 640;
    private final static int SCALE_INCREMENT = 4;

    private final static String profilePhotoUrl = "http://i.imgur.com/X3P1lnd.jpg";
    private String profilePhotoPath;

    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urls = Arrays.asList(
                "http://i.imgur.com/hYeR3Xp.jpg",
                "http://i.imgur.com/IIbEefR.jpg",
                "http://i.imgur.com/P1qUUrO.jpg",
                "http://i.imgur.com/hj1tvag.jpg");


        savedImageFiles = new ArrayList<>();
        savedLogos = new ArrayList<>();
        btnStart = (Button) findViewById(R.id.btn_ffmpeg);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_REQUEST);
        } else {
            downloadImages();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == WRITE_STORAGE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImages();
            }
        } else {

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private File createOutputFile(String tag, String extension) {
        File mediaFile = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_PICTURES), "/videotests/");

        if (!mediaFile.exists()) {
            mediaFile.mkdir();
        }

        File output = new File(mediaFile, tag + "-" + new Date().getTime() + "." + extension);
        return output;
    }

    public void startFfmpegEncoding(View v) {
        if (savedImageFiles.size() > 0 && savedLogos.size() > 0) {
            createVideo();
        }
    }

    private void createVideo() {
        final FFmpeg ffmpeg = FFmpeg.getInstance(this);

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {

                    disableButton("starting to load ffmpeg");
                }

                @Override
                public void onFailure() {

                    enableButton();
                    Toast.makeText(MainActivity.this, "failed to load ffmpeg", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess() {
                    disableButton("succeeded in loading ffmpeg");

                    output = createOutputFile("video", "mp4");

                    String[] command = createManualZoompanLargeCommand(savedImageFiles.size());


                    disableButton("now attempting command");

                    try {
                        // to execute "ffmpeg -version" command you just need to pass "-version"
                        ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                            long startDate = 0;
                            long endDate = 0;

                            @Override
                            public void onStart() {
                                startDate = new Date().getTime();
                            }

                            @Override
                            public void onProgress(String message) {

                                float totalDuration = (savedImageFiles.size() + 1) * 3;
                                int startIndex = message.indexOf("time=");
                                if (startIndex > 0) {
                                    String timeStr = message.substring(startIndex + 11,
                                            startIndex +
                                                    15);
                                    float currentProgress = Float.parseFloat(timeStr);
                                    float progress = (currentProgress / totalDuration) * 100.0f;
                                    disableButton(progress + "%");
                                } else {
//                                    Log.d(TAG, "unable to get progress");
                                }


                            }

                            @Override
                            public void onFailure(String message) {
                                enableButton();
                                Toast.makeText(MainActivity.this, "failed executing ffmpeg " +
                                        "command = " + message, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onSuccess(String message) {
                                enableButton();
                                Toast.makeText(MainActivity.this,"succeeded executing ffmpeg command = " + message + "," +
                                        " " + output.getAbsolutePath(), Toast.LENGTH_LONG).show();
                                        Log.d(TAG, "succeeded executing ffmpeg command = " + message + "," +
                                        " " + output.getAbsolutePath());
                            }

                            @Override
                            public void onFinish() {
                                endDate = new Date().getTime();
                                Log.d(TAG, "finished executing ffmpeg command = " + output
                                        .getAbsolutePath() + ", at endDate = " + endDate);
                                long duration = endDate - startDate;
                                float seconds = duration / 1000.0f;
                                Log.d(TAG, "finished creating video in " +
                                        seconds + " seconds / " + (seconds / 60.0f) + " minutes");

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

    private String[] createManualZoompanLargeCommand(int numberOfFiles) {
        ArrayList<String> commands = generateInitialCommands();
        StringBuilder complexFilterBuilder = new StringBuilder();
        StringBuilder setSars = new StringBuilder();
        StringBuilder zoompans = new StringBuilder();
        StringBuilder lastConcat = new StringBuilder();
        int numberOfFramesPerImage = 35;
        int i = 0;
        for (i = 0; i < numberOfFiles; i++) {

            boolean lastImage = i >= numberOfFiles - 1;
            zoompans.append(generateManualZoompanFiltersForInput(i, SCALE_INCREMENT, IMAGE_SIZE,
                    numberOfFramesPerImage, String
                            .format("img%1d", i), true));

            lastConcat.append(String.format("[img%1d]", i));
            if (!lastImage) {
                String sar = generateSetSarForInput(i + 1, String.format("%1dv", i + 1));
                setSars.append(sar);
                StringBuilder blend = new StringBuilder();
                String inputPad1 = String.format("[%1dv]", i + 1);
                String inputPad2 = String.format("[%s%1d]", test.charAt(i), numberOfFramesPerImage);
                blend.append(inputPad1);
                blend.append(inputPad2);
                String duration = Float.toString(1.0f / FPS);
                blend.append(String.format("blend=all_expr='A*(if(gte(T,%s),1,T/%s))+B*" +
                        "(1-(if(gte(T,%s),1,T/%s)))'", duration, duration, duration, duration));
                blend.append(String.format("[b%1dv];", i));
                zoompans.append(blend);
                lastConcat.append(String.format("[b%1dv]", i));
            }

        }

        lastConcat.append(String.format("concat=n=%1d:v=1:a=0,format=yuv420p[slides];", (
                (numberOfFiles * 2) - 1)));
        complexFilterBuilder.append(setSars);
        complexFilterBuilder.append(zoompans);
        complexFilterBuilder.append(lastConcat);

        // generate hold for one second
        // take extra last frame of last image


        String firstOverlayInputPad = String.format("[%1d:v]", i);
        String secondOverlayInputPad = String.format("[%1d:v]", i + 1);
        complexFilterBuilder.append(String.format("[slides]%soverlay[slideshow];",
                firstOverlayInputPad));

        String inputPad = String.format("[%s%1d]", test.charAt(numberOfFiles - 1),
                numberOfFramesPerImage);
        complexFilterBuilder.append(inputPad);
        complexFilterBuilder.append("split=");
        complexFilterBuilder.append(FPS);
        StringBuilder holdFrames = new StringBuilder();
        for (i = 0; i < FPS; i++) {
            holdFrames.append(String.format("[hld%1d]", i));
        }

        complexFilterBuilder.append(holdFrames);
        complexFilterBuilder.append(";");
        complexFilterBuilder.append(holdFrames);
        complexFilterBuilder.append(String.format("concat=n=%1d:v=1:a=0,split[base][blur];", FPS));
        complexFilterBuilder.append(String.format("[base]%soverlay[based];", firstOverlayInputPad));
        complexFilterBuilder.append("[blur]boxblur=20:1,eq=0.3:-0.1:0.7:1:1:1:1:1[iblur];");
        complexFilterBuilder.append(String.format("[iblur]%soverlay,split[blogo][blogo2];",
                secondOverlayInputPad));
        complexFilterBuilder.append("[blogo2]setpts=2*PTS[hold];");
        complexFilterBuilder.append("[blogo]fade=in:st=0:d=1:alpha=1[blurred];");
        complexFilterBuilder.append("[based][blurred]overlay[end];");
        complexFilterBuilder.append("[slideshow][end][hold]concat=n=3:v=1:a=0,format=yuv420p[v]");


        commands.add(complexFilterBuilder.toString());
        commands.add("-map");
        commands.add("[v]");
        commands.add("-preset");
        commands.add("ultrafast");
        commands.add("-c:v");
        commands.add("libx264");
        commands.add("-pix_fmt");
        commands.add("yuv420p");
        commands.add("-t");
        commands.add(String.format("00:00:%02d", (numberOfFiles + 1) * 3));
        commands.add("-s");
        commands.add("640x640");
        commands.add("-aspect");
        commands.add("1:1");
        commands.add("-crf");
        commands.add("28");
        commands.add("-r");
        commands.add(Integer.toString(FPS));
        commands.add(output.getAbsolutePath());

        String[] commandsArr = new String[commands.size()];
        commandsArr = commands.toArray(commandsArr);
        return commandsArr;
    }


    private String generateSetSarForInput(int fileIndex, String outputPad) {
        String sarFormat = "[%1d:v]setsar=sar=1/1[%s];";
        return String.format(sarFormat, fileIndex, outputPad);
    }

    private String generateManualZoompanFiltersForInput(int fileIndex, int zoomIncrement, int size,
                                                        int numberOfFramesNeeded, String
                                                                outputPad, boolean addExtra) {
        StringBuilder filterBuilder = new StringBuilder();
        StringBuilder endConcat = new StringBuilder();
        String inputPad = String.format("[%1d:v]", fileIndex);
        String scale = String.format("scale=-1:%1d", size);
        String cropFormat = "crop=%1d:%1d,";
        int sizeOfImageInFrame = 0;
        String frameOutputPadFormat = "[%s%1d]";
        String frameOutputPad = "";
        int framesToGenerate = numberOfFramesNeeded + (addExtra ? 1 : 0);
        for (int i = 0; i < framesToGenerate; i++) {
            sizeOfImageInFrame = size - (i * zoomIncrement);
            filterBuilder.append(inputPad);
            filterBuilder.append(String.format(cropFormat, sizeOfImageInFrame,
                    sizeOfImageInFrame));
            filterBuilder.append(scale);
            frameOutputPad = String.format(frameOutputPadFormat, test.charAt(fileIndex), i);


            boolean isExtraFrame = i == (framesToGenerate - 1) && addExtra;
            if (isExtraFrame) {
                filterBuilder.append(",setsar=sar=1/1");
            } else {
                endConcat.append(frameOutputPad);
            }
            filterBuilder.append(frameOutputPad);
            filterBuilder.append(";");

        }
        endConcat.append(String.format("concat=n=%1d:v=1:a=0,format=yuv420p",
                numberOfFramesNeeded));
        endConcat.append(String.format("[%s];", outputPad));
        filterBuilder.append(endConcat);


        return filterBuilder.toString();
    }


    @NonNull
    private ArrayList<String> generateInitialCommands() {
        ArrayList<String> commands = new ArrayList<>();
        for (String savedImage : savedImageFiles) {
            commands.add("-loop");
            commands.add("1");
            commands.add("-t");
            commands.add("0.0625");
            commands.add("-i");
            commands.add(savedImage);
        }

        for (String savedLogo : savedLogos) {
            commands.add("-loop");
            commands.add("1");
            commands.add("-t");
            commands.add("0.0625");
            commands.add("-i");
            commands.add(savedLogo);
        }

        commands.add("-filter_complex");
        return commands;
    }


    private String getFilenameTag(int scaleFactor, boolean centerZoom) {
        return "scale-factor-" + scaleFactor + "-zoom-" + (centerZoom ? "center" : "topleft");
    }

    private void downloadImages() {
        disableButton("preparing resources...");
        for (int i = 0; i < urls.size(); i++) {
            final int index = i;
            DrawableRequestBuilder builder = Glide.with(this).load(urls.get(i)).centerCrop();
            builder.into(new SimpleTarget<GlideBitmapDrawable>(640, 640) {

                @Override
                public void onResourceReady(GlideBitmapDrawable resource, GlideAnimation<? super
                        GlideBitmapDrawable> glideAnimation) {
                    String path = save(resource.getBitmap(), String.format("%04d.jpg", index),
                            false);
                    if (path != null) {
                        savedImageFiles.add(path);
                        if (savedImageFiles.size() >= urls.size()) {
                            downloadProfilePhoto();
                        }
                    }
                }
            });
        }
    }

    private void downloadProfilePhoto() {
        DrawableRequestBuilder builder = Glide.with(this).load(profilePhotoUrl).centerCrop()
                .bitmapTransform(new CropCircleTransformation(this));
        builder.into(new SimpleTarget<GlideBitmapDrawable>(120, 120) {

            @Override
            public void onResourceReady(GlideBitmapDrawable resource, GlideAnimation<? super
                    GlideBitmapDrawable> glideAnimation) {
                String path = save(resource.getBitmap(), "profile.png", true);
                if (path != null) {
                    profilePhotoPath = path;
                    createLogos();
                }
            }
        });
    }

    private void createLogos() {
        Bitmap transparentBitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config
                .ARGB_8888);
        try {
            String firstLogoPath = createOutputFile("logo1", "png").getAbsolutePath();
            ShareExperienceUtils.prepareSingleImageFile(transparentBitmap, firstLogoPath,
                    this, "Photography Contest", "Manila, Philippines");
            savedLogos.add(firstLogoPath);
            savedLogos.add(firstLogoPath);
            enableButton();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void enableButton() {
        btnStart.setEnabled(true);
        btnStart.setText("start encoding");
    }

    private void disableButton(String text) {
        btnStart.setEnabled(false);
        btnStart.setText(text);
    }
    @Nullable
    private String save(Bitmap bitmap, String filename, boolean png) {
        File mediaFile = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_PICTURES), "/videotests/");

        if (!mediaFile.exists()) {
            mediaFile.mkdir();
        }

        File file = new File(mediaFile.getPath() + File.separator + filename);
        try {
            OutputStream os = new FileOutputStream(file);
            if (png) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, os);
            }

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


//    private String[] createManualZoompanLargeCommand(int fps) {
//        ArrayList<String> commands = generateInitialCommands();
//        double totalDurationInSeconds = (SECS_PER_IMAGE * savedImageFiles.size());
//        int numOfFrames = (int) totalDurationInSeconds * fps;
//        int numOfFramesPerImage = numOfFrames / savedImageFiles.size();
//        double durationPerFrame = totalDurationInSeconds / numOfFrames;
//
//        String complexFilter = "[1:v]setsar=sar=1/1[1v];[2:v]setsar=sar=1/1[2v];" +
//                "[3:v]setsar=sar=1/1[3v];";
//        complexFilter += " [0:v]crop=640:640,scale=-1:640[a0];[0:v]crop=636:636,
// scale=-1:640[a1];" +
//                "[0:v]crop=632:632,scale=-1:640[a2];[0:v]crop=624:624,scale=-1:640[a3];" +
//                "[0:v]crop=620:620,scale=-1:640[a4];[0:v]crop=616:616,scale=-1:640[a5];" +
//                "[0:v]crop=612:612,scale=-1:640[a6];[0:v]crop=608:608,scale=-1:640[a7];" +
//                "[0:v]crop=604:604,scale=-1:640[a8];[0:v]crop=600:600,scale=-1:640[a9];" +
//                "[0:v]crop=596:596,scale=-1:640[a10];[0:v]crop=592:592,scale=-1:640[a11];" +
//                "[0:v]crop=588:588,scale=-1:640[a12];[0:v]crop=584:584,scale=-1:640[a13];" +
//                "[0:v]crop=580:580,scale=-1:640[a14];[0:v]crop=576:576,scale=-1:640[a15];" +
//                "[0:v]crop=572:572,scale=-1:640[a16];[0:v]crop=568:568,scale=-1:640[a17];" +
//                "[0:v]crop=564:564,scale=-1:640[a18];[0:v]crop=560:560,scale=-1:640[a19];" +
//                "[0:v]crop=550:550,scale=-1:640[a20];[0:v]crop=546:546,scale=-1:640[a21];" +
//                "[0:v]crop=542:542,scale=-1:640[a22];[0:v]crop=538:538,scale=-1:640[a23];" +
//                "[0:v]crop=534:534,scale=-1:640[a24];[0:v]crop=530:530,scale=-1:640[a25];" +
//                "[0:v]crop=526:526,scale=-1:640[a26];[0:v]crop=522:522,scale=-1:640[a27];" +
//                "[0:v]crop=518:518,scale=-1:640[a28];[0:v]crop=514:514,scale=-1:640[a29];" +
//                "[0:v]crop=510:510,scale=-1:640[a30];[0:v]crop=506:506,scale=-1:640[a31];" +
//                "[0:v]crop=502:502,scale=-1:640[a32];[0:v]crop=498:498,scale=-1:640[a33];" +
//                "[0:v]crop=494:494,scale=-1:640[a34];[0:v]crop=490:490,scale=-1:640," +
//                "setsar=sar=1/1[a35];" +
//                "[a0][a1][a2][a3][a4][a5][a6][a7][a8][a9][a10][a11][a12][a13][a14][a15][a16
// ][a17" +
//                "][a18][a19][a20][a21][a22][a23][a24][a25][a26][a27][a28][a29][a30][a31][a32
// ][a33" +
//                "][a34]concat=n=35:v=1:a=0[image1];[1v][a35]blend=all_expr='A*(if(gte(T,0.0625)
// ," +
//                "1,T/0.0625))+B*(1-(if(gte(T,0.0625),1,T/0.0625)))'[b1v];\n" +
//                " [1:v]crop=640:640,scale=-1:640[b0];[1:v]crop=636:636,scale=-1:640[b1];" +
//                "[1:v]crop=632:632,scale=-1:640[b2];[1:v]crop=624:624,scale=-1:640[b3];" +
//                "[1:v]crop=620:620,scale=-1:640[b4];[1:v]crop=616:616,scale=-1:640[b5];" +
//                "[1:v]crop=612:612,scale=-1:640[b6];[1:v]crop=608:608,scale=-1:640[b7];" +
//                "[1:v]crop=604:604,scale=-1:640[b8];[1:v]crop=600:600,scale=-1:640[b9];" +
//                "[1:v]crop=596:596,scale=-1:640[b10];[1:v]crop=592:592,scale=-1:640[b11];" +
//                "[1:v]crop=588:588,scale=-1:640[b12];[1:v]crop=584:584,scale=-1:640[b13];" +
//                "[1:v]crop=580:580,scale=-1:640[b14];[1:v]crop=576:576,scale=-1:640[b15];" +
//                "[1:v]crop=572:572,scale=-1:640[b16];[1:v]crop=568:568,scale=-1:640[b17];" +
//                "[1:v]crop=564:564,scale=-1:640[b18];[1:v]crop=560:560,scale=-1:640[b19];" +
//                "[1:v]crop=550:550,scale=-1:640[b20];[1:v]crop=546:546,scale=-1:640[b21];" +
//                "[1:v]crop=542:542,scale=-1:640[b22];[1:v]crop=538:538,scale=-1:640[b23];" +
//                "[1:v]crop=534:534,scale=-1:640[b24];[1:v]crop=530:530,scale=-1:640[b25];" +
//                "[1:v]crop=526:526,scale=-1:640[b26];[1:v]crop=522:522,scale=-1:640[b27];" +
//                "[1:v]crop=518:518,scale=-1:640[b28];[1:v]crop=514:514,scale=-1:640[b29];" +
//                "[1:v]crop=510:510,scale=-1:640[b30];[1:v]crop=506:506,scale=-1:640[b31];" +
//                "[1:v]crop=502:502,scale=-1:640[b32];[1:v]crop=498:498,scale=-1:640[b33];" +
//                "[1:v]crop=494:494,scale=-1:640[b34];[1:v]crop=490:490,scale=-1:640," +
//                "setsar=sar=1/1[b35];" +
//                "[b0][b1][b2][b3][b4][b5][b6][b7][b8][b9][b10][b11][b12][b13][b14][b15][b16
// ][b17" +
//                "][b18][b19][b20][b21][b22][b23][b24][b25][b26][b27][b28][b29][b30][b31][b32
// ][b33" +
//                "][b34]concat=n=35:v=1:a=0[image2];[2v][b35]blend=all_expr='A*(if(gte(T,0.0625)
// ," +
//                "1,T/0.0625))+B*(1-(if(gte(T,0.0625),1,T/0.0625)))'[b2v];\n" +
//                " [2:v]crop=640:640,scale=-1:640[c0];[2:v]crop=636:636,scale=-1:640[c1];" +
//                "[2:v]crop=632:632,scale=-1:640[c2];[2:v]crop=624:624,scale=-1:640[c3];" +
//                "[2:v]crop=620:620,scale=-1:640[c4];[2:v]crop=616:616,scale=-1:640[c5];" +
//                "[2:v]crop=612:612,scale=-1:640[c6];[2:v]crop=608:608,scale=-1:640[c7];" +
//                "[2:v]crop=604:604,scale=-1:640[c8];[2:v]crop=600:600,scale=-1:640[c9];" +
//                "[2:v]crop=596:596,scale=-1:640[c10];[2:v]crop=592:592,scale=-1:640[c11];" +
//                "[2:v]crop=588:588,scale=-1:640[c12];[2:v]crop=584:584,scale=-1:640[c13];" +
//                "[2:v]crop=580:580,scale=-1:640[c14];[2:v]crop=576:576,scale=-1:640[c15];" +
//                "[2:v]crop=572:572,scale=-1:640[c16];[2:v]crop=568:568,scale=-1:640[c17];" +
//                "[2:v]crop=564:564,scale=-1:640[c18];[2:v]crop=560:560,scale=-1:640[c19];" +
//                "[2:v]crop=550:550,scale=-1:640[c20];[2:v]crop=546:546,scale=-1:640[c21];" +
//                "[2:v]crop=542:542,scale=-1:640[c22];[2:v]crop=538:538,scale=-1:640[c23];" +
//                "[2:v]crop=534:534,scale=-1:640[c24];[2:v]crop=530:530,scale=-1:640[c25];" +
//                "[2:v]crop=526:526,scale=-1:640[c26];[2:v]crop=522:522,scale=-1:640[c27];" +
//                "[2:v]crop=518:518,scale=-1:640[c28];[2:v]crop=514:514,scale=-1:640[c29];" +
//                "[2:v]crop=510:510,scale=-1:640[c30];[2:v]crop=506:506,scale=-1:640[c31];" +
//                "[2:v]crop=502:502,scale=-1:640[c32];[2:v]crop=498:498,scale=-1:640[c33];" +
//                "[2:v]crop=494:494,scale=-1:640[c34];[2:v]crop=490:490,scale=-1:640," +
//                "setsar=sar=1/1[c35];" +
//                "[c0][c1][c2][c3][c4][c5][c6][c7][c8][c9][c10][c11][c12][c13][c14][c15][c16
// ][c17" +
//                "][c18][c19][c20][c21][c22][c23][c24][c25][c26][c27][c28][c29][c30][c31][c32
// ][c33" +
//                "][c34]concat=n=35:v=1:a=0[image3];[3v][c35]blend=all_expr='A*(if(gte(T,0.0625)
// ," +
//                "1,T/0.0625))+B*(1-(if(gte(T,0.0625),1,T/0.0625)))'[b3v];\n" +
//                " [3:v]crop=640:640,scale=-1:640[d0];[3:v]crop=636:636,scale=-1:640[d1];" +
//                "[3:v]crop=632:632,scale=-1:640[d2];[3:v]crop=624:624,scale=-1:640[d3];" +
//                "[3:v]crop=620:620,scale=-1:640[d4];[3:v]crop=616:616,scale=-1:640[d5];" +
//                "[3:v]crop=612:612,scale=-1:640[d6];[3:v]crop=608:608,scale=-1:640[d7];" +
//                "[3:v]crop=604:604,scale=-1:640[d8];[3:v]crop=600:600,scale=-1:640[d9];" +
//                "[3:v]crop=596:596,scale=-1:640[d10];[3:v]crop=592:592,scale=-1:640[d11];" +
//                "[3:v]crop=588:588,scale=-1:640[d12];[3:v]crop=584:584,scale=-1:640[d13];" +
//                "[3:v]crop=580:580,scale=-1:640[d14];[3:v]crop=576:576,scale=-1:640[d15];" +
//                "[3:v]crop=572:572,scale=-1:640[d16];[3:v]crop=568:568,scale=-1:640[d17];" +
//                "[3:v]crop=564:564,scale=-1:640[d18];[3:v]crop=560:560,scale=-1:640[d19];" +
//                "[3:v]crop=550:550,scale=-1:640[d20];[3:v]crop=546:546,scale=-1:640[d21];" +
//                "[3:v]crop=542:542,scale=-1:640[d22];[3:v]crop=538:538,scale=-1:640[d23];" +
//                "[3:v]crop=534:534,scale=-1:640[d24];[3:v]crop=530:530,scale=-1:640[d25];" +
//                "[3:v]crop=526:526,scale=-1:640[d26];[3:v]crop=522:522,scale=-1:640[d27];" +
//                "[3:v]crop=518:518,scale=-1:640[d28];[3:v]crop=514:514,scale=-1:640[d29];" +
//                "[3:v]crop=510:510,scale=-1:640[d30];[3:v]crop=506:506,scale=-1:640[d31];" +
//                "[3:v]crop=502:502,scale=-1:640[d32];[3:v]crop=498:498,scale=-1:640[d33];" +
//                "[3:v]crop=494:494,scale=-1:640[d34];[3:v]crop=490:490,scale=-1:640[e0];" +
//                "[3:v]crop=490:490,scale=-1:640[e1];[3:v]crop=490:490,scale=-1:640[e2];" +
//                "[3:v]crop=490:490,scale=-1:640[e3];[3:v]crop=490:490,scale=-1:640[e4];" +
//                "[3:v]crop=490:490,scale=-1:640[e5];[3:v]crop=490:490,scale=-1:640[e6];" +
//                "[3:v]crop=490:490,scale=-1:640[e7];[3:v]crop=490:490,scale=-1:640[e8];" +
//                "[3:v]crop=490:490,scale=-1:640[e9];[3:v]crop=490:490,scale=-1:640[e10];" +
//                "[3:v]crop=490:490,scale=-1:640[e11];[3:v]crop=490:490,scale=-1:640[e12];" +
//                "[3:v]crop=490:490,scale=-1:640[e13];[3:v]crop=490:490,scale=-1:640[e14];" +
//                "[3:v]crop=490:490,scale=-1:640[e15];" +
//                "[d0][d1][d2][d3][d4][d5][d6][d7][d8][d9][d10][d11][d12][d13][d14][d15][d16
// ][d17" +
//                "][d18][d19][d20][d21][d22][d23][d24][d25][d26][d27][d28][d29][d30][d31][d32
// ][d33" +
//                "][d34]concat=n=35:v=1:a=0[image4];" +
//                "[e0][e1][e2][e3][e4][e5][e6][e7][e8][e9][e10][e11][e12][e13][e14][e15]concat=n" +
//                "=16:v=1:a=0,split[base][blur];[base][4:v]overlay[based];[blur]boxblur=20:1," +
//                "eq=0.3:-0.3:0.5:1:1:1:1:1[iblur];[iblur][5:v]overlay,split[blogo][blogo2];" +
//                "[blogo2]setpts=2*PTS[hold];[blogo]fade=in:st=0:d=1:alpha=1[blurred];" +
//                "[based][blurred]overlay[end];" +
//                "[image1][b1v][image2][b2v][image3][b3v][image4]concat=n=7:v=1:a=0[slides];" +
//                "[slides][4:v]overlay[slideshow];[slideshow][end][hold]concat=n=3:v=1:a=0," +
//                "format=yuv420p[v]";
//        commands.add(complexFilter);
//        commands.add("-map");
//        commands.add("[v]");
//        commands.add("-preset");
//        commands.add("ultrafast");
//        commands.add("-c:v");
//        commands.add("libx264");
//        commands.add("-pix_fmt");
//        commands.add("yuv420p");
//        commands.add("-t");
//        commands.add("00:00:15");
//        commands.add("-s");
//        commands.add("640x640");
//        commands.add("-aspect");
//        commands.add("1:1");
//        commands.add("-crf");
//        commands.add("28");
//        commands.add("-r");
//        commands.add(Integer.toString(FPS));
//        commands.add(output.getAbsolutePath());
//
//
//        String[] commandsArr = new String[commands.size()];
//        commandsArr = commands.toArray(commandsArr);
//        return commandsArr;
//    }


}
