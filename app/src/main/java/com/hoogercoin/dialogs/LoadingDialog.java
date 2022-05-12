package com.hoogercoin.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.hoogercoin.R;
import com.hoogercoin.helpers.TypoHelper;

public class LoadingDialog {
    Dialog dialog;
    public LoadingDialog(Activity activity, String message, boolean cancelable){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(cancelable);
        dialog.setContentView(R.layout.loading_dialog_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // detect UI elements
        TextView messageText = dialog.findViewById(R.id.loadingMessage);
        if (message.length() > 0){
            messageText.setVisibility(View.VISIBLE);
            messageText.setText(TypoHelper.toPersianDigit(message));
        }
    }

    public void show(){
        if (dialog != null)
            dialog.show();
    }

    public void dismiss(){
        if (dialog != null)
            dialog.dismiss();
    }
}
