package org.objectrepository.ftp;

import org.apache.ftpserver.ftplet.FtpFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lwo
 * Date: 1/29/13
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetsFtpFile implements FtpFile {

    public MetsFtpFile(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    private String absolutePath;

    public String getAbsolutePath() {
        return this.absolutePath;
    }

    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isHidden() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDirectory() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isFile() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean doesExist() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isReadable() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isWritable() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRemovable() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getOwnerName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getGroupName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getLinkCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getLastModified() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean setLastModified(long l) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean mkdir() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean delete() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean move(FtpFile ftpFile) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<FtpFile> listFiles() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public OutputStream createOutputStream(long l) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public InputStream createInputStream(long l) throws IOException {
        throw new IOException("Method createInputStream not implemented");
    }
}
