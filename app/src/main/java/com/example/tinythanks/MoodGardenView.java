package com.example.tinythanks;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Figma'daki "Your Mood Garden" görünümünü çizen custom view.
 * Her Flower: petalColor, centerColor, xPercent, size, heightOffset içeriyor.
 */
public class MoodGardenView extends View {

    private static class Flower {
        int petalColor;
        int centerColor;
        float xPercent;      // 0f–1f arası, genişlik yüzdesi
        float size;          // ölçek (0.8–1.2)
        float heightOffset;  // dikey oynamalar
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Flower> flowers = new ArrayList<>();

    public MoodGardenView(Context context) {
        super(context);
    }

    public MoodGardenView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MoodGardenView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /** Dışarıdan hazır Flower listesi verebiliriz (gerekirse). */
    public void setFlowers(List<Flower> newFlowers) {
        flowers.clear();
        if (newFlowers != null) {
            flowers.addAll(newFlowers);
        }
        invalidate();
    }

    /**
     * Gratitude entry listesini alıp bahçe yaratır.
     * entries: en yeni ilk eleman olacak şekilde gelirse hoş olur.
     */
    public void setEntries(List<GratitudeEntry> entries) {
        flowers.clear();
        if (entries == null || entries.isEmpty()) {
            invalidate();
            return;
        }

        int max = Math.min(entries.size(), 7); // en fazla 7 çiçek
        // Eşit aralıklı x yüzdeleri (soldan sağa)
        float[] xPercents = {0.12f, 0.26f, 0.40f, 0.54f, 0.68f, 0.82f, 0.90f};
        float[] sizes     = {0.9f, 1.05f, 0.85f, 1.1f, 0.95f, 1.0f, 0.9f};
        float[] offsets   = {0f, -5f, 3f, -3f, 4f, -2f, 1f};

        for (int i = 0; i < max; i++) {
            GratitudeEntry e = entries.get(i);
            int mood = (int) e.getMood();

            Flower f = new Flower();
            int[] colors = getColorsForMood(mood);
            f.petalColor = colors[0];
            f.centerColor = colors[1];
            f.xPercent = xPercents[i];
            f.size = sizes[i];
            f.heightOffset = offsets[i];

            flowers.add(f);
        }

        invalidate();
    }

    // Mood -> {petalColor, centerColor}
    private int[] getColorsForMood(int mood) {
        // Figma'ya yakın palette
        switch (mood) {
            case 1: // Peaceful
                return new int[]{
                        Color.parseColor("#D4F4FF"), // petal
                        Color.parseColor("#FFF4A3")  // center
                };
            case 2: // Joyful
                return new int[]{
                        Color.parseColor("#FFFACD"),
                        Color.parseColor("#FFD4E5")
                };
            case 3: // Energetic
                return new int[]{
                        Color.parseColor("#D4F4FF"),
                        Color.parseColor("#FFE0B5")
                };
            case 4: // Creative
                return new int[]{
                        Color.parseColor("#E5D4FF"),
                        Color.parseColor("#FFE0B5")
                };
            case 5: // Loved
            default:
                return new int[]{
                        Color.parseColor("#FFD4E5"),
                        Color.parseColor("#FFF4A3")
                };
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        if (w == 0 || h == 0) return;

        // 1) ÇİMEN / YER
        drawGround(canvas, w, h);

        // 2) ÇİÇEKLER
        for (Flower f : flowers) {
            drawFlower(canvas, f, w, h);
        }
    }

    private void drawGround(Canvas canvas, float w, float h) {
        float centerY = h * 0.8f;

        RectF oval1 = new RectF(
                w * 0.05f,
                centerY - h * 0.04f,
                w * 0.95f,
                centerY + h * 0.04f
        );
        RectF oval2 = new RectF(
                w * 0.08f,
                centerY - h * 0.03f,
                w * 0.92f,
                centerY + h * 0.03f
        );

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#D5EDD5"));
        paint.setAlpha((int) (0.6f * 255));
        canvas.drawOval(oval1, paint);

        paint.setColor(Color.parseColor("#C5E8C5"));
        paint.setAlpha((int) (0.7f * 255));
        canvas.drawOval(oval2, paint);

        // Küçük çimenler
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.parseColor("#C5E8C5"));
        paint.setAlpha(255);

        Path p = new Path();

        float baseY = centerY - h * 0.02f;
        float dx = w / 6f;

        for (int i = 1; i <= 4; i++) {
            float x = dx * i;
            p.reset();
            p.moveTo(x, baseY + h * 0.02f);
            p.quadTo(x + w * 0.01f, baseY - h * 0.05f, x, baseY - h * 0.10f);
            canvas.drawPath(p, paint);
        }
    }

