package com.example.tinythanks;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

/**
 * Semi-realistic flower:
 *  - Tear–drop shaped petals with soft gradients
 *  - Colors depend on FlowerState (BUD / PARTIAL / FULL / WILTED)
 *  - Petal count depends on totalEntries
 *  - Small bloom animation + smooth color transition
 *
 * Public API is the same:
 *   setFlowerState(FlowerState)
 *   setTotalEntries(int)
 */
public class FlowerView extends View {

    // Paints
    private final Paint petalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Data
    private int totalEntries = 0;
    private FlowerState flowerState = FlowerState.BUD;
    private int petalCount = 6;

    // Animation state
    private float bloomScale = 1f;
    private int currentPetalColorOuter;
    private int currentPetalColorInner;

    private ValueAnimator bloomAnimator;
    private ValueAnimator colorAnimatorOuter;
    private ValueAnimator colorAnimatorInner;

    // Color palette (outer / inner) – pastel + yarı gerçekçi
    private static final int BUD_OUTER   = Color.parseColor("#FFE3F1");
    private static final int BUD_INNER   = Color.parseColor("#FFB7D8");

    private static final int PART_OUTER  = Color.parseColor("#FFE9D4");
    private static final int PART_INNER  = Color.parseColor("#FF9A4D");

    private static final int FULL_OUTER  = Color.parseColor("#FFD9F0");
    private static final int FULL_INNER  = Color.parseColor("#FF5EA8");

    private static final int WILT_OUTER  = Color.parseColor("#F0F2F5");
    private static final int WILT_INNER  = Color.parseColor("#B0B7C2");

    public FlowerView(Context context) {
        super(context);
        init();
    }

    public FlowerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlowerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        petalPaint.setStyle(Paint.Style.FILL);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(Color.WHITE);

        // initial colors
        currentPetalColorOuter = BUD_OUTER;
        currentPetalColorInner = BUD_INNER;

        updatePetalCount();
    }

    // --------- Public API ----------

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = Math.max(0, totalEntries);
        updatePetalCount();
        startBloomAnimation();
        invalidate();
    }

    public void setFlowerState(FlowerState state) {
        if (state == null) state = FlowerState.BUD;
        if (this.flowerState == state) return;

        this.flowerState = state;

        int[] target = getColorsForState(state);
        startColorAnimation(target[0], target[1]);
        startBloomAnimation();

        // wilted -> hafif soluk
        setAlpha(state == FlowerState.WILTED ? 0.9f : 1f);

        invalidate();
    }

    // --------- Helpers ----------

    private void updatePetalCount() {
        if (totalEntries <= 0) {
            petalCount = 5;
        } else if (totalEntries <= 3) {
            petalCount = 6;
        } else if (totalEntries <= 7) {
            petalCount = 7;
        } else if (totalEntries <= 12) {
            petalCount = 8;
        } else {
            petalCount = 10;
        }
    }

    private int[] getColorsForState(FlowerState state) {
        switch (state) {
            case PARTIAL:
                return new int[]{PART_OUTER, PART_INNER};
            case FULL:
                return new int[]{FULL_OUTER, FULL_INNER};
            case WILTED:
                return new int[]{WILT_OUTER, WILT_INNER};
            case BUD:
            default:
                return new int[]{BUD_OUTER, BUD_INNER};
        }
    }

    private void startBloomAnimation() {
        if (bloomAnimator != null) bloomAnimator.cancel();
        bloomAnimator = ValueAnimator.ofFloat(0.85f, 1f);
        bloomAnimator.setDuration(350);
        bloomAnimator.setInterpolator(new OvershootInterpolator());
        bloomAnimator.addUpdateListener(a -> {
            bloomScale = (float) a.getAnimatedValue();
            invalidate();
        });
        bloomAnimator.start();
    }

    private void startColorAnimation(int outerTarget, int innerTarget) {
        if (colorAnimatorOuter != null) colorAnimatorOuter.cancel();
        if (colorAnimatorInner != null) colorAnimatorInner.cancel();

        colorAnimatorOuter = ValueAnimator.ofObject(
                new ArgbEvaluator(), currentPetalColorOuter, outerTarget);
        colorAnimatorOuter.setDuration(300);
        colorAnimatorOuter.addUpdateListener(a -> {
            currentPetalColorOuter = (int) a.getAnimatedValue();
            invalidate();
        });
        colorAnimatorOuter.start();

        colorAnimatorInner = ValueAnimator.ofObject(
                new ArgbEvaluator(), currentPetalColorInner, innerTarget);
        colorAnimatorInner.setDuration(300);
        colorAnimatorInner.addUpdateListener(a -> {
            currentPetalColorInner = (int) a.getAnimatedValue();
            invalidate();
        });
        colorAnimatorInner.start();
    }

    // --------- Drawing ----------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;

        float size = Math.min(w, h);
        float maxR = size / 2f * bloomScale;

        float centerR = maxR * 0.32f;
        float petalLength = maxR * 0.95f;
        float petalWidth = maxR * 0.38f;

        // wilted ise petaller biraz aşağıda
        float droopOffset = (flowerState == FlowerState.WILTED) ? maxR * 0.10f : 0f;

        float angleStep = (float) (2 * Math.PI / petalCount);

        // Petal path (origin: 0,0; yukarı doğru uzayan damla)
        Path petalPath = new Path();
        petalPath.moveTo(0, 0);
        petalPath.cubicTo(
                -petalWidth, -petalLength * 0.25f,
                -petalWidth * 0.6f, -petalLength * 0.75f,
                0, -petalLength
        );
        petalPath.cubicTo(
                petalWidth * 0.6f, -petalLength * 0.75f,
                petalWidth, -petalLength * 0.25f,
                0, 0
        );
        petalPath.close();

        // her petal için gradient ayarla
        for (int i = 0; i < petalCount; i++) {
            float angle = i * angleStep;

            canvas.save();

            canvas.translate(cx, cy + droopOffset);
            canvas.rotate((float) Math.toDegrees(angle));

            // gradient: iç kısım daha koyu, uç daha açık
            RadialGradient shader = new RadialGradient(
                    0,
                    -petalLength * 0.55f,
                    petalLength,
                    currentPetalColorInner,
                    currentPetalColorOuter,
                    Shader.TileMode.CLAMP
            );
            petalPaint.setShader(shader);

            canvas.drawPath(petalPath, petalPaint);

            canvas.restore();
        }

        // center: soft white, hafif pastel gölge efekti için hafif gri stroke atılabilir (istersen)
        centerPaint.setShader(new RadialGradient(
                cx, cy, centerR * 1.1f,
                0xFFFFFFFF,
                0xFFF7F7F7,
                Shader.TileMode.CLAMP
        ));
        canvas.drawCircle(cx, cy + droopOffset * 0.5f, centerR, centerPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (bloomAnimator != null) bloomAnimator.cancel();
        if (colorAnimatorOuter != null) colorAnimatorOuter.cancel();
        if (colorAnimatorInner != null) colorAnimatorInner.cancel();
    }
}
