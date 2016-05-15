package com.blueglacier.email;

import com.blueglacier.configuration.AppConfig;
import com.blueglacier.exception.HeaderNotFoundException;
import com.blueglacier.util.Common;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.message.HeaderImpl;
import org.apache.james.mime4j.message.MaximalBodyDescriptor;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Contains core logic to recreate an com.blueglacier.email as seen and perceived by a general user.
 */
public class Email {

	private Header header;
	private ArrayList<Attachment> attachments;
	private Attachment plainTextEmailBody;
	private Attachment htmlEmailBody;
	private Attachment calendarBody;
	private boolean attachmentReplacedInHtmlBody;
	private Stack<MultipartType> multipartStack;

	//Added to distinguish between com.blueglacier.email attached within another com.blueglacier.email case
	private Stack<EmailMessageType> emailMessageStack;
	private int decodedEmailSize;
	private int emailSize;
	
	public int getEmailSize() {
		return emailSize;
	}

	public int getDecodedEmailSize() {
		return decodedEmailSize;
	}

	Logger LOGGER = LoggerFactory.getLogger(Email.class);

	public Email(){
		this.header = new HeaderImpl();
		this.attachments = new ArrayList<Attachment>();
		this.attachmentReplacedInHtmlBody = false;
		this.multipartStack = new Stack<MultipartType>();
		this.emailMessageStack = new Stack<EmailMessageType>();
		this.decodedEmailSize = 0;
		this.emailSize = 0;
	}

	public Header getHeader() {
		return header;
	}
	
	public Attachment getPlainTextEmailBody(){		
		return plainTextEmailBody;
	}
	
	public void fillEmailContents(BodyDescriptor bd, InputStream is){
		LOGGER.info("mime part received");
		if(addPlainTextEmailBody(bd,is)){}
		else if(addHTMLEmailBody(bd,is)){}
		else if(addCalendar(bd,is)){}
		else{
			addAttachments(bd,is);
		}
	}

	private boolean addCalendar(BodyDescriptor bd, InputStream is) {
		boolean isBodySet = false;
		BodyDescriptor calendarBodyDescriptor = bd;
		if(calendarBody == null){
			if(isCalendarBody(calendarBodyDescriptor)){
				calendarBody = new CalendarBody(bd,is);
				isBodySet = true;
				LOGGER.info("Email calendar body identified");
			}
		}
		
		return isBodySet;
	}

	private boolean shouldIgnore(BodyDescriptor bd, InputStream is) {
		String attachmentName = Common.getAttachmentName(bd);
		boolean shouldIgnore = (attachmentName == null);
		if(shouldIgnore){
			LOGGER.info("ignored mime part for attachment consideration");
		}
		return shouldIgnore;
	}

	public Stack<MultipartType> getMultipartStack() {
		return multipartStack;
	}
	
	public Stack<EmailMessageType> getMessageStack() {
		return emailMessageStack;
	}
	
	public String getEmailSubject(){
		Field subjectField = header.getField("Subject");	
		if (subjectField != null) {
			Field decodedSubjectField = new CustomUnstructuredFieldImpl(subjectField,DecodeMonitor.SILENT);
			return ((CustomUnstructuredFieldImpl)decodedSubjectField).getValue();
		}
		return null;
	}
	
	public String getToEmailHeaderValue() {
		Field to = header.getField("To");
		if (to != null) {
			return to.getBody();
		}
		return null;
	}
	
	public String getCCEmailHeaderValue(){
		Field cc = header.getField("Cc");	
		if (cc != null) {
			return cc.getBody();
		}
		return null;
	}
	
	public String getFromEmailHeaderValue(){
		Field from = header.getField("From");	
		if (from != null) {			
			return from.getBody();
		}
		return null;
	}
	
	public String getJobIDEmailHeaderValue(){
		Field jobID = header.getField("X-jobID");	
		if (jobID != null) {
			return jobID.getBody();
		}
		return null;
	}
	
	public String getShardCodeEmailHeaderValue(){
		Field shardCode = header.getField("X-shardCode");	
		if (shardCode != null) {
			return shardCode.getBody();
		}
		return null;
	}	
	
	public String getEmailVirusInfectedHeaderValue(){
		Field antiVirusInfoField = header.getField("X-Amavis-Alert");	
		if (antiVirusInfoField != null) {
			return antiVirusInfoField.getBody();
		}
		return null;
	}
	
	public String getEmailSpamScoreHeaderValue(){
		Field spamScoreField = header.getField("X-Spam-Score");	
		if (spamScoreField != null) {
			return spamScoreField.getBody();
		}
		return null;
	}
	
