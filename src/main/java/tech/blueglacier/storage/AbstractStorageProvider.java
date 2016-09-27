package tech.blueglacier.storage;

import tech.blueglacier.codec.CodecUtil;
import org.apache.james.mime4j.storage.Storage;
import org.apache.james.mime4j.storage.StorageOutputStream;
import org.apache.james.mime4j.storage.StorageProvider;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractStorageProvider implements StorageProvider {

	/**
     * Sole constructor.
     */
    protected AbstractStorageProvider() {
    	totalBytesTransffered = 0;
    }

    /**
     * This implementation creates a {@link StorageOutputStream} by calling
     * {@link StorageProvider#createStorageOutputStream() createStorageOutputStream()}
     * and copies the content of the given input stream to that output stream.
     * It then calls {@link StorageOutputStream#toStorage()} on the output
     * stream and returns this object.
     *
     * @param in
     *            stream containing the data to store.
     * @return a {@link Storage} instance that can be used to retrieve the
     *         stored content.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public final Storage store(InputStream in) throws IOException {
        StorageOutputStream out = createStorageOutputStream();
        totalBytesTransffered = CodecUtil.copy(in, out);
        return out.toStorage();
    }
    
    private int totalBytesTransffered;

	public int getTotalBytesTransffered() {
		return totalBytesTransffered;
	}

}
