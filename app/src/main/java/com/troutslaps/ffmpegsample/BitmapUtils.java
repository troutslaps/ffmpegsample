package com.troutslaps.ffmpegsample;

/**
 * Created by duchess on 20/04/2017.
 */

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

/**
 * Created by maicervantes on 05/04/2017.
 * This is a collection of methods and convenience classes to draw text and gradients over an
 * image. Text elements can be laid out by specifying a corner from which margin measurements
 * can be done. StaticLayout is used to determine the sizes of the text elements based on font
 * size and the text to be drawn.
 */


public class BitmapUtils {



    // this is a convenience class that contains a StaticLayout object and rectPosition object
    // that describe the position and size of the text to be drawn in the Canvas. StaticLayout
    // automatically wraps text to fit a provided maximum width
    public static class TextLayoutInfo {
        private StaticLayout layout;
        private Rect rectPosition;

        public TextLayoutInfo(StaticLayout layout, Rect rectPosition) {
            this.layout = layout;
            this.rectPosition = rectPosition;
        }

        public StaticLayout getLayout() {
            return layout;
        }

        public void setLayout(StaticLayout layout) {
            this.layout = layout;
        }

        public Rect getRectPosition() {
            return rectPosition;
        }

        public void setRectPosition(Rect rectPosition) {
            this.rectPosition = rectPosition;
        }

    }

    // This is a convenience builder class to build TextLayoutInfo objects. To use, one must
    // provide a corner from which to pivot, and the margins from this corner.
    public static class LayoutBuilder {
        private String text;
        private ImageCorner imageCorner;
        private TextPaint textPaint;
        private int fitToWidth;
        private Layout.Alignment alignment;
        private int horizontalMargin;
        private int verticalMargin;
        private Canvas canvas;
        private boolean centered;

        public LayoutBuilder() {
            // do nothing
            this.centered = false;
        }

        public LayoutBuilder drawText(String text) {
            this.text = text;
            return this;
        }

        public LayoutBuilder onCanvas(Canvas canvas) {
            this.canvas = canvas;
            return this;
        }

        public LayoutBuilder pinnedToCorner(ImageCorner imageCorner) {
            this.imageCorner = imageCorner;
            return this;
        }

        public LayoutBuilder withHorizontalMargin(int horizontalMargin) {
            this.horizontalMargin = horizontalMargin;
            return this;
        }

        public LayoutBuilder withVerticalMargin(int verticalMargin) {
            this.verticalMargin = verticalMargin;
            return this;
        }

        public LayoutBuilder usingTextPaint(TextPaint textPaint) {
            this.textPaint = textPaint;
            return this;
        }


        public LayoutBuilder fitToWidth(int fitToWidth) {
            this.fitToWidth = fitToWidth;
            return this;
        }

        public LayoutBuilder withAlignment(Layout.Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public TextLayoutInfo build() {
            StaticLayout layout = new StaticLayout(text, textPaint, fitToWidth, alignment, 1.0f,
                    0.0f,
                    true);
            int posX = 0, posY = 0;
            int height = layout.getHeight();
            int width = layout.getWidth();
            if(centered) {
                posX = ((canvas.getWidth() - layout.getWidth())/2) + horizontalMargin;
                posY = verticalMargin;
            } else {
                Point pivotPoint = imageCorner.getCoordinatesOfCornerForCanvasWithMargin(canvas,
                        horizontalMargin, verticalMargin);
                switch (imageCorner) {
                    case UpperLeft:
                        posX = pivotPoint.x;
                        posY = pivotPoint.y;
                        break;
                    case LowerLeft:
                        posX = pivotPoint.x;
                        posY = pivotPoint.y - height;
                        break;
                    case UpperRight:
                        posX = pivotPoint.x - width;
                        posY = pivotPoint.y;
                        break;
                    case LowerRight:
                        posX = pivotPoint.x - width;
                        posY = pivotPoint.y - height;
                        break;
                }
            }
            return new TextLayoutInfo(layout, new Rect(posX, posY, posX + width, posY + height));
        }

        public LayoutBuilder placeOnCenter() {
            this.centered = true;
            return this;
        }
    }

    // an enum to represent a corner in the canvas from which to pivot from
    public enum ImageCorner {
        None(-1, -1),
        UpperLeft(0, 0),
        LowerLeft(0, 1),
        UpperRight(1, 0),
        LowerRight(1, 1);

        private int x;
        private int y;

        ImageCorner(int x, int y) {
            this.x = x;
            this.y = y;
        }
        // convenience method to compute the coordinates of a point from a corner given
        // horizontal and vertical margin values
        public Point getCoordinatesOfCornerForCanvasWithMargin(Canvas canvas, int marginX,
                                                               int marginY) {
            int x = this.x * canvas.getWidth();
            int y = this.y = canvas.getHeight();
            if(marginX > 0) {
                if (x == 0) { // left
                    x += marginX;
                } else {
                    x -= marginX; // right
                }
            }
            if(marginY > 0) {
                if (y == 0) { // top
                    y += marginY;
                } else {
                    y -= marginY; // bottom
                }
            }
            return new Point(x, y);

        }
    }

    // convenience builder class for building TextPaint objects
    public static class PaintBuilder {
        Typeface typeface;
        float textSize;
        int textColor;

        public PaintBuilder() {

        }

        public PaintBuilder withTypeface(Typeface typeface) {
            this.typeface = typeface;
            return this;
        }

        public PaintBuilder withTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public PaintBuilder withTextSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public TextPaint build() {
            TextPaint paint = new TextPaint();
            paint.setAntiAlias(true);
            paint.setTypeface(typeface);
            paint.setTextSize(textSize);
            paint.setColor(textColor);
            return paint;
        }

    }
    // convenience method for drawing gradient specifically from the bottom  of the canvas
    public static void drawGradientFromBottomOfCanvas(Canvas canvas, int startColor, int
            endColor, int gradientHeight) {
        int startX = 0;
        int startY = canvas.getHeight();
        int endX = 0;
        int endY = canvas.getHeight() - gradientHeight;
        drawGradientOnCanvas(canvas, startColor, endColor, startX, startY, endX, endY,
                gradientHeight);
    }


    public static void drawGradientOnCanvas(Canvas canvas, int startColor, int endColor, float
            startX, float startY, float endX, float endY, int gradientHeight) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(startX, startY, endX,
                endY, startColor, endColor, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(0, h - gradientHeight, w, h, paint);

    }

    public static void drawTextOnCanvas(Canvas canvas, TextLayoutInfo layoutInfo) {
        canvas.save();
        canvas.translate(layoutInfo.getRectPosition().left, layoutInfo.getRectPosition()
                .top);
        layoutInfo.getLayout().draw(canvas);
        canvas.restore();
    }
}

