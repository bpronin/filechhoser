package com.bopr.filechooser;

import java.util.List;

/**
 * Class FileChooserContentProvider.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public interface FileChooserContentProvider {

    List<FileInfo> getFiles(FileInfo path, String[] extensions);

    boolean createFolder(FileInfo info);

    boolean renameFile(FileInfo info, FileInfo toInfo);

    boolean deleteFile(FileInfo info);

    long getFileTime(FileInfo info);

    FileInfo getDefaultPath(String fileName);

    FileInfo getParent(FileInfo file);
}
