package com.bopr.filechooser;

import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.bopr.filechooser.FileChooserFragment.UP_DIR;

/**
 * Class FileChooserContentProvider.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class FileChooserFileContentProvider implements FileChooserContentProvider {

    @Override
    public List<FileInfo> getFiles(FileInfo path, String[] extensions) {
        return null;
    }

    @Override
    public boolean createFolder(FileInfo info) {
        return false;
    }

    @Override
    public boolean renameFile(FileInfo info, FileInfo toInfo) {
        return false;
    }

    @Override
    public boolean deleteFile(FileInfo info) {
        return false;
    }

    @Override
    public long getFileTime(FileInfo info) {
        return 0;
    }

    @Override
    public FileInfo getDefaultPath(String fileName) {
        return null;
    }

    @Override
    public FileInfo getParent(FileInfo file) {
        return null;
    }
}
