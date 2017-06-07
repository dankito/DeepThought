/*
 *	SimpleImageInfo.java
 *
 *	@version 0.1
 *	@author  Jaimon Mathew <http://www.jaimon.co.uk>
 *
 *	A Java class to determine image width, height and MIME types for a number of image file formats without loading the whole image data.
 *
 *	Revision history
 *	0.1 - 29/Jan/2011 - Initial version created
 *
 *  -------------------------------------------------------------------------------

 	This code is licensed under the Apache License, Version 2.0 (the "License");
 	You may not use this file except in compliance with the License.

 	You may obtain a copy of the License at

 	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

 *  -------------------------------------------------------------------------------
 */


// Copied from http://blog.jaimon.co.uk/simpleimageinfo/SimpleImageInfo.java.html (https://jaimonmathew.wordpress.com/2011/01/29/simpleimageinfo/)

package net.dankito.faviconextractor

import java.io.*


class SimpleImageInfo {
    var height: Int = 0
    var width: Int = 0
    var mimeType: String? = null

    private constructor() {

    }

    @Throws(IOException::class)
    constructor(file: File) {
        val inputStream = FileInputStream(file)
        try {
            processStream(inputStream)
        } finally {
            inputStream.close()
        }
    }

    @Throws(IOException::class)
    constructor(inputStream: InputStream) {
        processStream(inputStream)
    }

    @Throws(IOException::class)
    constructor(bytes: ByteArray) {
        val inputStream = ByteArrayInputStream(bytes)
        try {
            processStream(inputStream)
        } finally {
            inputStream.close()
        }
    }

    @Throws(IOException::class)
    private fun processStream(inputStream: InputStream) {
        val c1 = inputStream.read()
        val c2 = inputStream.read()
        var c3 = inputStream.read()

        mimeType = null
        height = -1
        width = height

        if (isGifFile(c1, c2, c3)) { // GIF
            inputStream.skip(3)
            width = readInt(inputStream, 2, false)
            height = readInt(inputStream, 2, false)
            mimeType = "image/gif"
        } else if (isJpegFile(c1, c2)) { // JPG
            while (c3 == 255) {
                val marker = inputStream.read()
                val len = readInt(inputStream, 2, true)
                if (marker == 192 || marker == 193 || marker == 194) {
                    inputStream.skip(1)
                    height = readInt(inputStream, 2, true)
                    width = readInt(inputStream, 2, true)
                    mimeType = "image/jpeg"
                    break
                }
                inputStream.skip((len - 2).toLong())
                c3 = inputStream.read()
            }
        } else if (isPngFile(c1, c2, c3)) { // PNG
            inputStream.skip(15)
            width = readInt(inputStream, 2, true)
            inputStream.skip(2)
            height = readInt(inputStream, 2, true)
            mimeType = "image/png"
        } else if (c1 == 66 && c2 == 77) { // BMP
            inputStream.skip(15)
            width = readInt(inputStream, 2, false)
            inputStream.skip(2)
            height = readInt(inputStream, 2, false)
            mimeType = "image/bmp"
        } else {
            val c4 = inputStream.read()
            if (isTiffFile(c1, c2, c3, c4)) { //TIFF
                val bigEndian = c1 == 'M'.toInt()
                var ifd = 0
                val entries: Int
                ifd = readInt(inputStream, 4, bigEndian)
                inputStream.skip((ifd - 8).toLong())
                entries = readInt(inputStream, 2, bigEndian)
                for (i in 1..entries) {
                    val tag = readInt(inputStream, 2, bigEndian)
                    val fieldType = readInt(inputStream, 2, bigEndian)
                    val count = readInt(inputStream, 4, bigEndian).toLong()
                    val valOffset: Int
                    if (fieldType == 3 || fieldType == 8) {
                        valOffset = readInt(inputStream, 2, bigEndian)
                        inputStream.skip(2)
                    } else {
                        valOffset = readInt(inputStream, 4, bigEndian)
                    }
                    if (tag == 256) {
                        width = valOffset
                    } else if (tag == 257) {
                        height = valOffset
                    }
                    if (width != -1 && height != -1) {
                        mimeType = "image/tiff"
                        break
                    }
                }
            } else {
                val c5 = inputStream.read()
                val c6 = inputStream.read()

                if (isIconFileWithBitmap(c1, c2, c3, c4, c5, c6)) {
                    mimeType = "image/x-icon"
                    width = inputStream.read() // 7th byte is the width
                    height = inputStream.read() // and 8th the height
                }
            }
        }
        if (mimeType == null) {
            throw IOException("Unsupported image type")
        }
    }

    private fun isGifFile(c1: Int, c2: Int, c3: Int) = c1 == 'G'.toInt() && c2 == 'I'.toInt() && c3 == 'F'.toInt()

    private fun isJpegFile(c1: Int, c2: Int) = c1 == 0xFF && c2 == 0xD8

    private fun isPngFile(c1: Int, c2: Int, c3: Int) = c1 == 137 && c2 == 80 && c3 == 78

    private fun isTiffFile(c1: Int, c2: Int, c3: Int, c4: Int) : Boolean {
        return (c1 == 'M'.toInt() && c2 == 'M'.toInt() && c3 == 0 && c4 == 42) ||
                (c1 == 'I'.toInt() && c2 == 'I'.toInt() && c3 == 42 && c4 == 0)
    }

    private fun isIconFileWithBitmap(c1: Int, c2: Int, c3: Int, c4: Int, c5: Int, c6: Int): Boolean {
        return c1 == 0 && c2 == 0 // reserved

                && c3 == 1 // image

                && c4 == 0
                && c6 == 0 // c5 is number of images in this .ico container
    }

    @Throws(IOException::class)
    private fun readInt(inputStream: InputStream, noOfBytes: Int, bigEndian: Boolean): Int {
        var ret = 0
        var sv = if (bigEndian) (noOfBytes - 1) * 8 else 0
        val cnt = if (bigEndian) -8 else 8
        for (i in 0..noOfBytes - 1) {
            ret = ret or (inputStream.read() shl sv)
            sv += cnt
        }
        return ret
    }

    override fun toString(): String {
        return "MIME Type : $mimeType\t Width : $width\t Height : $height"
    }
}


