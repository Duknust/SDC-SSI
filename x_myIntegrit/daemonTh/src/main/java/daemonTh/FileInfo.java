package daemonTh;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by MrFabio on 11/06/2015.
 */
public class FileInfo implements Serializable{

    public enum State{
        OK,DELETED,NEW,UPDATED
    }
    private byte[] fileHash;
    private boolean canRead,canWrite,canExecute,valid,signaled;
    private String filename;
    private long lastModified;
    private State state;

    FileInfo(){
        fileHash = new byte[16];
        canRead = false;
        canWrite = false;
        canExecute = false;
        filename="";
        lastModified = 0;
        state = State.OK;
        signaled=false;
    }

    FileInfo(String filename,boolean canRead,boolean canWrite,boolean canExecute,boolean valid,long lastModified,State state,boolean signaled){
        this.filename=filename;
        fileHash = new byte[16];
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.canExecute = canExecute;
        this.valid = valid;
        this.lastModified = lastModified;
        this.state = state;
        this.signaled = signaled;
    }

    FileInfo(String filename,byte[] hash,boolean canRead,boolean canWrite,boolean canExecute,long lastModified,State state,boolean signaled){
        this.filename=filename;
        fileHash = hash;
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.canExecute = canExecute;
        this.valid = false;
        this.lastModified = lastModified;
        this.state = state;
        this.signaled = signaled;
    }


    FileInfo(String filename,byte[] hash,boolean canRead,boolean canWrite,boolean canExecute,boolean valid,long lastModified,State state,boolean signaled){
        this.filename=filename;
        fileHash = hash;
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.canExecute = canExecute;
        this.valid = valid;
        this.lastModified = lastModified;
        this.state = state;
        this.signaled = signaled;
    }

    public byte[] getFileHash() {
        return fileHash;
    }

    public void setFileHash(byte[] fileHash) {
        this.fileHash = fileHash;
    }

    public boolean CanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean CanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean CanExecute() {
        return canExecute;
    }

    public void setCanExecute(boolean canExecute) {
        this.canExecute = canExecute;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isSignaled() {
        return signaled;
    }

    public void setSignaled(boolean signaled) {
        this.signaled = signaled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        if (canRead != fileInfo.canRead) return false;
        if (canWrite != fileInfo.canWrite) return false;
        if (canExecute != fileInfo.canExecute) return false;
        if (valid != fileInfo.valid) return false;
        if (lastModified != fileInfo.lastModified) return false;
        if (!Arrays.equals(fileHash, fileInfo.fileHash)) return false;
        return filename.equals(fileInfo.filename);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(fileHash);
        result = 31 * result + (canRead ? 1 : 0);
        result = 31 * result + (canWrite ? 1 : 0);
        result = 31 * result + (canExecute ? 1 : 0);
        result = 31 * result + (valid ? 1 : 0);
        result = 31 * result + filename.hashCode();
        result = 31 * result + (int) (lastModified ^ (lastModified >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileHash=" + Arrays.toString(fileHash) +
                ", canRead=" + canRead +
                ", canWrite=" + canWrite +
                ", canExecute=" + canExecute +
                ", valid=" + valid +
                ", filename='" + filename + '\'' +
                '}';
    }
}
