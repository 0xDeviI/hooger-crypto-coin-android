package com.hoogercoin.dialogs;

import static com.hoogercoin.helpers.DialogHelper.checkNullListener;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.hoogercoin.R;
import com.hoogercoin.helpers.TypoHelper;

public class YesNoDialog {
    Dialog dialog;
    TextView messageText;
    TextView titleText;
    AppCompatButton acceptBtn;
    AppCompatButton rejectBtn;
    public YesNoDialog(Activity activity, String title, String message, View.OnClickListener acceptClickListener, View.OnClickListener rejectClickListener, boolean cancelable){
        if (title.length() != 0 || message.length() != 0) {
            dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(cancelable);
            dialog.setContentView(R.layout.yes_no_dialog_layout);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // detect UI elements
            messageText = dialog.findViewById(R.id.dialog_message);
            titleText = dialog.findViewById(R.id.dialog_title);
            acceptBtn = dialog.findViewById(R.id.dialog_accept);
            rejectBtn = dialog.findViewById(R.id.dialog_reject);

            messageText.setText(TypoHelper.toPersianDigit(message));
            titleText.setText(TypoHelper.toPersianDigit(title));
            if (title.length() == 0)
                titleText.setVisibility(View.GONE);
            acceptBtn.setOnClickListener(checkNullListener(dialog, acceptClickListener));
            rejectBtn.setOnClickListener(checkNullListener(dialog, rejectClickListener));
        }
    }

    public YesNoDialog setAcceptButtonText(String text){
        acceptBtn.setText(text);
        return this;
    }

    public YesNoDialog setRejectButtonText(String text){
        rejectBtn.setText(text);
        return this;
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
