package com.freddieptf.meh.imagecompressor.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.freddieptf.meh.imagecompressor.R;

/**
 * Created by freddieptf on 08/08/16.
 */
public class TaskView extends LinearLayout implements View.OnClickListener {

    LinearLayout taskView, taskBody;
    TextView taskTitle;
    RadioButton taskRadioButton;
    private static final String TAG = "TaskView";
    OnTaskCheck onTaskCheck;
    boolean checked = false;
    private String VIEW_SAVE_STATE = "view_save_state";

    public TaskView(Context context) {
        this(context, null);
    }

    public TaskView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.task_layout, this, true);
        taskView = (LinearLayout) findViewById(R.id.taskView);
        taskBody = (LinearLayout) findViewById(R.id.taskBody);
        taskTitle = (TextView) findViewById(R.id.taskTitle);
        taskRadioButton = (RadioButton) findViewById(R.id.taskCheck);

        TypedArray typedArray = context.getResources().obtainAttributes(attrs, R.styleable.TaskView);
        CharSequence title = typedArray.getString(R.styleable.TaskView_taskTitle);
        typedArray.recycle();
        if(title != null && !TextUtils.isEmpty(title)) taskTitle.setText(title);

        taskRadioButton.setOnClickListener(this);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if(taskBody == null) super.addView(child, index, params);
        else taskBody.addView(child, index, params);
    }

    public boolean isChecked(){
        return taskRadioButton.isChecked();
    }

    public void toggle(){
        checked = !checked;
        expand(checked);
        taskRadioButton.setChecked(checked);
    }

    private void expand(boolean checked){
        taskBody.setVisibility(checked ? VISIBLE : GONE);
    }

    @Override
    public void onClick(View v) {
        if(onTaskCheck != null) onTaskCheck.onTaskCheck(String.valueOf(getTag()));
    }

    public void setOnTaskCheckedListener(OnTaskCheck onTaskCheck){
       this.onTaskCheck = onTaskCheck;
    }

    public interface OnTaskCheck{
        void onTaskCheck(String tag);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable p = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable(VIEW_SAVE_STATE, p);
        bundle.putBoolean(getTag() + "_checked", taskRadioButton.isChecked());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(bundle.getParcelable(VIEW_SAVE_STATE));
        if(bundle.getBoolean(getTag() + "_checked")) expand(true);
    }

}
