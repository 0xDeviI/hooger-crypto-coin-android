package com.hoogercoin.helpers;

import android.app.Dialog;
import android.view.View;

public class DialogHelper {
    public static View.OnClickListener checkNullListener(Dialog dialog, View.OnClickListener onClickListener){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null)
                    onClickListener.onClick(v);
                dialog.dismiss();
            }
        };
    }
}
