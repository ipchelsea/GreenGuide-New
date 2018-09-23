package com.guide.green.green_guide.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ProgressBar;

import com.guide.green.green_guide.R;

/**
 * Show a dialog which allows a user to select cancel or to click outside of the dialog to cancel.
 * The dialog shows a progress bar. This bar initially shown an indefinite status but if the
 * progress is known, this class offers a way to set the progress.
 */
public class LoadingDialog extends DialogFragment {
    public interface Canceled {
        public void onCancel();
    }

    private Canceled callback;
    private ProgressBar progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Called if the user clicks on the button cancel on this fragment.
     *
     * @param callback
     */
    public LoadingDialog setCallback(Canceled callback) {
        if (callback == null)
            throw new IllegalArgumentException("The cancel callback can't be null.");

        this.callback = callback;
        return this;
    }

    /**
     * Runs the callback when the user clicks outside of the dialog or selects cancel.
     */
    private void onCancel() {
        if (callback != null) {
            callback.onCancel();
        }
    }

    public void setProgress(double completed) {
        if (progress == null) return;

        if (progress.isIndeterminate()) {
            progress.setIndeterminate(false);
        }
        progress.setProgress((int) (progress.getMax() * completed));
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        LoadingDialog.this.onCancel();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View pv = inflater.inflate(R.layout.dialog_loading_with_cancel, null);
        builder.setView(pv).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onCancel();
                }
            });
        Dialog result = builder.create();
        result.setCanceledOnTouchOutside(true);
        progress = (ProgressBar) pv.findViewById(R.id.progress);
        return result;
    }
}
