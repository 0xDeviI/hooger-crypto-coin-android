package com.hoogercoin.dialogs;

import static com.hoogercoin.helpers.DialogHelper.checkNullListener;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.hoogercoin.R;
import com.hoogercoin.helpers.TouchImageView;
import com.hoogercoin.helpers.TypoHelper;

public class ImagePreviewDialog {
    Dialog dialog;
    public ImagePreviewDialog(Activity activity, Drawable imgSrc, boolean cancelable){

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(cancelable);
        dialog.setContentView(R.layout.image_preview_dialog_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TouchImageView touchImage = dialog.findViewById(R.id.touchImage);
        touchImage.setImageDrawable(imgSrc);

        // detect UI elements

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
