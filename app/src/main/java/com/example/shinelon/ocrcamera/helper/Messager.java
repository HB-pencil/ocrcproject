package com.example.shinelon.ocrcamera.helper;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.shinelon.ocrcamera.R;

/**
 * Helper class to show notification and error messages.
 */
public class Messager {
    /**
     * Show alert message
     * @param context The parent context.
     * @param message The message to display.
     * @param title The message title.
     * @param onClickListener 'OK' button click listener.
     * @param onDismissListener on dismiss dialog listener.
     */
    private static void showAlert(Context context, String message, String title, OnClickListener onClickListener, OnDismissListener onDismissListener) {
        boolean threadHasLooper = threadHasLooper();
        if(!threadHasLooper)
            Looper.prepare();

        // Build the dialog
        AlertDialog.Builder msgDlgBuilder  = new AlertDialog.Builder(context);
        msgDlgBuilder.setMessage(message);
        msgDlgBuilder.setTitle(title);
        msgDlgBuilder.setPositiveButton(context.getString(R.string.ok), onClickListener);

        AlertDialog msgDlg = msgDlgBuilder.create();
        msgDlg.setCanceledOnTouchOutside(false);
        if(onDismissListener != null)
            msgDlg.setOnDismissListener(onDismissListener);

        // Show the dialog
        msgDlg.show();

        if(!threadHasLooper) {
            Looper.loop();
            Looper looper = Looper.myLooper();
            if(looper != null)
                looper.quit();
        }
    }

    /**
     * Show a message
     * @param context The parent context.
     * @param message The message to display.
     * @param title The message title.
     * @param listener 'OK' button click listener.
     */
    @SuppressWarnings("unused")
    public static void showMessage(Context context, String message, String title, OnClickListener listener) {
        showAlert(context, message, title, listener, null);
    }

    /**
     * Show an error message
     * @param context The parent context.
     * @param message The message to display.
     * @param title The message title.
     */
    public static void showError(Context context, String message, String title) {
        showAlert(context, message, title, null, null);
    }

    /**
     * Show an error message
     * @param context The parent context.
     * @param message The message to display.
     * @param title The message title.
     * @param listener on dismiss dialog listener.
     */
    public static void showError(Context context, String message, String title, OnDismissListener listener) {
        showAlert(context, message, title, null, listener);
    }

    /**
     * Show error loading native libs message.
     * @param context The parent context.
     * @param message The message to display.
     * @param listener on dismiss dialog listener.
     */
    public static void showErrorLoadingLibsMessage(Context context, String message, OnDismissListener listener) {
        showAlert(context,
                message,
                context.getString(R.string.err_loading_libs),
                null,
                listener);
    }

    /**
     * Show kernel expiration message
     * @param context The parent context.
     * @param listener on dismiss dialog listener.
     */
    public static void showKernelExpiredMessage(Context context, OnDismissListener listener) {
        showAlert(context,
                context.getString(R.string.err_invalid_lic),
                context.getString(R.string.err_no_lic),
                null,
                listener);
    }

    /**
     * Show a Toast message
     * @param context The parent context.
     * @param message The message to display.
     */
    @SuppressWarnings("unused")
    public static void showNotification(Context context, String message) {
        boolean threadHasLooper = threadHasLooper();
        if(!threadHasLooper)
            Looper.prepare();

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        if(!threadHasLooper) {
            Looper.loop();
            Looper looper = Looper.myLooper();
            if(looper != null)
                looper.quit();
        }
    }

    private static boolean threadHasLooper() {
        return Looper.myLooper() != null;
    }
}