package tech.blueglacier.manager;

import tech.blueglacier.email.Email;
import tech.blueglacier.parser.CustomContentHandler;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.MimeConfig;

import java.io.IOException;
import java.io.InputStream;

public class EmailParseManager {

	InputStream rawEmailFile;
	ContentHandler contentHandler;
	
	public EmailParseManager(InputStream rawEmailFile) {
		this.rawEmailFile = rawEmailFile;
		contentHandler = new CustomContentHandler();
	}	

	public Email getParsedEmail() throws MimeException, IOException {
		
		MimeConfig mime4jParserConfig = MimeConfig.DEFAULT;
		BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();
		MimeStreamParser mime4jParser = new MimeStreamParser(mime4jParserConfig,DecodeMonitor.SILENT,bodyDescriptorBuilder);
		mime4jParser.setContentDecoding(true);
		mime4jParser.setContentHandler(contentHandler);		
		
		mime4jParser.parse(rawEmailFile);
		
		return ((CustomContentHandler)contentHandler).getEmail();
	}
}
