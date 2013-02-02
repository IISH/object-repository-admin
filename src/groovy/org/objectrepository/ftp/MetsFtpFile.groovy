package org.objectrepository.ftp;


import au.edu.apsr.mtk.base.METS
import org.apache.ftpserver.ftplet.FtpFile
import org.apache.ftpserver.ftplet.User
import au.edu.apsr.mtk.base.Div

public class MetsFtpFile implements FtpFile {

    private String currLabel
    private User user
    private METS mets
    private long length
    private long created
    private boolean isDirectory
    private def gridFSService
    private def bucket
    private def pid

    public MetsFtpFile(String currLabel, User user, METS mets, boolean isDirectory = true, long length = 0,  long created = 0, def gridFSService = null) {
        this.currLabel = currLabel
        this.user = user
        this.mets = mets
        this.isDirectory = isDirectory
        this.length = length
        this.created = created
        this.gridFSService = gridFSService
    }

    public String getAbsolutePath() {
        (currLabel == "") ? "/" : currLabel
    }

    public String getName() {
        currLabel.substring(currLabel.lastIndexOf("/") + 1)
    }

    public boolean isHidden() {
        false
    }

    public boolean isDirectory() {
        isDirectory
    }

    public boolean isFile() {
        !isDirectory
    }

    /**
     * Check if the file exists. Whilst doing so, we retrieve the PID and bucket values
     * @return
     */
    public boolean doesExist() {
        String parent = currLabel[0..currLabel.lastIndexOf("/") - 1]
        findDiv(mets.getStructMapByType("logical").get(0).divs[0], parent)?.fptrs?.each { fprt ->
            final f = mets.fileSec.getFile(fprt.fileID)
            f?.FLocats?.each { locat ->
                if (parent + "/" + locat.title == currLabel) {
                    URL url = new URL(locat.href) //  retrieve the PID syntax: URL/NA/PID?qualifier
                    pid = url.path.substring(1) // /NA/ID -> NA/ID
                    bucket = currLabel.split("/")[2]
                    isDirectory = false
                    length = f.size
                    created = getTime(f.created)
                    return true
                }
            }
        }
        (pid)
    }

    public boolean isReadable() {
        true
    }

    public boolean isWritable() {
        false
    }

    public boolean isRemovable() {
        false
    }

    public String getOwnerName() {
        "user"
    }

    public String getGroupName() {
        "group"
    }

    public int getLinkCount() {
        (isFile()) ? 1 : 3
    }

    public long getLastModified() {
        created
    }

    public boolean setLastModified(long l) {
        false
    }

    public long getSize() {
        length
    }

    public boolean mkdir() {
        false
    }

    public boolean delete() {
        false
    }

    public boolean move(FtpFile ftpFile) {
        false
    }

    public List<FtpFile> listFiles() {
        // now return all the files under the directory
        def hack = null
        def virtualFiles = []
        if (currLabel == "") {
            mets.getStructMapByType("logical").get(0).divs.each {
                virtualFiles << new MetsFtpFile(it.label, user, mets)
            }
        }
        else {
            def div = findDiv(mets.getStructMapByType("logical").get(0).divs[0], currLabel)
            div?.divs?.each {
                virtualFiles << new MetsFtpFile(it.label, user, mets)
            }
            div?.fptrs?.each { fptr ->
                def file = mets.fileSec.getFile(fptr.fileID)
                if (file) file.FLocats.each {
                    virtualFiles << new MetsFtpFile(it.title, user, mets, false, file.size, getTime(file.created))
                } else {      // Oddly, when we find a fileID; we cannot (always) locate the corresponding file ID in the fileSec
                    if (hack) {
                        file = hack[fptr.fileID]
                        virtualFiles << new MetsFtpFile(file.FLocats[0].title, user, mets, false, file.size, getTime(file.created))
                    } else {
                        hack = [:]
                        println("NO file found. Odd")
                        mets.fileSec.fileGrps.each {
                            it.files.each {
                                hack.put(it.ID, it)
                            }
                        }
                        file = hack[fptr.fileID]
                        virtualFiles << new MetsFtpFile(file.FLocats[0].title, user, mets, false, file.size, getTime(file.created))
                    }
                }
            }
        }
        hack?.clear()
        virtualFiles
    }

    long getTime(String s) {
        ( s == null ) ? 0 : Date.parse("yyyy-MM-dd'T'mm:hh:ss'Z'", s).time
    }

    public OutputStream createOutputStream(long l) throws IOException {
        throw new IOException("Method createOutputStream not implemented.");
    }

    public InputStream createInputStream(long l) throws IOException {
        gridFSService.findByPid(pid, bucket)?.inputStream
    }

    private Div findDiv(Div div, String label) {
        if (div.label == label) return div;
        for (int i = 0; i < div.divs.size(); i++) {
            def d = findDiv(div.divs[i], label)
            if (d) return d;
        }
        null
    }
}
