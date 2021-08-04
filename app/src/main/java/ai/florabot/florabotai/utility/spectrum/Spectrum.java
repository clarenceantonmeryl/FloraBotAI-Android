package ai.florabot.florabotai.utility.spectrum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class Spectrum extends View {

    private Paint paint;

    public Spectrum(Context context) {
        super(context);
        initSpectrum();
    }

    public void initSpectrum() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#F101F1"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, 20, paint);
        canvas.save();

        super.onDraw(canvas);
    }
}
