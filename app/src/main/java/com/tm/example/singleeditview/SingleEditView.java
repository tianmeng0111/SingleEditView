package com.tm.example.singleeditview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tian on 2016/7/27.
 */
public class SingleEditView extends View implements View.OnKeyListener{

    //输入密码样式
    private static final int PWD_STYLE_NORMAL = 0;
    private static final int PWD_STYLE_CIRCLE = 1;
    private static final int PWD_STYLE_STAR = 2;
    private static final int PWD_STYLE_POUND = 3;
    private int pwdStyle = PWD_STYLE_NORMAL;
    //文字大小，不包括样式大小
    private static final float DEFAULT_TEXT_SIZE = 56;
    private float textSize = DEFAULT_TEXT_SIZE;
    //文字或样式颜色
    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#3F51B5");
    private int textColor = DEFAULT_TEXT_COLOR;
    //文字图片样式
    private int textIcon;
    private Bitmap bitmapTextIcon;
    //线宽
    private static final int DEFAULT_BORDER_WIDTH = 2;
    private int borderWidth = DEFAULT_BORDER_WIDTH;
    //边框圆角半径
    private static final int DEFAULT_BORDER_RADIUS = 6;
    private int borderRadius = DEFAULT_BORDER_RADIUS;
    //线颜色
    private static final int DEFAULT_BORDER_COLOR = Color.parseColor("#3F51B5");
    private int borderColor = DEFAULT_BORDER_COLOR;
    //输入文字个数
    private static final int DEFAULT_EDIT_COUNT = 6;
    private int editCount = DEFAULT_EDIT_COUNT;

    private int singleSize = 30;

    private Paint paintBorder;
    private Paint paintText;

    private int[] padding = {0, 0, 0, 0};

    private int width;
    private int height;

    private RectF rectBorder;

    private InputMethodManager inputMethodManager;

    private List<Integer> listNum = new ArrayList<>();

    public SingleEditView(Context context) {
        super(context);
        init(context, null);
    }

    public SingleEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SingleEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SingleEditView);
            textColor = ta.getColor(R.styleable.SingleEditView_text_color, DEFAULT_TEXT_COLOR);
            textSize = ta.getDimension(R.styleable.SingleEditView_text_size, DEFAULT_TEXT_SIZE);
            pwdStyle = ta.getInt(R.styleable.SingleEditView_pwd_style, PWD_STYLE_NORMAL);
            textIcon = ta.getResourceId(R.styleable.SingleEditView_text_icon, 0);
            bitmapTextIcon = BitmapFactory.decodeResource(context.getResources(), textIcon);

            borderWidth = (int) ta.getDimension(R.styleable.SingleEditView_border_width, DEFAULT_BORDER_WIDTH);
            borderRadius = (int) ta.getDimension(R.styleable.SingleEditView_border_radius, DEFAULT_BORDER_RADIUS);
            borderColor = ta.getColor(R.styleable.SingleEditView_border_color, DEFAULT_BORDER_COLOR);
            editCount = ta.getInt(R.styleable.SingleEditView_edit_count, DEFAULT_EDIT_COUNT);
            ta.recycle();
        }

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setStrokeWidth(borderWidth);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setAntiAlias(true);
        paintBorder.setTextAlign(Paint.Align.CENTER);
        paintBorder.setColor(borderColor);


        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setColor(textColor);
        paintText.setTextSize(textSize);

        rectBorder = new RectF();

        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        /**
         * 键盘按键监听
         */
        this.setOnKeyListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        padding[0] = getPaddingLeft();
        padding[1] = getPaddingRight();
        padding[2] = getPaddingRight();
        padding[3] = getPaddingBottom();

        for (int i = 0; i < 4; i++) {
            if (padding[i] <= 0) {
                padding[i] = borderWidth;
            }
        }

        int paddingWidth = padding[0] +  padding[2];
        int paddingHeight = padding[1] + padding[3];

        if (getMeasure(widthMeasureSpec) <= 0) {
            if (getMeasure(heightMeasureSpec) <= 0) {//宽高都没指定 WrapContent
                width = singleSize * editCount + paddingWidth;
                height = singleSize + paddingHeight;
            } else {//高度指定了
                singleSize = height - paddingHeight;
                width = singleSize * editCount + paddingWidth;
            }
        } else {//宽度指定了
            if (getMeasure(heightMeasureSpec) <= 0) {//高度没指定
                singleSize = (width - paddingWidth) / editCount;
                height = singleSize + paddingHeight;
            } else {//宽高都指定了
                if (width - paddingWidth < (height - paddingHeight) * singleSize) {//宽度比高的6倍小
                    singleSize = (width - paddingWidth) / editCount;
                    height = singleSize + paddingHeight;
                } else {

                }
            }
        }
