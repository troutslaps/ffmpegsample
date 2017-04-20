package com.troutslaps.ffmpegsample;

/**
 * Created by duchess on 20/04/2017.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.TextPaint;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by maicervantes on 05/04/2017.
 */

public class ShareExperienceUtils {

    // set up constants for margins and font sizes (in pixels)
    public static final int IMAGE_WIDTH = 640;
    public static final int IMAGE_HEIGHT = 640;
    private static final float TEXT_SIZE_GOODWALL = 28.0f;
    private static final float TEXT_SIZE_MADE_WITH = 20.0f;
    private static final float TEXT_SIZE_LOCATION = 24.0f;
    private static final float TEXT_SIZE_TITLE = 40.0f;

    private static final int TEXT_COLOR = Color.WHITE;

    private static final int BASE_HORIZONTAL_MARGIN = 24;
    private static final int BASE_VERTICAL_MARGIN = 36;
    private static final int PADDING = 12;
    private static final int TITLE_BOTTOM_MARGIN = 36;
    private static final float LINE_DECOR_HEIGHT = 1.0f;
    private static final int LOGO_TOP_LINE_MARGIN = 12;
    private static final int LOGO_BOTTOM_LINE_MARGIN = 36;

    private static final int GRADIENT_START_COLOR = 0x00FFFFFF;
    private static final int GRADIENT_END_COLOR = 0xFFFFFFFF;

    static String goodwall;
    static String madeWith;
    static String followMyStory;


    @NonNull
    public static String prepareSingleImageFile(Bitmap bitmap, String
            path, Context context, String title, String location) throws IOException {
        // obtain handle to the external writeable directory for the app, getting it through the
        // context allows us to write without asking for permissions

        File file = new File(path);
        OutputStream os = new FileOutputStream(file);
        ShareExperienceUtils.decorateImageForSharing(context, bitmap, title, location);
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
        os.flush();
        os.close();
        return "file://" + file.getAbsolutePath();
    }


    /*
     * convenience method for drawing text on Achievement photo
     */
    private static void decorateImageForSharing(Context context, Bitmap bitmap, String title,
                                                String location) {
        Canvas canvas = new Canvas(bitmap);

        goodwall = "GOODWALL";
        madeWith = "MADE WITH";

        // paint black-to-transparent gradient over the image's bottom half
        BitmapUtils.drawGradientFromBottomOfCanvas(canvas, GRADIENT_START_COLOR, GRADIENT_END_COLOR,
                canvas.getHeight() / 2);

        // draw the 'Made with Goodwall' watermark, positioning from the lower right corner of
        // the canvas
        BitmapUtils.TextLayoutInfo goodWallLayout = drawGoodwallLogo(context, canvas);

        // draw the title, relative to the logo
        BitmapUtils.TextLayoutInfo titleLayout = drawTitleLayout(context, canvas, goodWallLayout,
                title);

        // draw the location, relative to the title
        drawLocation(context, location, canvas, titleLayout);

    }

    private static BitmapUtils.TextLayoutInfo drawGoodwallLogo(Context context, Canvas canvas) {

        Rect baseBounds = new Rect();

        TextPaint goodwallTextPaint = new BitmapUtils.PaintBuilder().withTextColor(TEXT_COLOR)
                .withTextSize(TEXT_SIZE_GOODWALL).withTypeface(TypefaceKarla.getTypefaceBold
                        (context)).build();

        // draw the goodwall label
        BitmapUtils.TextLayoutInfo goodWallLayout = drawGoodwallLabelInLogo(canvas, baseBounds,
                goodwallTextPaint);

        // draw the "made with" label, determine position relative to goodwall label
        BitmapUtils.TextLayoutInfo madeWithLayout = drawMadeWithLabelInLogo(context, canvas,
                baseBounds, goodWallLayout);

        // set up paint for the lines
        Paint paint = new Paint();
        paint.setColor(TEXT_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINE_DECOR_HEIGHT);

        // draw line on top
        canvas.drawLine(goodWallLayout.getRectPosition().right - baseBounds.width(),
                madeWithLayout.getRectPosition().top - LOGO_TOP_LINE_MARGIN, goodWallLayout
                        .getRectPosition().right, madeWithLayout.getRectPosition().top -
                        LOGO_TOP_LINE_MARGIN, paint);

        // draw line at the bottom
        canvas.drawLine(goodWallLayout.getRectPosition().right - baseBounds.width(), canvas
                        .getHeight() - LOGO_BOTTOM_LINE_MARGIN, goodWallLayout.getRectPosition()
                        .right,
                canvas.getHeight() - LOGO_BOTTOM_LINE_MARGIN, paint);

        // return layout of goodwall label for use in measuring the size available for the title
        // and the location
        return goodWallLayout;
    }

