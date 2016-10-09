package com.bopr.filechooser;

/**
 * Class FileInfo.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class FileInfo {

    private String path;
    private String name;

    public FileInfo(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFile() {
        return name != null;
    }

    public boolean isDirectory() {
        return name == null;
    }
}