    // Flower componentini Canvas ile çizen fonksiyon
    private void drawFlower(Canvas canvas, Flower f, float w, float h) {
        // Orijinal viewBox: 0..340 x 0..100, ama Flower lokali 0..60 civarı
        float groundCenterY = h * 0.78f;

        float unit = h / 100f; // 1 birim = h/100
        float scale = unit * f.size;

        float cx = f.xPercent * w;
        float bottomStemY = groundCenterY - 5 * unit + f.heightOffset;

        // Flower local koordinatlarını: 0..60, alt = 55
        float translateY = bottomStemY - 55f * scale;

        canvas.save();
        canvas.translate(cx, translateY);
        canvas.scale(scale, scale);

        int stemColor = Color.parseColor("#C5E8C5");

        Paint p = paint;
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(2.5f);
        p.setColor(stemColor);

        // Stem
        canvas.drawLine(0, 20, 0, 55, p);

        // Leaves
        p.setStyle(Paint.Style.FILL);
        p.setColor(stemColor);

        // left leaf
        canvas.save();
        canvas.rotate(-25, -8, 40);
        RectF leftLeaf = new RectF(-8 - 5, 40 - 9, -8 + 5, 40 + 9);
        canvas.drawOval(leftLeaf, p);
        canvas.restore();

        // right leaf
        canvas.save();
        canvas.rotate(25, 8, 35);
        RectF rightLeaf = new RectF(8 - 5, 35 - 9, 8 + 5, 35 + 9);
        canvas.drawOval(rightLeaf, p);
        canvas.restore();

        // Petals
        p.setColor(f.petalColor);

        // top petal
        RectF petalTop = new RectF(0 - 6, 6 - 9, 0 + 6, 6 + 9);
        canvas.drawOval(petalTop, p);

        // top-right
        canvas.save();
        canvas.rotate(72, 8.5f, 11);
        RectF petalTR = new RectF(8.5f - 6, 11 - 9, 8.5f + 6, 11 + 9);
        canvas.drawOval(petalTR, p);
        canvas.restore();

        // bottom-right
        canvas.save();
        canvas.rotate(144, 5.5f, 22);
        RectF petalBR = new RectF(5.5f - 6, 22 - 9, 5.5f + 6, 22 + 9);
        canvas.drawOval(petalBR, p);
        canvas.restore();

        // bottom-left
        canvas.save();
        canvas.rotate(-144, -5.5f, 22);
        RectF petalBL = new RectF(-5.5f - 6, 22 - 9, -5.5f + 6, 22 + 9);
        canvas.drawOval(petalBL, p);
        canvas.restore();

        // top-left
        canvas.save();
        canvas.rotate(-72, -8.5f, 11);
        RectF petalTL = new RectF(-8.5f - 6, 11 - 9, -8.5f + 6, 11 + 9);
        canvas.drawOval(petalTL, p);
        canvas.restore();

        // Center
        p.setColor(f.centerColor);
        canvas.drawCircle(0, 15, 7, p);

        canvas.restore();
    }
}