//        Logger.e("width-->>" + width + ",height-->>" + height);
        setMeasuredDimension(width, height);
        rectBorder.set(padding[0], padding[1], width - padding[2], height - padding[3]);
    }

    private int getMeasure(int measureSpec) {
        if (MeasureSpec.getMode(measureSpec) == MeasureSpec.AT_MOST) {
            return -1;
        }
        return MeasureSpec.getSize(measureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawRoundRect(rectBorder, borderRadius, borderRadius, paintBorder);//绘制边框
        for (int i = 1; i < editCount; i++){//绘制分割线
            final int x = i * singleSize;
            canvas.drawLine(padding[0] + x, padding[1], padding[0] + x, height - padding[3], paintBorder);
        }
        Paint.FontMetricsInt fontMetricsInt = paintText.getFontMetricsInt();
        float baseline = (padding[1] + height - padding[3] - fontMetricsInt.top - fontMetricsInt.bottom) / 2;
        if (pwdStyle == PWD_STYLE_NORMAL) {
            for (int i = 0; i < listNum.size(); i++) {
                final int x = i * singleSize;
                canvas.drawText(listNum.get(i).toString(), padding[0] + singleSize / 2 + x, baseline, paintText);
            }
        } else if (pwdStyle == PWD_STYLE_CIRCLE) {
            for (int i = 0; i < listNum.size(); i++) {
                final int x = i * singleSize;
                canvas.drawCircle(padding[0] + singleSize / 2 + x, padding[1] + (height - padding[3] - padding[1]) / 2, singleSize / 6, paintText);
            }
        } else if (pwdStyle == PWD_STYLE_STAR) {
            for (int i = 0; i < listNum.size(); i++) {
                final int x = i * singleSize;
                canvas.drawText("*", padding[0] + singleSize / 2 + x, baseline, paintText);
            }
        } else if (pwdStyle == PWD_STYLE_POUND) {
            int rectPadding = singleSize / 3;
            for (int i = 0; i < listNum.size(); i++) {
                final int x = i * singleSize;
                Rect rect = new Rect();
                rect.set(padding[0] + x + rectPadding, (int)rectBorder.top + rectPadding, padding[0] + x + singleSize - rectPadding, (int)rectBorder.bottom - rectPadding);
                canvas.drawBitmap(bitmapTextIcon, null, rect, paintText);
            }
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//        Logger.e("gainFocus-->>" + gainFocus);
        if (gainFocus) {
            showSoftInput();
        } else {
            hideSoftInput();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
//        Logger.e("hasWindowFocus-->>" + hasWindowFocus);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;//数字键盘
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;//键盘中功能键是done或完成
//        return super.onCreateInputConnection(outAttrs);
        return new MyInputConnection(this, false);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Logger.e("====onKey");
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {//点击0~9数字键
                listNum.add(keyCode - 7);
                invalidate();
            } else if (keyCode == KeyEvent.KEYCODE_DEL) {
                listNum.remove(listNum.size() - 1);
                invalidate();
            } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                hideSoftInput();
            }
            if (listNum.size() >= editCount) {
                hideSoftInput();
            }
        }
        return false;
    }

    private void hideSoftInput() {
        inputMethodManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }
    private void showSoftInput() {
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
    }


    class MyInputConnection extends BaseInputConnection {
        public MyInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return true;
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength==1 && afterLength==0){
                return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    /**
     * 是否完成输入
     * @return
     */
    public boolean isEditDown() {
        return listNum.size() >= editCount;
    }

    /**
     * 返回输入的字符串
     * @return
     */
    public String getEditText() {
        StringBuilder sb = new StringBuilder();
        for (int i : listNum) {
            sb.append(i);
        }
        return sb.toString();
    }

    /**
     * 清空输入
     */
    public void clearEdit() {
        listNum.clear();
        invalidate();
    }

    public void setPwdStyle(int pwdStyle) {
        this.pwdStyle = pwdStyle;
    }

}
