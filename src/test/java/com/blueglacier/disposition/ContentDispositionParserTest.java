package com.blueglacier.disposition;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.james.mime4j.MimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.mail.internet.ParseException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ContentDispositionParserTest {

	private String fileName;

	@Test
	public void getContentDispositionFileName() throws ParseException, MimeException, IOException{		
		setDispositionDecodedFilename().assertDecodedFileString(getPlainTextCheckSum());		
	}

	private String getPlainTextCheckSum() throws IOException {				
		return "a24f8a3f3da2801b3d81a94f842580799cfccd72";
	}
	
	private String generateCheckSum(String plainText) throws IOException {
		return DigestUtils.shaHex(plainText);
	}

	private void assertDecodedFileString(String plainFileNameChecksum) throws IOException {
		Assert.assertEquals(generateCheckSum(fileName),plainFileNameChecksum);
	}

	private ContentDispositionParserTest setDispositionDecodedFilename() throws ParseException, MimeException {
		fileName = ContentDispositionDecoder.decodeDispositionFileName(getEncodedContentDispositionMap());
		return this;
	}

	private Map<String, String> getEncodedContentDispositionMap() {
		Map<String, String> encodedDispositionMap = new HashMap<String, String>();
		
		encodedDispositionMap.put("filename*3*","%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8.txt");
		encodedDispositionMap.put("filename*1*","%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD");
		encodedDispositionMap.put("filename*0*","utf-8''%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8");
		encodedDispositionMap.put("filename*2*","%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8%E5%A5%BD%E6%82%A8");
		
		return encodedDispositionMap;
	}	
}