	public String getEmailSpamStatusHeaderValue(){
		Field spamStatusField = header.getField("X-Spam-Status");	
		if (spamStatusField != null) {
			return spamStatusField.getBody();
		}
		return null;
	}
	
	public String getEmailPrinterIdentityHeaderValue(){
		Field printerIdentityField = header.getField("X-printerIdentifier");	
		if (printerIdentityField != null) {
			return printerIdentityField.getBody();
		}
		return null;
	}
	
	private void addAttachments(BodyDescriptor bd, InputStream is) {		
	   attachments.add(new EmailAttachment(bd,is));
	   LOGGER.info("Email attachment identified");
	}

	private void addAttachments(Attachment attachment) {
		attachments.add(attachment);
		LOGGER.info("Email attachment identified");
	}

	private boolean addHTMLEmailBody(BodyDescriptor bd, InputStream is) {
		boolean isBodySet = false;
		BodyDescriptor emailHTMLBodyDescriptor = bd;
		if(htmlEmailBody == null){
			if(isHTMLBody(emailHTMLBodyDescriptor)){
				htmlEmailBody = new HtmlEmailBody(bd,is);
				isBodySet = true;
				LOGGER.info("Email html body identified");
			}
		}else{
			if(isHTMLBody(emailHTMLBodyDescriptor)){
				if(multipartStack.peek().getBodyDescriptor().getMimeType().equalsIgnoreCase("multipart/mixed")){
					InputStream mainInputStream;
					try {
						mainInputStream = concatInputStream(is, htmlEmailBody.getIs());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					htmlEmailBody.setIs(mainInputStream);
				}else{
					addAttachments(new HtmlEmailBody(bd,is));
				}
				isBodySet = true;
			}
		}		
		return isBodySet;
	}

	private boolean isHTMLBody(BodyDescriptor emailHTMLBodyDescriptor) {
		String bodyName = Common.getAttachmentName(emailHTMLBodyDescriptor);
		return (emailHTMLBodyDescriptor.getMimeType().equalsIgnoreCase("text/html") && bodyName == null);
	}
	
	private boolean isCalendarBody(BodyDescriptor emailCalendarBodyDescriptor) {
		String bodyName = Common.getAttachmentName(emailCalendarBodyDescriptor);
		return (emailCalendarBodyDescriptor.getMimeType().equalsIgnoreCase("text/calendar") && bodyName == null);
	}

	private boolean addPlainTextEmailBody(BodyDescriptor bd, InputStream is) {
		boolean isBodySet = false;
		BodyDescriptor emailPlainBodyDescriptor = bd;
		if(plainTextEmailBody == null){
			if(isPlainTextBody(emailPlainBodyDescriptor)){
				plainTextEmailBody = new PlainTextEmailBody(bd,is);
				isBodySet = true;
				LOGGER.info("Email plain text body identified");
			}
		}else{
			if(isPlainTextBody(emailPlainBodyDescriptor)){
				if(multipartStack.peek().getBodyDescriptor().getMimeType().equalsIgnoreCase("multipart/mixed")){
					InputStream mainInputStream;
					try {
						mainInputStream = concatInputStream(is,plainTextEmailBody.getIs());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					plainTextEmailBody.setIs(mainInputStream);
				}else{
					addAttachments(new PlainTextEmailBody(bd,is));
				}
				isBodySet = true;
			}
		}		
		return isBodySet;
	}

	private boolean isPlainTextBody(BodyDescriptor emailPlainBodyDescriptor) {
		String bodyName = Common.getAttachmentName(emailPlainBodyDescriptor);
		return (emailPlainBodyDescriptor.getMimeType().equalsIgnoreCase("text/plain") && bodyName == null);
	}

	public List<Attachment> getAttachments() {		
		return attachments;
	}

	public Attachment getHTMLEmailBody() {		
		return htmlEmailBody;
	}
	
	public Attachment getCalendarBody() {		
		return calendarBody;
	}

	public void reArrangeEmail() {
		decodedEmailSize = setEmailSize();
		replaceInlineImageAttachmentsInHtmlBody();		
		removeUnidentifiedMimePartsForAttachment();
		emailSize = setEmailSize();
	}

	private int setEmailSize() {
		int emailSize = 0;
		
		if(getHTMLEmailBody() != null){
			emailSize += getHTMLEmailBody().getAttachmentSize();
		}
		if (getPlainTextEmailBody() != null) {
			emailSize += getPlainTextEmailBody().getAttachmentSize();
		}
		
		if (getCalendarBody() != null) {
			emailSize += getCalendarBody().getAttachmentSize();
		}
		
		for (Attachment attachment : getAttachments()) {
			emailSize += attachment.getAttachmentSize();
		}
		return emailSize;		
	}	

	private void removeUnidentifiedMimePartsForAttachment() {
		List<Attachment> removeList = new ArrayList<Attachment>();
		for (Attachment attachment : attachments) {
			if(shouldIgnore(attachment.bd, attachment.getIs())){				
				removeList.add(attachment);				
			}
		}
		removeAttachments(removeList);
	}

	private void replaceInlineImageAttachmentsInHtmlBody() {
		if(htmlEmailBody != null){
			String strHTMLBody = getHtmlBodyString();	

			List<Attachment> removalList = new ArrayList<Attachment>();

			for (int i = 0; i < attachments.size(); i++) {
				Attachment attachment = attachments.get(i);
				if(isImage(attachment)){
				String contentId = getAttachmentContentID(attachment);			
				strHTMLBody = replaceAttachmentInHtmlBody(strHTMLBody, removalList, attachment, contentId);
			}		
			}		

			removeAttachments(removalList);
			resetRecreatedHtmlBody(strHTMLBody);
			LOGGER.info("Finished embedding images in html");
		}
	}

	private String replaceAttachmentInHtmlBody(String strHTMLBody,
			List<Attachment> removalList, Attachment attachment,
			String contentId) {
		if (strHTMLBody.contains("cid:" + contentId)) {
			String base64EncodedAttachment = null;
			try {
				base64EncodedAttachment = Base64.encodeBase64String(IOUtils.toByteArray(attachment.getIs()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			strHTMLBody = strHTMLBody.replace("cid:" + contentId, "data:;base64," + base64EncodedAttachment);
			removalList.add(attachment);
			attachmentReplacedInHtmlBody = true;
		}
		return strHTMLBody;
	}

	private boolean isImage(Attachment attachment) {
		if((((MaximalBodyDescriptor)attachment.getBd()).getMediaType().equalsIgnoreCase("image")) || AppConfig.getInstance().isImageFormat(attachment.getAttachmentName())){
			return true;
		}
		return false;
	}

	public boolean isAttachmentReplacedInHtmlBody() {
		return attachmentReplacedInHtmlBody;
	}

	private void resetRecreatedHtmlBody(String strHTMLBody) {
		try {
			htmlEmailBody.setIs(new ByteArrayInputStream(strHTMLBody.getBytes(getCharSet())));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
	}
	}

	private void removeAttachments(List<Attachment> removalList) {
		attachments.removeAll(removalList);
	}

	private String getAttachmentContentID(Attachment attachment) {
		String contentId = ((MaximalBodyDescriptor) attachment.getBd()).getContentId();
		contentId = stripContentID(contentId);
		return contentId;
	}

	private String stripContentID(String contentId) {
		contentId = StringUtils.stripStart(contentId, "<");
		contentId = StringUtils.stripEnd(contentId, ">");
		return contentId;
	}

	private String getHtmlBodyString() {
		String strHTMLBody = "";
		try {
			String charSet = getCharSet();
			strHTMLBody = convertStreamToString(htmlEmailBody.getIs(),charSet);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return strHTMLBody;
	}

	private String getCharSet() {
		String charSet = Common.getFallbackCharset(htmlEmailBody.getBd().getCharset());
		return charSet;
	}

	
	private String convertStreamToString(InputStream is, String charSet) throws IOException {
		if (is != null) {
			return IOUtils.toString(is, charSet);
		} else {       
			return "";
		}
	}
	
	private InputStream concatInputStream(InputStream source, InputStream destination) throws IOException{		
		return new SequenceInputStream(destination, source);		
	}	
	
	public boolean getPrinterValidationCompletedHeaderValue() throws HeaderNotFoundException {
		Field printerValidationCompleted = header.getField("X-printerValidationCompleted");
		if (printerValidationCompleted != null) {
			return Boolean.valueOf(printerValidationCompleted.getBody());
		}
		throw new HeaderNotFoundException("Header X-printerValidationCompleted not found");
	}
	
	public String getEmailFilterLeavingTime() throws HeaderNotFoundException {
		Field emailFilteredTime = header.getField("X-emailFilterLeavingTime");	
		if (emailFilteredTime != null) {
			return emailFilteredTime.getBody();
		}
		throw new HeaderNotFoundException("Header X-emailFilterLeavingTime not found");
	}

    public String getEmailFilterArrivalTime() throws HeaderNotFoundException {
        Field emailFilterArrivalTime = header.getField("X-emailFilterArrivalTime");
        if (emailFilterArrivalTime != null) {
            return emailFilterArrivalTime.getBody();
        }
        throw new HeaderNotFoundException("Header X-emailFilterArrivalTime not found");
    }
}
