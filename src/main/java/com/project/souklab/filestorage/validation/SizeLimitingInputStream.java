package com.project.souklab.filestorage.validation;

import com.project.souklab.filestorage.exception.FileTooLargeException;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link InputStream} decorator that counts bytes read and throws {@link FileTooLargeException}
 * as soon as the number of bytes read exceeds the configured maximum limit.
 *
 * <p>Guarantees that a client declaring a small Content-Length cannot cause unbounded memory consumption
 * or disk storage by streaming a larger payload than allowed.
 */
public class SizeLimitingInputStream extends FilterInputStream {

    private final long maxBytes;
    private long bytesRead = 0;
    private long markedBytes = 0;

    public SizeLimitingInputStream(InputStream in, long maxBytes) {
        super(in);
        this.maxBytes = maxBytes;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            trackBytes(1);
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read != -1) {
            trackBytes(read);
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = super.skip(n);
        if (skipped > 0) {
            trackBytes(skipped);
        }
        return skipped;
    }

    private void trackBytes(long count) {
        bytesRead += count;
        if (bytesRead > maxBytes) {
            throw new FileTooLargeException(bytesRead, maxBytes);
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        markedBytes = bytesRead;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        bytesRead = markedBytes;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public long getMaxBytes() {
        return maxBytes;
    }
}
