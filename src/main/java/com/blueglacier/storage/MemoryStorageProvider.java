package com.blueglacier.storage;

import org.apache.james.mime4j.storage.Storage;
import org.apache.james.mime4j.storage.StorageOutputStream;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.util.ByteArrayBuffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link StorageProvider} that stores the data entirely in memory.
 * <p>
 * Example usage:
 *
 * <pre>
 * StorageProvider provider = new MemoryStorageProvider();
 * DefaultStorageProvider.setInstance(provider);
 * </pre>
 */
public class MemoryStorageProvider extends AbstractStorageProvider {

    /**
     * Creates a new <code>MemoryStorageProvider</code>.
     */
    public MemoryStorageProvider() {
    }

    public StorageOutputStream createStorageOutputStream() {
        return new MemoryStorageOutputStream();
    }

    private static final class MemoryStorageOutputStream extends
            StorageOutputStream {
        ByteArrayBuffer bab = new ByteArrayBuffer(1024);

        @Override
        protected void write0(byte[] buffer, int offset, int length)
                throws IOException {
            bab.append(buffer, offset, length);
        }

        @Override
        protected Storage toStorage0() throws IOException {
            return new MemoryStorage(bab.buffer(), bab.length());
        }
    }

    static final class MemoryStorage implements Storage {
        private byte[] data;
        private final int count;

        public MemoryStorage(byte[] data, int count) {
            this.data = data;
            this.count = count;
        }

        public InputStream getInputStream() throws IOException {
            if (data == null)
                throw new IllegalStateException("com.blueglacier.storage has been deleted");

            return new ByteArrayInputStream(data, 0, count);
        }

        public void delete() {
            data = null;
        }
    }

}
