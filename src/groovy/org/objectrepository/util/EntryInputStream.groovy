package org.objectrepository.util

import com.mongodb.gridfs.GridFSDBFile
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * EntryInputStream
 *
 * ToDo: Expand this Input stream to support HTTP 206 and Image Iso 9660 file delivery.
 *
 */
class EntryInputStream extends InputStream {

    private final GridFSDBFile file
    private long from
    private long to

    EntryInputStream(GridFSDBFile file, long from, long  to) {
        this.file = file
        this.from = from
        this.to = to
    }

    @Override
    int read() throws IOException {
        0 // Not implemented
    }

    @Override
    int read(byte[] bytes, int fromChunk, int toChunk) throws IOException {

        long c = 0
        int fromOffset, toOffset, length

        for (int n = fromChunk; n <= toChunk; n++) {
            fromOffset = (fromChunk == n) ? from : 0
            toOffset = (toChunk == n) ? to : file.chunkSize - 1
            length = toOffset - fromOffset + 1
            c += length
            final byte[] _data = file.getChunk(n)
            if (log.isInfoEnabled()) println([chunk: n, fromChunk: fromChunk, toChunk: toChunk, from: from, to: to, fromOffset: fromOffset, toOffset: toOffset, _dataLength: _data.length, length: length, total: c, fileLength: file.length])
        }
    }

    private static final Log log = LogFactory.getLog(EntryInputStream.class);

}
