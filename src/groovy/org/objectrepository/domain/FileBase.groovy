package org.objectrepository.domain

import org.bson.types.ObjectId

/**
 * ObjectFile
 *
 * A base file class
 *
 */
abstract class FileBase {

    ObjectId id
    String md5
    Long length = 0
    String contentType
}