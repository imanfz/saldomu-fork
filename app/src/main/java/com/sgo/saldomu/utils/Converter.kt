package com.sgo.saldomu.utils

import java.nio.ByteOrder.LITTLE_ENDIAN
import android.R.attr.order
import java.nio.ByteBuffer
import java.nio.ByteOrder

import java.nio.ByteOrder.BIG_ENDIAN

class Converter {
    companion object {
        private val HEX_CHARS = "0123456789ABCDEF"
        fun hexStringToByteArray(data: String) : ByteArray {

            val result = ByteArray(data.length / 2)

            for (i in 0 until data.length step 2) {
                val firstIndex = HEX_CHARS.indexOf(data[i]);
                val secondIndex = HEX_CHARS.indexOf(data[i + 1]);

                val octet = firstIndex.shl(4).or(secondIndex)
                result.set(i.shr(1), octet.toByte())
            }

            return result
        }

        private val HEX_CHARS_ARRAY = "0123456789ABCDEF".toCharArray()
        fun toHex(byteArray: ByteArray) : String {
            val result = StringBuffer()

            byteArray.forEach {
                val octet = it.toInt()
                val firstIndex = (octet and 0xF0).ushr(4)
                val secondIndex = octet and 0x0F
                result.append(HEX_CHARS_ARRAY[firstIndex])
                result.append(HEX_CHARS_ARRAY[secondIndex])
            }

            return result.toString()
        }

        fun toLittleEndian(hex: String): Int {
            var ret = 0
            var hexLittleEndian = ""
            if (hex.length % 2 != 0) return ret
            var i = hex.length - 2
            while (i >= 0) {
                hexLittleEndian += hex.substring(i, i + 2)
                i -= 2
            }
            ret = Integer.parseInt(hexLittleEndian, 16)
            return ret
        }

        fun littleEndianToBigEndian(value: Int): Int {
            val b1 = value shr 0 and 0xff
            val b2 = value shr 8 and 0xff
            val b3 = value shr 16 and 0xff
            val b4 = value shr 24 and 0xff

            return b1 shl 24 or (b2 shl 16) or (b3 shl 8) or (b4 shl 0)
        }

        fun littleEndianToBigEndian2(value: Int): Int {
            val b1 = value shl 0 and 0xff
            val b2 = value shl 8 and 0xff
            val b3 = value shl 16 and 0xff
            val b4 = value shl 24 and 0xff

            return b1 shr 24 or (b2 shr 16) or (b3 shr 8) or (b4 shr 0)
        }

        fun INT_little_endian_TO_big_endian(i: Int): Int {
            return (i and 0xff shl 24) + (i and 0xff00 shl 8) + (i and 0xff0000 shr 8) + (i shr 24 and 0xff)
        }
//
        fun hexToLittleEndianHexString(hex: String) : String{

            // val hex = "5A109061" // mEditText.getText().toString()

            // Parse hex to int
            val value = Integer.parseInt(hex, 16)

            // Flip byte order using ByteBuffer
            val buffer = ByteBuffer.allocate(8)
            buffer.order(LITTLE_ENDIAN)
            buffer.asIntBuffer().put(value)
            buffer.order(BIG_ENDIAN)
            val flipped = buffer.asIntBuffer().get()

            // println("hex: 0x$hex")
            // println("flipped: $flipped")
            return flipped.toString()
        }


        fun intToLittleEndian1(numero: String): ByteArray {
            val bb = ByteBuffer.allocate(4)
            bb.order(ByteOrder.LITTLE_ENDIAN)
            bb.putInt(numero.toInt())
            return bb.array()
        }

// OR ...

        fun intToLittleEndian2(numero: Int): ByteArray {
            val b = ByteArray(4)
            b[0] = (numero and 0xFF).toByte()
            b[1] = (numero shr 8 and 0xFF).toByte()
            b[2] = (numero shr 16 and 0xFF).toByte()
            b[3] = (numero shr 24 and 0xFF).toByte()
            return b
        }
//
        fun intToByteArray(a: Int): ByteArray {
            val ret = ByteArray(4)
            ret[3] = (a and 0xFF).toByte()
            ret[2] = (a shr 8 and 0xFF).toByte()
            ret[1] = (a shr 16 and 0xFF).toByte()
            ret[0] = (a shr 24 and 0xFF).toByte()
            return ret
        }
//
//        fun BE_convert2Bytes(src: Int): ByteArray {
//            //an int is equivalent to 32 bits, 4 bytes
//            val tgt = ByteArray(4)
//            val mask = 0xff /* 0377 in octal*/
//
//            /* this works. which set is more elegant? */
//            /*
//	tgt[0] = (byte)(src >>> 24);
//	tgt[1] = (byte)((src << 8) >>> 24);
//	tgt[2] = (byte)((src << 16) >>> 24);
//	tgt[3] = (byte)((src << 24) >>> 24);
//	*/
//            tgt[0] = (src shr 24 and mask).toByte()
//            tgt[1] = (src shr 16 and mask).toByte()
//            tgt[2] = (src shr 8 and mask).toByte()
//            tgt[3] = (src and mask).toByte()
//             System.out.println("byteconvert" + tgt[0] + tgt[1] + tgt[2] + tgt[3]);
//            return tgt
//        }

        fun reverseHex(originalHex: String): String {
            // TODO: Validation that the length is even
            val lengthInBytes = originalHex.length / 2
            val chars = CharArray(lengthInBytes * 2)
            for (index in 0 until lengthInBytes) {
                val reversedIndex = lengthInBytes - 1 - index
                chars[reversedIndex * 2] = originalHex[index * 2]
                chars[reversedIndex * 2 + 1] = originalHex[index * 2 + 1]
            }
            return String(chars)
        }


    }
}