package com.pine.media.base.util;

import android.content.Intent;
import android.os.Build;

public class SysIntentUtils {
    public static Intent getFileSelectorIntent(String... supportedMimeTypes) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(supportedMimeTypes.length == 1 ? supportedMimeTypes[0] : "*/*");
            if (supportedMimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes);
            }
        } else {
            String mimeTypes = "";
            for (String mimeType : supportedMimeTypes) {
                mimeTypes += mimeType + "|";
            }
            intent.setType(mimeTypes.substring(0, mimeTypes.length() - 1));
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }
}
