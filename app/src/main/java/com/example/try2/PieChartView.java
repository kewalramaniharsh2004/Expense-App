package com.example.try2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PieChartView extends View {

    private Paint paint;
    private float totalIncome, totalExpense;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void setData(float totalIncome, float totalExpense) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) * 0.8f;

        float total = totalIncome + totalExpense;
        float incomeAngle = 360 * (totalIncome / total);
        float expenseAngle = 360 * (totalExpense / total);

        // Draw income slice
        paint.setColor(Color.GREEN);
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
                0, incomeAngle, true, paint);

        // Draw expense slice
        paint.setColor(Color.RED);
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
                incomeAngle, expenseAngle, true, paint);
    }
}
