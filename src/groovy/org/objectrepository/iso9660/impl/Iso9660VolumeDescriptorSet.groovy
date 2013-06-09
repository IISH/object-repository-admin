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

package org.objectrepository.iso9660.impl

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.objectrepository.iso9660.spi.VolumeDescriptorSet

public class Iso9660VolumeDescriptorSet implements VolumeDescriptorSet<Iso9660FileEntry> {

    public static final int TYPE_BOOTRECORD = 0
    public static final int TYPE_PRIMARY_DESCRIPTOR = 1
    public static final int TYPE_SUPPLEMENTARY_DESCRIPTOR = 2
    public static final int TYPE_PARTITION_DESCRIPTOR = 3
    public static final int TYPE_TERMINATOR = 255

    private static final Log log = LogFactory.getLog(Iso9660VolumeDescriptorSet.class)

    private final Iso9660FileSystem isoFile

    // common
    private String systemIdentifier
    private String volumeSetIdentifier
    private String volumeIdentifier
    private String publisher
    private String preparer
    private String application
    private Iso9660FileEntry rootDirectoryEntry

    // primary
    private String standardIdentifier
    private long totalBlocks
    private int volumeSetSize
    private int volumeSequenceNumber
    private long creationTime
    private long mostRecentModificationTime
    private long expirationTime
    private long effectiveTime
    private long pathTableSize
    private long locationOfLittleEndianPathTable
    private long locationOfOptionalLittleEndianPathTable
    private long locationOfBigEndianPathTable
    private long locationOfOptionalBigEndianPathTable

    // supplementary
    public String encoding = Constants.DEFAULT_ENCODING
    public String escapeSequences

    private boolean hasPrimary = false
    private boolean hasSupplementary = false

    /**
     * Initialize this instance.
     *
     * @param fileSystem the parent file system
     */
    public Iso9660VolumeDescriptorSet(Iso9660FileSystem fileSystem) {
        this.isoFile = fileSystem
    }

    public boolean deserialize(byte[] descriptor) throws IOException {
        final int type = Util.getUInt8(descriptor, 1)

        boolean terminator = false

        switch (type) {
            case TYPE_TERMINATOR:
                if (!this.hasPrimary) {
                    throw new IOException("No primary volume descriptor found")
                }
                terminator = true
                break
            case TYPE_BOOTRECORD:
                log.debug("Found boot record")
                break
            case TYPE_PRIMARY_DESCRIPTOR:
                log.debug("Found primary descriptor")
                deserializePrimary(descriptor)
                break
            case TYPE_SUPPLEMENTARY_DESCRIPTOR:
                log.debug("Found supplementatory descriptor")
                deserializeSupplementary(descriptor)
                break
            case TYPE_PARTITION_DESCRIPTOR:
                log.debug("Found partition descriptor")
                break
            default:
                log.debug("Found unknown descriptor with type " + type)
        }

        return terminator
    }

    /**
     * Read the fields of a primary volume descriptor.
     *
     * @param descriptor the descriptor bytes
     */
    private void deserializePrimary(byte[] descriptor) throws IOException {
        // according to the spec, some ISO 9660 file systems can contain multiple identical primary
        // volume descriptors
        if (this.hasPrimary) {
            return
        }

        validateBlockSize(descriptor)

        if (!this.hasSupplementary) {
            deserializeCommon(descriptor)
        }

        this.standardIdentifier = Util.getDChars(descriptor, 2, 5)
        this.volumeSetSize = Util.getUInt16Both(descriptor, 121)
        this.volumeSequenceNumber = Util.getUInt16Both(descriptor, 125)
        this.totalBlocks = Util.getUInt32Both(descriptor, 81)
        this.publisher = Util.getDChars(descriptor, 319, 128)
        this.preparer = Util.getDChars(descriptor, 447, 128)
        this.application = Util.getDChars(descriptor, 575, 128)
        //this.copyrightFile = Descriptor.get(buffer, 703, 37)
        //this.abstractFile = Descriptor.get(buffer, 740, 37)
        //this.bibliographicalFile = Descriptor.get(buffer, 777, 37)
        this.creationTime = Util.getStringDate(descriptor, 814)
        this.mostRecentModificationTime = Util.getStringDate(descriptor, 831)
        this.expirationTime = Util.getStringDate(descriptor, 848)
        this.effectiveTime = Util.getStringDate(descriptor, 865)
        this.pathTableSize = Util.getUInt32Both(descriptor, 133)
        this.locationOfLittleEndianPathTable = Util.getUInt32LE(descriptor, 141)
        this.locationOfOptionalLittleEndianPathTable = Util.getUInt32LE(descriptor, 145)
        this.locationOfBigEndianPathTable = Util.getUInt32BE(descriptor, 149)
        this.locationOfOptionalBigEndianPathTable = Util.getUInt32BE(descriptor, 153)

        this.hasPrimary = true
    }

