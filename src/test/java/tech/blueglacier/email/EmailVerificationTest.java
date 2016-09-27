package tech.blueglacier.email;

import tech.blueglacier.parser.CustomContentHandler;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URL;

public class EmailVerificationTest {

    @Test
    public void getAndVerifyEmailHeader() throws MimeException, IOException {
        assertGettingHeader("From");
    }

    @Test
    public void testGettingEmailSubject() throws MimeException, IOException {
        assertEmailSubject();
    }

    private void assertEmailSubject() throws MimeException, IOException {
        Email email = getParsedSimpleMailForSubject();
        Assert.assertEquals("Test email for header", email.getEmailSubject());
    }

    private Email getParsedSimpleMailForSubject() throws MimeException, IOException {
        ContentHandler contentHandler = getContentHandler();
        Email email = getParsedEmail("simpleEmailForSubjectVerification.eml", contentHandler);
        return email;
    }

    @Test
    public void assertEmptyEmailSubject() throws MimeException, IOException {
        Email email = getParsedSimpleMailWithEmptySubject();
        Assert.assertEquals(email.getEmailSubject(), "");
    }

    private Email getParsedSimpleMailWithEmptySubject() throws MimeException, IOException {
        ContentHandler contentHandler = getContentHandler();
        Email email = getParsedEmail("simpleEmailWithEmptySubject.eml", contentHandler);
        return email;
    }

    @Test
    public void assertNoEmailSubjectHeader() throws MimeException, IOException {
        Email email = getParsedSimpleMailWithNoSubjectHeader();
        Assert.assertEquals(email.getEmailSubject(), null);
    }

    private Email getParsedSimpleMailWithNoSubjectHeader() throws MimeException, IOException {
        ContentHandler contentHandler = getContentHandler();
        Email email = getParsedEmail("simpleEmailWithNoSubjectHeader.eml", contentHandler);
        return email;
    }

    @Test
    public void assertToEmailHeader() throws MimeException, IOException {
        Email email = getParsedSimpleMail();
        Assert.assertEquals(email.getToEmailHeaderValue(), "ram.sharma.6453@gmail.com");
    }

    private Email getParsedSimpleMail() throws MimeException, IOException {
        ContentHandler contentHandler = getContentHandler();
        Email email = getParsedEmail("simpleEmailForHeaderVerification.eml", contentHandler);
        return email;
    }

    @Test
    public void assertCcEmailHeader() throws MimeException, IOException {
        Email email = getParsedSimpleMail();
        Assert.assertEquals(email.getCCEmailHeaderValue(), "\"Sharma, Ram\" <ram.sharma.6453@aol.com>, Ram Sharma <ram.sharma.6453.ait@gmail.com>");
    }

    @Test
    public void assertFromEmailHeader() throws MimeException, IOException {
        Email email = getParsedSimpleMail();
        Assert.assertEquals(email.getFromEmailHeaderValue(), "Ram Sharma <ram.sharma.6453@gmail.com>");
    }

    @Test
    public void assertMissingToHeader() throws MimeException, IOException {
        Email email = getParsedMailWithMissingHeaders();
        Assert.assertEquals(email.getToEmailHeaderValue(), null);
    }

    @Test
    public void assertMissingCcHeader() throws MimeException, IOException {
        Email email = getParsedMailWithMissingHeaders();
        Assert.assertEquals(email.getCCEmailHeaderValue(), null);
    }

    @Test
    public void assertMissingFromHeader() throws MimeException, IOException {
        Email email = getParsedMailWithMissingHeaders();
        Assert.assertEquals(email.getFromEmailHeaderValue(), null);
    }


    private Email getParsedMailWithMissingHeaders() throws MimeException, IOException {
        ContentHandler contentHandler = getContentHandler();
        Email email = getParsedEmail("simpleEmailWithMissingHeaders.eml", contentHandler);
        return email;
    }


    private void assertGettingHeader(String header)
            throws MimeException, IOException {

        Email email = getParsedSimpleGmail();
        Header parsedHeader = email.getHeader();

        Field from = parsedHeader.getField(header);
        Assert.assertEquals(header, from.getName());
    }

    private Email getParsedSimpleGmail() throws
            MimeException, IOException {
        ContentHandler basicGmailContentHandler = getContentHandler();
        Email email = getParsedEmail("gmailMessage.eml",
                basicGmailContentHandler);
        return email;
    }

    private ContentHandler getContentHandler() {
        return new CustomContentHandler();
    }

    private Email getParsedEmail(String messageFileName,
                                 ContentHandler contentHandler) throws MimeException, IOException {
        parseEmail(messageFileName, contentHandler);
        Email email = ((CustomContentHandler) contentHandler).getEmail();
        return email;
    }

    private void parseEmail(String messageFileName,
                            ContentHandler contentHandler) throws
            MimeException, IOException {

        MimeConfig mime4jParserConfig = new MimeConfig();

        BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();
        MimeStreamParser mime4jParser = new MimeStreamParser(mime4jParserConfig, DecodeMonitor.SILENT, bodyDescriptorBuilder);

        mime4jParser.setContentDecoding(true);
        mime4jParser.setContentHandler(contentHandler);

        URL url = this.getClass().getClassLoader().getResource(messageFileName);

        InputStream mailIn = new FileInputStream(new File(url.getFile()));
        mime4jParser.parse(mailIn);

    }
}