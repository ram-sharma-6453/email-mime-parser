package tech.blueglacier.email;

import org.apache.james.mime4j.stream.BodyDescriptor;

import java.io.InputStream;


public class HtmlEmailBody extends Attachment {

	public HtmlEmailBody(BodyDescriptor bd, InputStream is) {
		super(bd, is);
	}

	@Override
	public String getAttachmentName() {
		return "emailBody.html";
	}

}
