package daemonTh;

import java.util.HashMap;

/**
 * Created by MrFabio on 12/06/2015.
 */
public class PathInfo {

    String path;
    HashMap<String,FileInfo> validFilesInfo;
    int checkSeconds;

    public PathInfo(String path, HashMap<String, FileInfo> validFilesInfo, int checkSeconds) {
        this.path = path;
        this.validFilesInfo = validFilesInfo;
        this.checkSeconds = checkSeconds;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HashMap<String, FileInfo> getValidFilesInfo() {
        return validFilesInfo;
    }

    public void setValidFilesInfo(HashMap<String, FileInfo> validFilesInfo) {
        this.validFilesInfo = validFilesInfo;
    }

    public int getCheckSeconds() {
        return checkSeconds;
    }

    public void setCheckSeconds(int checkSeconds) {
        this.checkSeconds = checkSeconds;
    }
}
