package yesuaini.chinoisinteractif.hsk.views;

import java.util.ArrayList;

import yesuaini.chinoisinteractif.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ExamResultPieChart extends View {

	private static final String LOG_TAG = ExamResultPieChart.class.getName();
	private static String[] PIE_COLORS = {"#038C17", "#BF1A0B"};
	private RectF rectangle;
	private ArrayList<Float> percentages = new ArrayList<Float>();
	private Paint paintPieFill;
	private Paint paintPieBorder;

	public ExamResultPieChart(Context context, AttributeSet attrs) {
		super(context, attrs);

		// used for paint circle
		paintPieFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintPieFill.setStyle(Paint.Style.FILL);
		
		// used for paint border
		paintPieBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintPieBorder.setStyle(Paint.Style.STROKE);
		paintPieBorder.setStrokeWidth(fnGetRealPxFromDp(3));
		paintPieBorder.setColor(Color.WHITE);
		Log.i(LOG_TAG, "PieChart init done");

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		float fStartAngle = 0f;
		for (int i = 0; i < percentages.size(); i++) {

			// check whether the data size larger than color list size
			if (i >= 2) {
				paintPieFill.setColor(Color.parseColor(PIE_COLORS[i % 2]));
			} else {
				paintPieFill.setColor(Color.parseColor(PIE_COLORS[i]));
			}

			float fEndAngle = percentages.get(i);

			// convert percentage to angle
			fEndAngle = fEndAngle / 100 * 360;

			canvas.drawArc(rectangle, fStartAngle, fEndAngle, true, paintPieFill);

			fStartAngle = fStartAngle + fEndAngle;
		}

		Log.i(LOG_TAG, "onDraw done");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// get screen size
		int iDisplayWidth = MeasureSpec.getSize(widthMeasureSpec);
		int iDisplayHeight = MeasureSpec.getSize(heightMeasureSpec);

		if (iDisplayWidth > iDisplayHeight) {
			iDisplayWidth = iDisplayHeight;
		}

		//determine the rectangle size
		int iCenterWidth = iDisplayWidth / 2;
		int iR = iCenterWidth - (int) fnGetRealPxFromDp(40);
		if (rectangle == null) {
			rectangle = new RectF(iCenterWidth - iR, // top
					iCenterWidth - iR, // left
					iCenterWidth + iR, // rights
					iCenterWidth + iR); // bottom
		}
		setMeasuredDimension(iDisplayWidth, iDisplayWidth);
	}

	private float fnGetRealPxFromDp(float fDp) {
		float fDensity = this.getResources().getDisplayMetrics().density;
		return (fDensity != 1.0f) ? fDensity * fDp : fDp;
	}
	
	public void setAdapter(ArrayList<Float> percentages) throws DisproportionatePieChartSumException {
		this.percentages = percentages;
		float fSum = 0;
		for (int i = 0; i < percentages.size(); i++) {
			fSum += percentages.get(i);
		}
		if (fSum != 100) {
			throw new DisproportionatePieChartSumException();
		}
	}
	
	public class DisproportionatePieChartSumException extends Exception {
		private static final long serialVersionUID = 1L;

		@Override
		public String getMessage() {
			return (String) ExamResultPieChart.this.getResources().getText(R.string.piechart_error_msg);
		}
	}

}
