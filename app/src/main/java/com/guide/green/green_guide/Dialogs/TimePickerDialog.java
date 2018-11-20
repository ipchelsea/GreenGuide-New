package com.guide.green.green_guide.Dialogs;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import com.guide.green.green_guide.R;

/**
 * Show a dialog which allows a user to select cancel or to click outside of the dialog to cancel.
 * The dialog shows a progress bar. This bar initially shown an indefinite status but if the
 * progress is known, this class offers a way to set the progress.
 */
public class TimePickerDialog extends DialogFragment {
    private OnTimePickedListener mOnTimePickedListener;
    private int mHour, mMinute;

    public interface OnTimePickedListener {
        void onTimePicked(int hour, int minute);
        void onCancel();
    }

    public void setOnTimePickedListener(OnTimePickedListener listener) {
        mOnTimePickedListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstState) {
        View layout = inflater.inflate(R.layout.dialog_time_picker, container);
        TimePicker inputText = layout.findViewById(R.id.dialog_time_picker_picker);
        Button selectTime = layout.findViewById(R.id.dialog_time_picker_select);
        Button cancel = layout.findViewById(R.id.dialog_time_picker_cancel);
        inputText.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                mHour = hour;
                mMinute = minute;
            }
        });
        selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnTimePickedListener != null) {
                    mOnTimePickedListener.onTimePicked(mHour, mMinute);
                    mOnTimePickedListener = null;
                    dismiss();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return layout;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mOnTimePickedListener != null) {
            mOnTimePickedListener.onCancel();
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        if (mOnTimePickedListener != null) {
            mOnTimePickedListener.onCancel();
        }
    }
}
