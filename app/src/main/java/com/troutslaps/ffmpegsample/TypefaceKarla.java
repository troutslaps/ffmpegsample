package com.troutslaps.ffmpegsample;

import android.content.Context;
import android.graphics.Typeface;


public class TypefaceKarla {

    private static Typeface typefaceRegular;
    private static Typeface typefaceItalic;
    private static Typeface typefaceBoldItalic;
    private static Typeface typefaceBold;

    public static Typeface getRegularTypeface(Context context) {
        if (typefaceRegular == null) {
            typefaceRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Karla-Regular.ttf");
        }
        return typefaceRegular;
    }

    public static Typeface getTypefaceItalic(Context context) {
        if (typefaceItalic == null) {
            typefaceItalic = Typeface.createFromAsset(context.getAssets(), "fonts/Karla-Italic.ttf");
        }
        return typefaceItalic;
    }

    public static Typeface getTypefaceBoldItalic(Context context) {
        if (typefaceBoldItalic == null) {
            typefaceBoldItalic = Typeface.createFromAsset(context.getAssets(), "fonts/Karla-BoldItalic.ttf");
        }
        return typefaceBoldItalic;
    }

    public static Typeface getTypefaceBold(Context context) {
        if (typefaceBold == null) {
            typefaceBold = Typeface.createFromAsset(context.getAssets(), "fonts/Karla-Bold.ttf");
        }
        return typefaceBold;
    }
}