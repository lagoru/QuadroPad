package com.lagoru.quadromini;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class DualJoystickView extends LinearLayout {
    private Button mOptionsButton;
    private JoystickView stickL;
    private JoystickView stickR;

    private MenuOpenListener mMenuOpenListener;
    private View pad;

    public DualJoystickView(Context context) {
        super(context);
        initDualJoystickView(context);
    }

    public DualJoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDualJoystickView(context);
    }

    private void initDualJoystickView(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        stickL = new JoystickView(context);
        mOptionsButton = new Button(context);
        mOptionsButton.setText(R.string.options_text);
        mOptionsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMenuOpenListener != null){
                    mMenuOpenListener.menuOpened();
                }
            }
        });
        stickR = new JoystickView(context);
        pad = new View(getContext());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        removeView(stickL);
        removeView(mOptionsButton);
        removeView(stickR);

        //float padW = getMeasuredWidth() - (getMeasuredHeight() * 2);
        //int joyWidth = (int) ((getMeasuredWidth() - padW) / 2);
        //TODO moze dodac obluge nizszych api
        int side = (getMeasuredWidth()-mOptionsButton.getMinWidth())/2;
        //int side = (getMeasuredHeight() < getMeasuredWidth() / 2 ? getMeasuredHeight() : getMeasuredWidth() / 2);

        LayoutParams joyLParams = new LayoutParams(side, side);
        //joyLParams.gravity = Gravity.CENTER;
        stickL.setLayoutParams(joyLParams);
        stickR.setLayoutParams(joyLParams);

        stickL.TAG = "L";
        stickR.TAG = "R";
        stickL.setPointerId(JoystickView.INVALID_POINTER_ID);
        stickR.setPointerId(JoystickView.INVALID_POINTER_ID);

        addView(stickL);
        addView(mOptionsButton);
        addView(stickR);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        stickR.setTouchOffset(stickR.getLeft(), stickR.getTop());
    }

    public void setAutoReturnToCenter(boolean left, boolean right) {
        stickL.setAutoReturnToCenter(left);
        stickR.setAutoReturnToCenter(right);
    }

    public void setOnJostickMovedListener(JoystickMovedListener left, JoystickMovedListener right) {
        stickL.setOnJostickMovedListener(left);
        stickR.setOnJostickMovedListener(right);
    }

    public void setOnJostickClickedListener(JoystickClickedListener left, JoystickClickedListener right) {
        stickL.setOnJostickClickedListener(left);
        stickR.setOnJostickClickedListener(right);
    }

    public void setYAxisInverted(boolean leftYAxisInverted, boolean rightYAxisInverted) {
        stickL.setYAxisInverted(leftYAxisInverted);
        stickL.setYAxisInverted(rightYAxisInverted);
    }

    public void setMovementConstraint(int movementConstraint) {
        stickL.setMovementConstraint(movementConstraint);
        stickR.setMovementConstraint(movementConstraint);
    }

    public void setMovementRange(float movementRangeLeft, float movementRangeRight) {
        stickL.setMovementRange(movementRangeLeft);
        stickR.setMovementRange(movementRangeRight);
    }

    public void setMoveResolution(float leftMoveResolution, float rightMoveResolution) {
        stickL.setMoveResolution(leftMoveResolution);
        stickR.setMoveResolution(rightMoveResolution);
    }

    public void setUserCoordinateSystem(int leftCoordinateSystem, int rightCoordinateSystem) {
        stickL.setUserCoordinateSystem(leftCoordinateSystem);
        stickR.setUserCoordinateSystem(rightCoordinateSystem);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean l = stickL.dispatchTouchEvent(ev);
        boolean button = mOptionsButton.dispatchTouchEvent(ev);
        boolean r = stickR.dispatchTouchEvent(ev);
        return l || r || button;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean l = stickL.onTouchEvent(ev);
        boolean button = mOptionsButton.onTouchEvent(ev);
        boolean r = stickR.onTouchEvent(ev);
        return l || r || button;
    }

    public void setMenuOpenListener(MenuOpenListener listener){
        mMenuOpenListener = listener;
    }
}