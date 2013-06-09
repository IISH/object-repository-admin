/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006-2007. loopy project (http://loopy.sourceforge.net).
 *  
 * This library is free software you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.objectrepository.iso9660.spi

import com.github.stephenc.javaisotools.loopfs.api.FileEntry
import com.github.stephenc.javaisotools.loopfs.api.FileSystem
import com.mongodb.gridfs.GridFSDBFile

import javax.servlet.http.HttpServletResponse

/**
 * Implementation of FileSystem that is backed by a {@link java.io.RandomAccessFile}.
 */
public abstract class AbstractFileSystem<T extends FileEntry> implements FileSystem<T> {

    /**
     * Channel to the open file.
     */
    private def channel
    private long pos
    private byte[] _n = -1
    private byte[] _data

    protected AbstractFileSystem(final def file) {
        this.channel = file
    }

    public synchronized void close() throws IOException {
        // Method not implemented
    }

    /**
     * Moves the pointer in the underlying file to the specified position.
     */
    public synchronized boolean isClosed() {
        (null == this.channel)
    }

    protected final void seek(long pos) throws IOException {
        this.pos = pos;
    }

    /**
     * Reads up to <code>length</code> bytes into the specified buffer, starting at the specified offset. The actual
     * number of bytes read will be less than <code>length</code> if there are not enough available bytes to read, or if
     * the buffer is not large enough.
     *
     * @return the number of bytes read into the buffer
     */
    protected final int read(byte[] buffer, int offset, int length) throws IOException {
        //this.channel.read(buffer, offset, length)
        long from = offset + pos;
        long to = from + length - 1

        if (from > to) return -1

        // find first chunk by index:
        int fromChunk = Math.floor(from / channel.chunkSize)
        if (fromChunk < 0 || fromChunk > channel.numChunks() - 1) return -1

        // find the last chunk by index:
        int toChunk = Math.floor(to / channel.chunkSize)
        if (toChunk < 0 || toChunk > channel.numChunks() - 1) return -1

        // Standardize to the range of chunkSize
        from = from % channel.chunkSize
        to = to % channel.chunkSize

        int fromOffset, toOffset, c
        // now get all chunks that cover this range
        for (int n = fromChunk; n <= toChunk; n++) {
            fromOffset = (fromChunk == n) ? from : 0
            toOffset = (toChunk == n) ? to : channel.chunkSize - 1
            length = toOffset - fromOffset + 1
            if (_n != n) {
                _n = n
                _data = channel.getChunk(n)
            }
            System.arraycopy(_data, fromOffset, buffer, c, length)
            c += length
        }
        c
    }

}