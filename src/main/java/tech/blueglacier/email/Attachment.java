package tech.blueglacier.email;

import org.apache.james.mime4j.storage.DefaultStorageProvider;
import tech.blueglacier.storage.AbstractStorageProvider;
import org.apache.james.mime4j.storage.Storage;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.stream.BodyDescriptor;

import java.io.IOException;
import java.io.InputStream;

public abstract class Attachment {

    protected BodyDescriptor bd;

    public abstract String getAttachmentName();

    private InputStream is;

    public BodyDescriptor getBd() {
        return bd;
    }

    private Storage storage;

    public InputStream getIs() {
        try {
            is = storage.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return is;
    }

    private int attachmentSize;

    public int getAttachmentSize() {
        return attachmentSize;
    }

    public void setIs(InputStream is) {
        StorageProvider storageProvider = DefaultStorageProvider.getInstance();
        try {
            storage = storageProvider.store(is);
            if (storageProvider instanceof AbstractStorageProvider) {
                attachmentSize = ((AbstractStorageProvider) storageProvider).getTotalBytesTransffered();
            } else {
                attachmentSize = 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Attachment(BodyDescriptor bd, InputStream is) {
        this.bd = bd;
        attachmentSize = 0;
        setIs(is);
    }

}