package tech.blueglacier.codec;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URL;

public class CodecUtilTest {

	private FileInputStream in;
	private URL url;
	private FileOutputStream out;

	@Test
	public void testTotalBytesTransffered() throws IOException {		
		whenInputStreamIs("gmailMessage.eml").andOutputStreamIs("CopiedgmailMessage.eml").assertTotalBytesTransfferedAre(1171);
	}

	private void assertTotalBytesTransfferedAre(int expectedTotalBytesTransffered) throws IOException {
		Assert.assertEquals(CodecUtil.copy(in, out), expectedTotalBytesTransffered);		
	}

	private CodecUtilTest andOutputStreamIs(String string) throws FileNotFoundException {		
		String outFile = url.getFile().replace("gmailMessage.eml","CopiedgmailMessage.eml");
		out = new FileOutputStream(outFile);
		return this;
	}

	private CodecUtilTest whenInputStreamIs(String fileResource) throws FileNotFoundException {
		url = this.getClass().getClassLoader().getResource(fileResource);
		in = new FileInputStream(new File(url.getFile()));		
		return this;
	}
}
