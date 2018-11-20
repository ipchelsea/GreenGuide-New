package com.guide.green.green_guide.Dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.guide.green.green_guide.R;

/**
 * Show a dialog which allows a user to select cancel or to click outside of the dialog to cancel.
 * The dialog shows a progress bar. This bar initially shown an indefinite status but if the
 * progress is known, this class offers a way to set the progress.
 */
public class DatePickerDialog extends DialogFragment {
    private OnDatePickedListener mOnDatePickedListener;

    public interface OnDatePickedListener {
        void onDatePicked(int year, int month, int day);
        void onCancel();
    }

    public void setOnDatePickedListener(OnDatePickedListener listener) {
        mOnDatePickedListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstState) {
        View layout = inflater.inflate(R.layout.dialog_date_picker, container);
        final DatePicker inputDate = layout.findViewById(R.id.dialog_date_picker_picker);
        Button selectDate = layout.findViewById(R.id.dialog_date_picker_select);
        Button cancel = layout.findViewById(R.id.dialog_date_picker_cancel);

        selectDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
            if (mOnDatePickedListener != null) {
                mOnDatePickedListener.onDatePicked(inputDate.getYear(), inputDate.getMonth(),
                        inputDate.getDayOfMonth());
                mOnDatePickedListener = null;
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
        if (mOnDatePickedListener != null) {
            mOnDatePickedListener.onCancel();
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        if (mOnDatePickedListener != null) {
            mOnDatePickedListener.onCancel();
        }
    }
}
