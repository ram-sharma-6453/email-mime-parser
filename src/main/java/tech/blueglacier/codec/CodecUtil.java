package tech.blueglacier.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CodecUtil {
	static final int DEFAULT_ENCODING_BUFFER_SIZE = 1024;

	/**
	 * Copies the contents of one stream to the other.
	 * @param in not null
	 * @param out not null
	 * @return total bytes transferred
	 * @throws IOException
	 */
	public static int copy(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buffer = new byte[DEFAULT_ENCODING_BUFFER_SIZE];
		int inputLength;
		int totalBytesTransferred = 0;
		while (-1 != (inputLength = in.read(buffer))) {
			out.write(buffer, 0, inputLength);
			totalBytesTransferred += inputLength;
		}
		return totalBytesTransferred;
	}
}
