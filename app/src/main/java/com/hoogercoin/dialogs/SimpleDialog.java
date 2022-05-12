package com.hoogercoin.dialogs;

import static com.hoogercoin.helpers.DialogHelper.checkNullListener;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.hoogercoin.R;
import com.hoogercoin.helpers.TypoHelper;

public class SimpleDialog {
    Dialog dialog;
    public SimpleDialog(Activity activity, String title, String message, View.OnClickListener acceptClickListener, boolean cancelable){
        if (title.length() != 0 || message.length() != 0) {
            dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(cancelable);
            dialog.setContentView(R.layout.simple_dialog_layout);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // detect UI elements
            TextView messageText = dialog.findViewById(R.id.dialog_message);
            TextView titleText = dialog.findViewById(R.id.dialog_title);
            AppCompatButton confirmBtn = dialog.findViewById(R.id.dialog_confirm);

            messageText.setText(TypoHelper.toPersianDigit(message));
            titleText.setText(TypoHelper.toPersianDigit(title));
            if (title.length() == 0)
                titleText.setVisibility(View.GONE);
            confirmBtn.setOnClickListener(checkNullListener(dialog, acceptClickListener));
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
