package com.badeeb.driveit.client.shared;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.badeeb.driveit.client.R;

/**
 * Created by meldeeb on 9/21/17.
 */

public class UiUtils {

    public static void hide(View v) {
        v.setVisibility(View.GONE);
    }

    public static void show(View v) {
        v.setVisibility(View.VISIBLE);
    }

    public static void enable(View v) {
        v.setEnabled(true);
        v.setAlpha(1f);
    }

    public static void disable(View v) {
        v.setEnabled(false);
        v.setAlpha(0.5f);
    }

    public static void hideActivityShownKeyboard(Activity activity){
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideEditTextKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static void showEditTextKeyboard(Context context, EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static ProgressDialog createProgressDialog(Context context){
        return createProgressDialog(context, "Loading. Please wait...");
    }

    public static ProgressDialog createProgressDialog(Context context, String message){
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.DialogTheme);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    /**
     * Dialog with both positive and negative listeners
     * @param context
     * @param style
     * @param title
     * @param message
     * @param positiveMessage
     * @param positiveListener
     * @param negativeMessage
     * @param negativeListener
     * @return
     */
    public static AlertDialog showDialog(Context context, int style, int title, int message,
                                         int positiveMessage, DialogInterface.OnClickListener positiveListener,
                                         int negativeMessage, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveMessage, positiveListener);
        builder.setNegativeButton(negativeMessage, negativeListener);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    /**
     * Dialog with on positive listener
     * @param context
     * @param style
     * @param title
     * @param message
     * @param positiveMessage
     * @param positiveListener
     * @return
     */
    public static AlertDialog showDialog(Context context, int style, int title, int message,
                                         int positiveMessage, DialogInterface.OnClickListener positiveListener
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveMessage, positiveListener);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static AlertDialog showDialog(Context context, int style, int title, String message,
                                         int positiveMessage, DialogInterface.OnClickListener positiveListener,
                                         int negativeMessage, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveMessage, positiveListener);
        builder.setNegativeButton(negativeMessage, negativeListener);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    /**
     * Dialog with on positive listener title only
     * @param context
     * @param style
     * @param title
     * @param positiveMessage
     * @param positiveListener
     * @return
     */
    public static AlertDialog showDialog(Context context, int style, int title,
                                         int positiveMessage, DialogInterface.OnClickListener positiveListener
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
        builder.setTitle(title);
        builder.setPositiveButton(positiveMessage, positiveListener);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

}
