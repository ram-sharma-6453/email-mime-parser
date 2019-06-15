package tech.blueglacier.storage;

import org.apache.james.mime4j.storage.Storage;
import org.apache.james.mime4j.storage.StorageOutputStream;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.util.ByteArrayBuffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

/**
 * A {@link StorageProvider} that keeps small amounts of data in memory and
 * writes the remainder to another <code>StorageProvider</code> (the back-end)
 * if a certain threshold size gets exceeded.
 * <p>
 * Example usage:
 *
 * <pre>
 * StorageProvider tempStore = new TempFileStorageProvider();
 * StorageProvider provider = new ThresholdStorageProvider(tempStore, 4096);
 * DefaultStorageProvider.setInstance(provider);
 * </pre>
 */
public class ThresholdStorageProvider extends AbstractStorageProvider {

    private final StorageProvider backend;
    private final int thresholdSize;

    /**
     * Creates a new <code>ThresholdStorageProvider</code> for the given
     * back-end using a threshold size of 2048 bytes.
     */
    public ThresholdStorageProvider(StorageProvider backend) {
        this(backend, 2048);
    }

    /**
     * Creates a new <code>ThresholdStorageProvider</code> for the given
     * back-end and threshold size.
     *
     * @param backend       used to store the remainder of the data if the threshold size
     *                      gets exceeded.
     * @param thresholdSize determines how much bytes are kept in memory before that
     *                      back-end storage provider is used to store the remainder of
     *                      the data.
     */
    public ThresholdStorageProvider(StorageProvider backend, int thresholdSize) {
        if (backend == null || thresholdSize < 1) {
            throw new IllegalArgumentException();
        }
        this.backend = backend;
        this.thresholdSize = thresholdSize;
    }

    public StorageOutputStream createStorageOutputStream() {
        return new ThresholdStorageOutputStream();
    }

    private final class ThresholdStorageOutputStream extends StorageOutputStream {

        private final ByteArrayBuffer head;
        private StorageOutputStream tail;

        public ThresholdStorageOutputStream() {
            final int bufferSize = Math.min(thresholdSize, 1024);
            head = new ByteArrayBuffer(bufferSize);
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (tail != null) {
                tail.close();
            }
        }

        @Override
        protected void write0(byte[] buffer, int offset, int length) throws IOException {
            int remainingHeadSize = thresholdSize - head.length();
            if (remainingHeadSize > 0) {
                int n = Math.min(remainingHeadSize, length);
                head.append(buffer, offset, n);
                offset += n;
                length -= n;
            }
            if (length > 0) {
                if (tail == null) {
                    tail = backend.createStorageOutputStream();
                }
                tail.write(buffer, offset, length);
            }
        }

        @Override
        protected Storage toStorage0() throws IOException {
            if (tail == null) {
                return new MemoryStorageProvider.MemoryStorage(head.buffer(), head.length());
            }
            return new ThresholdStorage(head.buffer(), head.length(), tail.toStorage());
        }
    }

    private static final class ThresholdStorage implements Storage {

        private byte[] head;
        private final int headLen;
        private Storage tail;

        public ThresholdStorage(byte[] head, int headLen, Storage tail) {
            this.head = head;
            this.headLen = headLen;
            this.tail = tail;
        }

        public void delete() {
            if (head != null) {
                head = null;
                tail.delete();
                tail = null;
            }
        }

        public InputStream getInputStream() throws IOException {
            if (head == null) {
                throw new IllegalStateException("storage has been deleted");
            }

            InputStream headStream = new ByteArrayInputStream(head, 0, headLen);
            InputStream tailStream = tail.getInputStream();
            return new SequenceInputStream(headStream, tailStream);
        }
    }
}
