package org.objectrepository.ftp;


import au.edu.apsr.mtk.base.Div
import au.edu.apsr.mtk.base.METS
import org.apache.ftpserver.ftplet.FileSystemView
import org.apache.ftpserver.ftplet.FtpException
import org.apache.ftpserver.ftplet.FtpFile
import org.apache.ftpserver.ftplet.User
import org.objectrepository.mets.MetsService

public class MetsFileSystemView implements FileSystemView {

    final MetsService metsService
    final User user
    final LinkedHashMap<String, METS> metsDocuments = [:] // label and mets document
    String currLabel

    public MetsFileSystemView(MetsService metsService, User user) {
        this.metsService = metsService
        this.user = user
        this.currLabel = ""
        metsDocuments.put("", metsService.writeMetsFile(user.homeDirectory).METSObject)
    }

    public FtpFile getHomeDirectory() throws FtpException {
        new MetsFtpFile("", user, metsDocuments[""])
    }

    public FtpFile getWorkingDirectory() throws FtpException {
        new MetsFtpFile(currLabel, user, metsDocuments[currLabel])
    }

    /*
    changeWorkingDirectory

    Sets the new working directory.
    As directories as divided into mets views on a per folder basis, we always use the second folder as query.
    for example:
    s=/FOLDER/SUBFOLDER we use labelRoot=FOLDER for the query
     */

    public boolean changeWorkingDirectory(String s) throws FtpException {

        if (s == "./") {  // PWD
            return true
        }
        if (s == "..") {//CDUP
            s = CDUP(s)
        } else if (s[0] != '/') { // cd to directory in same folder
            s = currLabel + "/" + s
        }

        String metsLabel = rootLabel(s)

        if (!metsDocuments.containsKey(metsLabel)) {
            def d = metsService.writeMetsFile(user.homeDirectory, metsLabel.substring(1))
            if (!d) return false
            metsDocuments.put(metsLabel, d.METSObject)
            if (!findDiv(d.METSObject.getStructMapByType("logical")[0].divs[0], s)) return false
        }
        currLabel = s
        true
    }

    // As we were given a filename and not the identifier, we need to get it by browsing the fileSec
    public FtpFile getFile(String s) throws FtpException {
        if (s == "./") {
            s = currLabel
        }
        else if (s[0] != '/') { // directory or file in same folder
            s = currLabel + "/" + s
        }
        def metsLabel = rootLabel(s)
        def d = metsDocuments[metsLabel]
        if (d) return new MetsFtpFile(s, user, d, true, 0, metsService.gridFSService)
        null
    }

    public boolean isRandomAccessible() throws FtpException {
        return false;
    }

    public void dispose() {
        metsDocuments.clear()
    }

    private Div findDiv(Div div, String label) {
        if (div.label == label) return div;
        for (int i = 0; i < div.divs.size(); i++) {
            def d = findDiv(div.divs[i], label)
            if (d) return d;
        }
        null
    }

    /**
     * CDUP
     *
     * Returns the parent or self if there is no parent path
     * / = /
     * /folder = /
     * /folder a/folder b = /folder a
     * /folder a/folder b/folder c = /folder a/folder b
     *
     * @param s
     * @return the parent
     */
    private CDUP(String s) {
        def t = s.tokenize("/")
        if (t.size() < 2) return ""
        t.remove(t.size() - 1)
        "/" + t.join("/")
    }

    /**
     * rootLabel
     *
     * Returns the first folder:
     * / = /
     * /folder = /
     * /folder a/folder b = /folder a
     * /folder a/folder b/folder c = /folder a
     *
     * @param s
     * @return the first folder from the root
     */
    private String rootLabel(String s) {
        def t = s.tokenize("/")
        if (t.size() != 0) return "/" + t[0]
        ""
    }

    /*private FLocat getFLocat(List<FileGrp> fileGrps, String title) {
        fileGrps?.each {
            it.files?.each {
                def flocat = it.FLocats?.find {
                    it.title == title
                }
                if (flocat) return flocat
            }
            def flocat = getFLocat(it.fileGrps, title)
            if ( flocat ) return flocat
        }
        null
    }*/
}
