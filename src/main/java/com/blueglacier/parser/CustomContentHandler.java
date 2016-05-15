package com.blueglacier.parser;

import com.blueglacier.email.Email;
import com.blueglacier.email.EmailMessageType;
import com.blueglacier.email.EmailMessageType.EmailMessageTypeHierarchy;
import com.blueglacier.email.MultipartType;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;

import java.io.IOException;
import java.io.InputStream;

public class CustomContentHandler extends AbstractContentHandler {
	
	private Email email;

	public CustomContentHandler(){
		this.email = new Email();
	}	
	
	public Email getEmail() {
		return email;
	}

	@Override
    public void field(Field field) throws MimeException {
		email.getHeader().addField(field);
    }	
	
	@Override
	public void body(BodyDescriptor bd, InputStream is) throws MimeException, IOException {
		// Gracefully switiching off the case of com.blueglacier.email attached within an com.blueglacier.email
		if(email.getMessageStack().peek().getEmailMessageTypeHierarchy() == EmailMessageTypeHierarchy.parent){
		email.fillEmailContents(bd, is);
	}
	}
	
	@Override
	public void startMessage() throws MimeException {
		if(email.getMessageStack().empty()){
			email.getMessageStack().push(new EmailMessageType(EmailMessageTypeHierarchy.parent));
		}else{
			email.getMessageStack().push(new EmailMessageType(EmailMessageTypeHierarchy.child));
		}
	}
	
	@Override
	public void endMessage(){
		if(email.getMessageStack().peek().getEmailMessageTypeHierarchy() == EmailMessageTypeHierarchy.parent){
		email.reArrangeEmail();
	}
		email.getMessageStack().pop();
	}
	
	public void endMultipart() throws MimeException {
		email.getMultipartStack().pop();
	}

	public void startMultipart(BodyDescriptor bd) throws MimeException {
		email.getMultipartStack().push(new MultipartType(bd));
	}
}