    /**
     * The supplementary descriptor sets the character encoding and may override the common
     * descriptor information.
     *
     * @param descriptor the descriptor bytes
     */
    private void deserializeSupplementary(byte[] descriptor) throws IOException {
        // for now, only recognize one supplementary descriptor
        if (this.hasSupplementary) {
            return
        }

        validateBlockSize(descriptor)

        String escapeSequences = Util.getDChars(descriptor, 89, 32)

        String enc = _getEncoding(escapeSequences)

        if (null != enc) {
            this.encoding = enc
            this.escapeSequences = escapeSequences

            deserializeCommon(descriptor)

            this.hasSupplementary = true
        } else {
            log.warn("Unsupported encoding, escapeSequences: '" + this.escapeSequences + "'")
        }
    }

    /**
     * Read the information common to primary and secondary volume descriptors.
     *
     * @param descriptor the volume descriptor bytes
     */
    private void deserializeCommon(byte[] descriptor) throws IOException {
        this.systemIdentifier = Util.getAChars(descriptor, 9, 32, this.encoding)
        this.volumeIdentifier = Util.getDChars(descriptor, 41, 32, this.encoding)
        this.volumeSetIdentifier = Util.getDChars(descriptor, 191, 128, this.encoding)
        this.rootDirectoryEntry = new Iso9660FileEntry(this.isoFile, descriptor, 157)
    }

    /**
     * Check that the block size is what we expect.
     *
     * @param descriptor the descriptor bytes
     */
    private void validateBlockSize(byte[] descriptor) throws IOException {
        int blockSize = Util.getUInt16Both(descriptor, 129)
        if (blockSize != Constants.DEFAULT_BLOCK_SIZE) {
            throw new IOException("Invalid block size: " + blockSize)
        }
    }

    /**
     * Derive an encoding name from the given escape sequences.
     *
     * @param escapeSequences
     * @return
     */
    private String _getEncoding(String escapeSequences) {
        String encoding = null

        if (escapeSequences.equals("%/@")) {
            // UCS-2 level 1
            encoding = "UTF-16BE"
        } else if (escapeSequences.equals("%/C")) {
            // UCS-2 level 2
            encoding = "UTF-16BE"
        } else if (escapeSequences.equals("%/E")) {
            // UCS-2 level 3
            encoding = "UTF-16BE"
        }

         encoding
    }

    /**
     * Returns true if the .iso file has one or more supplementary volume descriptors.
     *
     * @return
     */
    public boolean hasSupplementary() {
         this.hasSupplementary
    }

    public String getSystemIdentifier() {
         this.systemIdentifier
    }

    public String getVolumeSetIdentifier() {
         this.volumeSetIdentifier
    }

    public String getVolumeIdentifier() {
         this.volumeIdentifier
    }

    public String getPublisher() {
         this.publisher
    }

    public String getPreparer() {
         this.preparer
    }

    public String getApplication() {
         this.application
    }

    public Iso9660FileEntry getRootEntry() {
         this.rootDirectoryEntry
    }

    public String getStandardIdentifier() {
         this.standardIdentifier
    }

    public long getTotalBlocks() {
         this.totalBlocks
    }

    public int getVolumeSetSize() {
         this.volumeSetSize
    }

    public int getVolumeSequenceNumber() {
         this.volumeSequenceNumber
    }

    public long getCreationTime() {
         this.creationTime
    }

    public long getLastModifiedTime() {
         this.mostRecentModificationTime
    }

    public long getExpirationTime() {
         this.expirationTime
    }

    public long getEffectiveTime() {
         this.effectiveTime
    }

    public long getPathTableSize() {
         this.pathTableSize
    }

    public long getLocationOfLittleEndianPathTable() {
         this.locationOfLittleEndianPathTable
    }

    public long getLocationOfOptionalLittleEndianPathTable() {
         this.locationOfOptionalLittleEndianPathTable
    }

    public long getLocationOfBigEndianPathTable() {
         this.locationOfBigEndianPathTable
    }

    public long getLocationOfOptionalBigEndianPathTable() {
         this.locationOfOptionalBigEndianPathTable
    }

    /**
     * Returns the character encoding.
     *
     * @return
     */
    public String getEncoding() {
         this.encoding
    }

    public String getEscapeSequences() {
         this.escapeSequences
    }
}