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

        fun hexToLittleEndianHexString(hex: String) : String{

            // val hex = "5A109061" // mEditText.getText().toString()

            // Parse hex to int
            val value = Integer.parseInt(hex, 16)

            // Flip byte order using ByteBuffer
            val buffer = ByteBuffer.allocate(4)
            buffer.order(BIG_ENDIAN)
            buffer.asIntBuffer().put(value)
            buffer.order(LITTLE_ENDIAN)
            val flipped = buffer.asIntBuffer().get()

            // println("hex: 0x$hex")
            // println("flipped: $flipped")
            return flipped.toString()
        }
    }
}