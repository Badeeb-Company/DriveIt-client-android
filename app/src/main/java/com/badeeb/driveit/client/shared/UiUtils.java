package com.badeeb.driveit.client.shared;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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

    public static ProgressDialog createProgressDialog(Context context, int style){
        ProgressDialog progressDialog = new ProgressDialog(context, style);
        progressDialog.setMessage("Loading. Please wait...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

}
