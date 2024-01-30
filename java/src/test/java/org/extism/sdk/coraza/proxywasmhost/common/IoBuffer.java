package org.extism.sdk.coraza.proxywasmhost.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class IoBuffer {
    private byte[] buf;

    public IoBuffer(byte[] buf) {

        if(buf == null) {
            this.buf = new byte[0];
        } else {
            this.buf = buf;
        }
    }

    public IoBuffer(String buf) {
        this.buf = buf.getBytes(StandardCharsets.UTF_8);
    }

    // Len returns the number of bytes of the unread portion of the buffer;
    // b.Len() == len(b.Bytes()).
    public int length() {
        return buf.length;
    }

    // Bytes returns all bytes from buffer, without draining any buffered data.
    // It can be used to get fixed-length content, such as headers, body.
    // Note: do not change content in return bytes, use write instead
    public byte[] bytes() {
        return buf;
    }

    // Write appends the contents of p to the buffer, growing the buffer as
    // needed. The return value n is the length of p; err is always nil. If the
    // buffer becomes too large, Write will panic with ErrTooLarge.
    public int write(byte[] p) throws IOException { // (n int, err error)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(buf);
        outputStream.write(p);

        p = outputStream.toByteArray();

        return p.length;
    }

    // Drain drains a offset length of bytes in buffer.
    // It can be used with Bytes(), after consuming a fixed-length of data
    public void drain(int offset, int maxSize) {
        if (offset > buf.length) {
            return;
        }

        if (maxSize == -1)
            buf = Arrays.copyOfRange(buf, offset, buf.length);
        else
            buf = Arrays.copyOfRange(buf, offset, offset+maxSize);
    }
}
