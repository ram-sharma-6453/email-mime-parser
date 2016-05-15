package com.blueglacier.email;

import org.apache.james.mime4j.stream.BodyDescriptor;

import java.io.InputStream;

public class CalendarBody extends Attachment {

	public CalendarBody(BodyDescriptor bd, InputStream is) {
		super(bd, is);
	}

	@Override
	public String getAttachmentName() {
		return "calendarBody.ics";
	}

}
