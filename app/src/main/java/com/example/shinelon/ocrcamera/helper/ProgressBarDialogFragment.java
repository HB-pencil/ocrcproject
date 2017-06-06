package com.example.shinelon.ocrcamera.helper;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.shinelon.ocrcamera.R;

/**
 * Created by Shinelon on 2017/4/7.ProgressBarDialogFragment,用于显示转化过程的对话框提示。
 */

public class ProgressBarDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setCancelable(false)
                .setTitle(R.string.recognize)
                .setView(R.layout.progressbar_layout)
                .setCancelable(true)
                .create();
    }
}
