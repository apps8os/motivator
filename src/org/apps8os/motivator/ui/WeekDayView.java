package org.apps8os.motivator.ui;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class WeekDayView extends View {
	
	private Context mContext;
	private String mDayOfWeek;
	private boolean mEvents;
	private int mDrinks;
	private Paint mTextPaint;
	private int mTextColor;
	private float mTextY;
	private float mTextX;
	private Rect mMoodImageRect;
	private Bitmap mMoodImage;
	private Paint mImagePaint;
	private float mImageRatio;
	private Resources mRes;
	

	/**
	 * Custom view for the day in a week.
	 * @param context
	 * @param attrSet
	 */
	
	public WeekDayView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		mContext = context;
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrSet, R.styleable.WeekDayView, 0, 0);
		
		try {
			mDayOfWeek = a.getString(R.styleable.WeekDayView_dayOfWeek);
			mEvents = a.getBoolean(R.styleable.WeekDayView_events, false);
			mDrinks = a.getInt(R.styleable.WeekDayView_drinks, 0);
			mTextColor = a.getColor(R.styleable.WeekDayView_textColor, Color.BLACK);
		} finally {
			a.recycle();
		}
		
		if (mDayOfWeek == null) {
			mDayOfWeek = "NULL";
		}
		
		init();
	}
	
	private void init() {
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setColor(mTextColor);
		mTextPaint.setTextSize(30);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mRes = getResources();
		
		mMoodImage = BitmapFactory.decodeResource(mRes, R.drawable.temp_emoticon);
		mImageRatio = (float) mMoodImage.getWidth() / (float) mMoodImage.getHeight();
		
		mImagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mImagePaint.setFilterBitmap(true);
		mImagePaint.setDither(true);
	}
	
	public void setDay(DayInHistory day) {
		Calendar date = new GregorianCalendar();
		date.setTimeInMillis(day.getDateInMillis());
		switch (date.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			mDayOfWeek = mRes.getString(R.string.monday_short);
			break;
		case Calendar.TUESDAY:
			mDayOfWeek = mRes.getString(R.string.tuesday_short);
			break;
		case Calendar.WEDNESDAY:
			mDayOfWeek = mRes.getString(R.string.wednesday_short);
			break;
		case Calendar.THURSDAY:
			mDayOfWeek = mRes.getString(R.string.thursday_short);
			break;
		case Calendar.FRIDAY:
			mDayOfWeek = mRes.getString(R.string.friday_short);
			break;
		case Calendar.SATURDAY:
			mDayOfWeek = mRes.getString(R.string.saturday_short);
			break;
		case Calendar.SUNDAY:
			mDayOfWeek = mRes.getString(R.string.sunday_short);
			break;
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
		mTextY = 0.2f * h + getPaddingTop();
		mTextX = 0.1f * w + getPaddingLeft();

       int imageX = (int) (getPaddingLeft() + 10 * getResources().getDisplayMetrics().density);
       int imageY = (int) (getPaddingTop() + 50 * getResources().getDisplayMetrics().density);
       int imageXend = (int) (getWidth() - getPaddingRight() - 10 * mRes.getDisplayMetrics().density);
       int imageYend = (int) (imageY + imageXend * mImageRatio);
       mMoodImageRect = new Rect(imageX, imageY, imageXend, imageYend);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawText(mDayOfWeek, getWidth() / 2,  mTextY , mTextPaint);
		
		canvas.drawBitmap(mMoodImage, null, mMoodImageRect, mImagePaint);
	}
	
	

}
