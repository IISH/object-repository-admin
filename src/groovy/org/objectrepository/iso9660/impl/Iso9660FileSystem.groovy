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

import org.objectrepository.iso9660.spi.AbstractBlockFileSystem
import org.objectrepository.iso9660.spi.VolumeDescriptorSet


public class Iso9660FileSystem extends AbstractBlockFileSystem<Iso9660FileEntry> {

    public Iso9660FileSystem(def file) throws IOException {
        super(file, Constants.DEFAULT_BLOCK_SIZE, Constants.RESERVED_SECTORS)
    }

    public String getEncoding() {
         ((Iso9660VolumeDescriptorSet) getVolumeDescriptorSet()).getEncoding()
    }

    public InputStream getInputStream(Iso9660FileEntry entry) {
         new EntryInputStream(entry, this)
    }

    byte[] getBytes(Iso9660FileEntry entry) throws IOException {
        int size = (int) entry.getSize()

        byte[] buf = new byte[size]

        readBytes(entry, 0, buf, 0, size)

         buf
    }

    int readBytes(Iso9660FileEntry entry, int entryOffset, byte[] buffer, int bufferOffset, int len)
            throws IOException {
        long startPos = (entry.getStartBlock() * Constants.DEFAULT_BLOCK_SIZE) + entryOffset
         readData(startPos, buffer, bufferOffset, len);
    }

    protected Iterator<Iso9660FileEntry> iterator(Iso9660FileEntry rootEntry) {
         new EntryIterator(this, rootEntry)
    }

    protected VolumeDescriptorSet<Iso9660FileEntry> createVolumeDescriptorSet() {
         new Iso9660VolumeDescriptorSet(this)
    }
}
