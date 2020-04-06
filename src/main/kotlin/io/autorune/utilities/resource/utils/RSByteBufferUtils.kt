package io.autorune.utilities.resource.utils

import java.nio.ByteBuffer

object RSByteBufferUtils
{

    fun readRS2String(buffer: ByteBuffer): String {
        val sb = StringBuilder()
        var b: Byte
        while (buffer.remaining() > 0) {
            b = buffer.get()
            if (b.toChar() == 0.toChar())
                break
            sb.append(b.toChar());
        }
        return sb.toString();
    }

}