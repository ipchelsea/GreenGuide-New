package com.guide.green.green_guide.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.StaticLayout;
import android.text.TextPaint;

/**
 * Provides utilities for drawing shapes & figures and for converting graphical units.
 */
public class Drawing {
    /**
     * Converts scale-independent pixels to pixles.
     *
     * @param ctx   context
     * @param sp    scale-independent pixels
     * @return      pixels
     */
    public static float convertSpToPx(@NonNull Context ctx, float sp) {
        return sp * (float) ctx.getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * Converts pixels to scale-independent pixels.
     *
     * @param ctx   context
     * @param px    pixels
     * @return      scale-independent pixels
     */
    public static float convertPxToSp(@NonNull Context ctx, float px) {
        return px / (float) ctx.getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * Converts device independent pixels to pixels.
     *
     * @param ctx   context
     * @param dp    device independent pixels
     * @return      pixels
     */
    public static float convertDpToPx(@NonNull Context ctx, float dp) {
        return dp * (float) ctx.getResources().getDisplayMetrics().density;
    }

    /**
     * Converts pixels to device independent pixels.
     *
     * @param ctx   context
     * @param px    pixels
     * @return      device independent pixles
     */
    public static float convertPxToDp(@NonNull Context ctx, float px) {
        return px / ctx.getResources().getDisplayMetrics().density;
    }

    /**
     * Draws the lines needed to create a 5-pointed star. If the star where enclosed in a exactly
     * fitting square, the top left of the square whould be ({@code x}, {@code y}). If the star were
     * enclosed in a exactly fitting circle, the diameter of the circle would be {@code d}.
     *
     * @param x left most of the star
     * @param y top most of the star
     * @param d diameter of the smallest circle which would fit the star.
     * @return  path to create the
     */
    public static Path getStarPath(float x, float y, float d) {
        float r = d / 2; x += r; y += r;

        float rB = r / 2;
        final int dblNumOfVertices = 10;
        double quarterTurn = - Math.PI / 2;
        double t = quarterTurn;

        Path path = new Path();
        path.moveTo( (float) Math.cos(t) * r + x, (float) Math.sin(t) * r + y);

        for (int i = 1; i < dblNumOfVertices; i++) {
            t = 2 * i * Math.PI / dblNumOfVertices + quarterTurn;
            if (i % 2 == 0)
                path.lineTo((float) Math.cos(t) * r + x, (float) Math.sin(t) * r + y);
            else
                path.lineTo((float) Math.cos(t) * rB + x, (float) Math.sin(t) * rB + y);
        }

        path.close();
        return path;
    }

    /**
     * Draws rating stars. These stars are drawn in the provided canvas object. They do not get
     * stretched out and sized & centered in the specified rectangle drawn by {@code x}, {@code y},
     * {@code w}, {@code h}.
     *
     * @param x the left most
     * @param y the top most
     * @param w the width of the box to draw the stars in
     * @param h the height of the box to draw the stars in
     * @param starsCount    the number of stars
     * @param ratio a number between 0 and 1 inclusive of both which is the shaded part
     * @param shadedColor   color of the shaded percentage of stars
     * @param unshadedColor color of the unshaded percentage of stars. (1 - {@code ratio})
     * @param cv    the non-null canvas to draw on
     */
    public static void drawStars(float x, float y, float w, float h, int starsCount, float ratio,
                                 @ColorInt int shadedColor, @ColorInt int unshadedColor, Canvas cv)
    {
        float starDiameter = Math.min(w / starsCount, h);
        float marginTop = (h - starDiameter) / 2;
        float marginLeft = (w - starDiameter * starsCount) / 2;

        int partiallyShadedStar = (int)(ratio * starsCount);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(null);
        paint.setColor(shadedColor);

        for (int i = 0; i < starsCount; i++) {
            if (i == partiallyShadedStar) {
                float amountToColor = ratio * starsCount - i;

                paint.setShader(new LinearGradient(i * starDiameter + marginLeft, marginTop + starDiameter / 2,
                        i * starDiameter + marginLeft + starDiameter, marginTop + starDiameter / 2,
                        new int[] {shadedColor, unshadedColor},
                        new float[] {amountToColor, amountToColor},
                        Shader.TileMode.CLAMP));

                cv.drawPath(getStarPath(i * starDiameter + marginLeft, marginTop, starDiameter), paint);

                paint.setShader(null);
                paint.setColor(unshadedColor);
            } else {
                cv.drawPath(getStarPath(i * starDiameter + marginLeft, marginTop, starDiameter), paint);
            }
        }
    }

    /**
     * Draws a side-ways bar graph where the x-axis is vertical. The bars of the graph extend
     * towards the right. This bar graph can not represent negative values.
     *
     * @param dataX a 1D list of x values. Should be the same length as {@code dataY}
     * @param dataY a 1D list of y values. Should be the same length as {@code dataX}
     * @param dataStartIndex lowest index with a non-zero value in {@code dataY}
     * @param dataLastIndex highest index with a non-zero value in {@code dataY}
     * @param minWidth the desired width, in px, of the returned bitmap. (Might be larger)
     * @param textSizePx the size of the {@code dataX} text
     * @param barColor the background color of the returned bitmap
     * @param textColor the color of the text
     * @param backColor background color of the returned bmp
     * @param afterTextMargin The space, in px, between the {@code dataX} text & bar in the graph
     * @param minBarHeight the desired height of the bars in px. (Might be larger)
     * @param barMargin the distance, in px, between the bars.
     * @return a bitmap with the bar graph drawn on it
     */
    public static Bitmap createBarGraph(@NonNull String[] dataX, @NonNull int[] dataY,
                                        int dataStartIndex, int dataLastIndex, float minWidth,
                                        float textSizePx, @ColorInt int barColor,
                                        @ColorInt int textColor, @ColorInt int backColor,
                                        int afterTextMargin, int minBarHeight, int barMargin)
    {
        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setColor(barColor);
        paint.setStyle(Paint.Style.FILL);

        TextPaint tp = new TextPaint();
        tp.setAntiAlias(true);
        tp.setTextSize(textSizePx);
        tp.setColor(textColor);

        float textHeight = tp.descent() - tp.ascent();
        float maxTextWidth = 0;
        int maxBarData = 0;

        for (int i = dataStartIndex; i <= dataLastIndex; i++) {
            if (dataY[i] > maxBarData) { maxBarData = dataY[i]; }
            float w = tp.measureText(dataX[i]);
            if (w > maxTextWidth) { maxTextWidth = w; }
        }

        float barLeftMost = maxTextWidth + afterTextMargin;
        float width = Math.max(minWidth, barLeftMost);
        float barHeight = Math.max(textHeight, minBarHeight);
        float maxBarWidth = width - barLeftMost;
        float height = (barHeight + barMargin) * (dataLastIndex - dataStartIndex + 1) - barMargin;

        Bitmap bmp = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bmp);
        cv.drawColor(backColor);

        float textOffsetTop = (barHeight - textHeight) / 2;
        for (int i = dataStartIndex; i <= dataLastIndex; i++) {
            cv.save();
            cv.translate(0, textOffsetTop + (i - dataStartIndex) * (barHeight + barMargin));
            StaticLayout sLayout;
            if (Build.VERSION.SDK_INT >= 28) {
                sLayout = StaticLayout.Builder
                            .obtain(dataX[i], 0, dataX[i].length(), tp, (int) maxTextWidth).build();
            } else {
                sLayout = new StaticLayout(dataX[i], tp, (int) maxTextWidth,
                    StaticLayout.Alignment.ALIGN_NORMAL, 0, 0, false);
            }
            sLayout.draw(cv);
            cv.restore();
            float bX = barLeftMost;
            float bY = (i - dataStartIndex) * (barHeight + barMargin);
            float bW = Math.max((maxBarWidth * dataY[i]) / maxBarData, 1);
            float bH = barHeight;
            cv.drawRect(bX, bY, bX + bW, bY + bH, paint);
        }

        return bmp;
    }

    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(drawableId);
        } else {
            return context.getResources().getDrawable(drawableId);
        }
    }
}