    /*
     *  a convenience method for drawing the goodwall label in the logo on the lower right-hand side
     */
    private static BitmapUtils.TextLayoutInfo drawGoodwallLabelInLogo(Canvas canvas, Rect
            baseBounds, TextPaint goodwallTextPaint) {
        // get the rect of the Goodwall label to have an idea about the size
        goodwallTextPaint.getTextBounds(goodwall, 0, goodwall.length(), baseBounds);

        BitmapUtils.TextLayoutInfo goodWallLayout = new BitmapUtils.LayoutBuilder()
                .drawText(goodwall)
                .usingTextPaint(goodwallTextPaint)
                .onCanvas(canvas)
                .pinnedToCorner(BitmapUtils.ImageCorner.LowerRight)
                .withHorizontalMargin(BASE_HORIZONTAL_MARGIN)
                .withVerticalMargin(BASE_VERTICAL_MARGIN + PADDING)
                .fitToWidth(baseBounds.width() + (PADDING * 2))
                .withAlignment(Layout.Alignment.ALIGN_OPPOSITE).build();

        BitmapUtils.drawTextOnCanvas(canvas, goodWallLayout);
        return goodWallLayout;
    }

    private static BitmapUtils.TextLayoutInfo drawMadeWithLabelInLogo(Context context, Canvas
            canvas, Rect baseBounds, BitmapUtils.TextLayoutInfo goodWallLayout) {

        TextPaint madeWithTextPaint = new BitmapUtils.PaintBuilder().withTextColor(TEXT_COLOR)
                .withTextSize(TEXT_SIZE_MADE_WITH).withTypeface(TypefaceKarla.getRegularTypeface
                        (context))
                .build();

        int margin = (canvas.getHeight() - goodWallLayout.getRectPosition().top) +
                PADDING;

        BitmapUtils.TextLayoutInfo madeWithLayout = new BitmapUtils.LayoutBuilder()
                .drawText(madeWith)
                .usingTextPaint(madeWithTextPaint)
                .onCanvas(canvas)
                .pinnedToCorner(BitmapUtils.ImageCorner.LowerRight)
                .withHorizontalMargin(BASE_HORIZONTAL_MARGIN)
                .withVerticalMargin(margin)
                .fitToWidth(baseBounds.width())
                .withAlignment(Layout.Alignment.ALIGN_CENTER).build();

        BitmapUtils.drawTextOnCanvas(canvas, madeWithLayout);
        return madeWithLayout;
    }

    private static BitmapUtils.TextLayoutInfo drawTitleLayout(Context context, Canvas canvas,
                                                              BitmapUtils.TextLayoutInfo
                                                                      goodWallLayout, String
                                                                      title) {
        // get remaining width in canvas for the title, subtract margins on left and right side
        // as well, to determine the title's final maximum width
        TextPaint titleTextPaint = new BitmapUtils.PaintBuilder().withTextColor(TEXT_COLOR)
                .withTextSize(TEXT_SIZE_TITLE).withTypeface(TypefaceKarla.getTypefaceBold(context))
                .build();

        int maxTitleWidth = canvas.getWidth() - goodWallLayout.getRectPosition().width() -
                (BASE_HORIZONTAL_MARGIN * 2);

        BitmapUtils.TextLayoutInfo titleLayout = new BitmapUtils.LayoutBuilder()
                .drawText(title)
                .usingTextPaint(titleTextPaint)
                .onCanvas(canvas)
                .pinnedToCorner(BitmapUtils.ImageCorner.LowerLeft)
                .withHorizontalMargin(BASE_HORIZONTAL_MARGIN)
                .withVerticalMargin(TITLE_BOTTOM_MARGIN)
                .fitToWidth(maxTitleWidth)
                .withAlignment(Layout.Alignment.ALIGN_NORMAL).build();

        BitmapUtils.drawTextOnCanvas(canvas, titleLayout);
        return titleLayout;
    }


    private static void drawLocation(Context context, String location, Canvas canvas,
                                     BitmapUtils.TextLayoutInfo titleLayout) {
        TextPaint locationTextPaint = new BitmapUtils.PaintBuilder().withTextColor(TEXT_COLOR)
                .withTextSize(TEXT_SIZE_LOCATION).withTypeface(TypefaceKarla.getRegularTypeface
                        (context))
                .build();
        int margin = BASE_HORIZONTAL_MARGIN + titleLayout.getLayout().getHeight() + PADDING;

        BitmapUtils.TextLayoutInfo locationLayout = new BitmapUtils.LayoutBuilder()
                .drawText(location.toUpperCase())
                .usingTextPaint(locationTextPaint)
                .onCanvas(canvas)
                .pinnedToCorner(BitmapUtils.ImageCorner.LowerLeft)
                .withHorizontalMargin(BASE_HORIZONTAL_MARGIN)
                .withVerticalMargin(margin)
                .fitToWidth(titleLayout.getRectPosition().width())
                .withAlignment(Layout.Alignment.ALIGN_NORMAL).build();

        BitmapUtils.drawTextOnCanvas(canvas, locationLayout);
    }
}
