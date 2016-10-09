package com.bopr.filechooser;

import android.content.Context;
import android.content.Intent;

/**
 * Class FileChooserIntent.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class FileChooserIntent extends Intent {

    public static final String EXTRA_FILTER = "filter";
    public static final String EXTRA_FILE_NAME = "file_name";
    public static final String EXTRA_ALLOW_CREATE_FILE = "allow_create_file";
    public static final String EXTRA_ALLOW_FILE_OPERATIONS = "allow_file_operations";

    public FileChooserIntent(Context context) {
        super(context, FileChooserActivity.class);
    }

}
