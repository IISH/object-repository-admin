/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006-2007. loopy project (http://loopy.sourceforge.net).
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.objectrepository.iso9660.impl

/**
 * InputStream that reads a FileEntry's data.
 */
class EntryInputStream extends InputStream {
    // entry within the file system
    private Iso9660FileEntry entry
    // the parent file system
    private Iso9660FileSystem fileSystem

    // current position within entry data
    private int pos

    // number of remaining bytes within entry
    private int rem

    EntryInputStream(final Iso9660FileEntry entry, final Iso9660FileSystem fileSystem) {
        this.fileSystem = fileSystem
        this.entry = entry
        this.pos = 0
        this.rem = (int) entry.getSize()
    }

    @Override
    int read(byte[] b, int off, int len) throws IOException {

        if (this.rem <= 0) {
            return -1
        }
        if (len <= 0) {
            return 0
        }

        int toRead = len

        if (toRead > this.rem) {
            toRead = this.rem
        }

        int read = this.fileSystem.readBytes(this.entry, this.pos, b, off, toRead)
        if (read > 0) {
            this.pos += len
            this.rem -= len
        }

         read
    }

    public int read() throws IOException {

        final byte[] b = new byte[1]

        if (read(b, 0, 1) == 1) {
            return b[0] & 0xff
        } else {
            return -1
        }
    }

    public long skip(final long n) {

        final int len = (n > this.rem) ? this.rem : (int) n

        this.pos += len
        this.rem -= len

        if (this.rem <= 0) {
            close()
        }

         len
    }

    public int available() {
         Math.max(this.rem, 0)
    }

    public int size() {

         (int) this.entry.getSize()
    }

    public void close() {
        this.rem = 0
        this.entry = null
        this.fileSystem = null
    }
}