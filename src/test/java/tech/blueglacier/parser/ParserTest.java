package tech.blueglacier.parser;

import org.apache.james.mime4j.storage.DefaultStorageProvider;
import org.testng.annotations.BeforeClass;
import tech.blueglacier.email.Attachment;
import tech.blueglacier.email.Email;
import tech.blueglacier.storage.MemoryStorageProvider;
import tech.blueglacier.storage.TempFileStorageProvider;
import tech.blueglacier.storage.ThresholdStorageProvider;
import tech.blueglacier.util.MimeWordDecoder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URL;
import java.util.*;

public class ParserTest {

	@BeforeClass
	public static void setUp() {
		DefaultStorageProvider.setInstance(new MemoryStorageProvider());
	}

	private ContentHandler getContentHandler() {
		return new CustomContentHandler();
	}

	private void parseEmail(String messageFileName, ContentHandler contentHandler) throws FileNotFoundException,
			MimeException, IOException {

		MimeConfig mime4jParserConfig = MimeConfig.copy(MimeConfig.DEFAULT).setMaxLineLen(-1).setMaxHeaderLen(-1).build();
		BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();
		MimeStreamParser mime4jParser = new MimeStreamParser(mime4jParserConfig,DecodeMonitor.SILENT,bodyDescriptorBuilder);
		mime4jParser.setContentDecoding(true);
		mime4jParser.setContentHandler(contentHandler);

		URL url = this.getClass().getClassLoader().getResource(messageFileName);

		InputStream mailIn = new FileInputStream(new File(url.getFile()));
		mime4jParser.parse(mailIn);

	}

	private Email getParsedSimpleGmail() throws
			MimeException, IOException {
		ContentHandler basicGmailContentHandler = getContentHandler();
		Email email = getParsedEmail("gmailMessage.eml",basicGmailContentHandler);
		return email;
	}

	private Email getParsedEmail(String messageFileName, ContentHandler contentHandler) throws  MimeException, IOException {
		parseEmail(messageFileName, contentHandler);
		Email email = ((CustomContentHandler) contentHandler).getEmail();
		return email;
	}

	@Test
	public void getAndVerifyEmailAttachmentFileName() throws  MimeException, IOException {
		assertEmailAttachmentFileName("JMXParameters.txt");
	}

	private void assertEmailAttachmentFileName(String fileName) throws  MimeException, IOException {
		Email email = getParsedSimpleGmail();
		List<Attachment> attachments =  email.getAttachments();

		Attachment attachment = attachments.get(0);

		String parsedEmailFileName = attachment.getAttachmentName();
		Assert.assertEquals(fileName,parsedEmailFileName);
	}

	@Test
	public void verifyEmailAttachmentContents() throws IOException, MimeException{
		assertEmailAttachmentContents();
	}

	private void assertEmailAttachmentContents() throws IOException, MimeException {
		Email email = getParsedSimpleGmail();
		List<Attachment> attachments =  email.getAttachments();

		Attachment attachment = attachments.get(0);
		String fileContentChecksum = generateCheckSum(attachment.getIs());

		Assert.assertEquals(fileContentChecksum,"306c36617f39004e656974fd383cfe36f3cfe090");

	}

	@Test
	public void getEmailPlainTextBody() throws IOException, MimeException{
		assertEmailPlainTextBodyContents();
	}

	private void assertEmailPlainTextBodyContents() throws IOException, MimeException {
		Email email = getParsedSimpleGmail();
		String bodyContentChecksum = generateCheckSum(email.getPlainTextEmailBody().getIs());
		Assert.assertEquals(bodyContentChecksum, "d95e0afb0868bd495d347cee4f199a8a1ad265b4");

	}

	private String generateCheckSum(InputStream in) throws IOException {
		return DigestUtils.shaHex(in);
	}

	private String generateCheckSum(String filename) throws IOException {
		return DigestUtils.shaHex(filename);
	}

	@Test
	public void getEmailHTMLBody() throws IOException, MimeException{
		assertEmailHTMLBodyContents();
	}

	private void assertEmailHTMLBodyContents() throws IOException, MimeException {
		Email email = getParsedSimpleGmail();
		String htmlBodyChecksum = generateCheckSum(email.getHTMLEmailBody().getIs());
		Assert.assertEquals(htmlBodyChecksum, "dfe85ca4b15e94f8a06ac17f8a4f2b06b14b6a1b");
	}

	@Test
	public void assertPlainTextEmailBodyFileName() throws  MimeException, IOException{
		Email email = getParsedSimpleGmail();
		Attachment plainTextBody = email.getPlainTextEmailBody();
		Assert.assertEquals(plainTextBody.getAttachmentName(), "emailBody.txt");
	}

	@Test
	public void assertHTMLEmailBodyFileName() throws  MimeException, IOException{
		Email email = getParsedSimpleGmail();
		Attachment htmlBody = email.getHTMLEmailBody();
		Assert.assertEquals(htmlBody.getAttachmentName(), "emailBody.html");
	}

	@Test
	public void testInlineAttachmentMergingInEmailBody() throws MimeException, IOException{
		Email email = getParsedEmailWithInlineAttachment();
		assertInlineAttachmentMerge(email);
		assertNumberOfAttachmnetInEmail(email);
	}

	private Email getParsedEmailWithInlineAttachment()
			throws  MimeException, IOException {
		ContentHandler inlineMessageContentHandler = getContentHandler();
		Email email = getParsedEmail("inlineMessage.eml",inlineMessageContentHandler);
		return email;
	}

	@Test
	public void testAttachmentsSavedToStorage() throws IOException, MimeException {
		TempFileStorageProvider storageProvider = new TempFileStorageProvider(new File(System.getProperty("java.io.tmpdir")));
		DefaultStorageProvider.setInstance(new ThresholdStorageProvider(storageProvider, 1024));
		Email email = getParsedEmailWithLargeImageAttachments();
		Assert.assertNull(email.getPlainTextEmailBody().getIs());
		Assert.assertTrue(email.getPlainTextEmailBody().getAttachmentSize() <= 1024);
		Assert.assertEquals(SequenceInputStream.class, email.getHTMLEmailBody().getIs().getClass());
        Assert.assertTrue(email.getHTMLEmailBody().getAttachmentSize() > 1024);
        Assert.assertEquals(3, email.getAttachments().size());
        for (Attachment attachment: email.getAttachments()) {
            Assert.assertTrue((attachment.getAttachmentSize() <= 1024 && attachment.getIs() == null) ||
                    (attachment.getAttachmentSize() > 1024 && attachment.getIs() instanceof SequenceInputStream));
        }
	}

	@Test
	public void testEmailWithImageContentType() throws MimeException, IOException{
		Email email = getEmailWithImageContentType();
		assertEmailWithImageContentMerge(email);
		assertNumberOfAttachmnetInEmail(email);
	}

	private Email getEmailWithImageContentType()
			throws  MimeException, IOException {
		ContentHandler inlineMessageContentHandler = getContentHandler();
		Email email = getParsedEmail("emailWithImageContentType.eml",inlineMessageContentHandler);
		return email;
	}

	private void assertEmailWithImageContentMerge(Email email) throws IOException {
		String htmlBodyChecksum = generateCheckSum(email.getHTMLEmailBody().getIs());
		Assert.assertEquals(htmlBodyChecksum,"2b9092245d581e97ba587c897f6cd89f41c15fde");
	}

	private void assertNumberOfAttachmnetInEmail(Email email) {
		Assert.assertEquals(email.getAttachments().size(), 0);
	}

	private void assertInlineAttachmentMerge(Email email) throws IOException {
		String htmlBodyChecksum = generateCheckSum(email.getHTMLEmailBody().getIs());
		Assert.assertEquals(htmlBodyChecksum,"0620b63bb72deed17f069409b52d7969f6c3e7d8");
	}

	@Test
	public void assertHtmlBodyForLargeInlineImages() throws IOException, MimeException{
		Email email = getParsedEmailWithLargeImageAttachments();
		Attachment htmlBody = email.getHTMLEmailBody();

		assertHtmlIsCorrectlyParsed(htmlBody);
	}

	private Email getParsedEmailWithLargeImageAttachments()
			throws  MimeException, IOException {
		ContentHandler imageAttachmentContentHandler = getContentHandler();
		Email email = getParsedEmail("multipleLargeImage.eml",imageAttachmentContentHandler);
		return email;
	}

	@Test
	public void assertEmailAttachmentRenderingForLargeAttachedImages() throws  MimeException, IOException{

		Email email = getParsedEmailWithLargeImageAttachments();

		List<Attachment> attachments = email.getAttachments();
		Map<String, String> fileNameCheckSumPair = generateFileNameCheckSumPair();

		for (Attachment attachment : attachments) {
			String actualFileChecksum = generateCheckSum(attachment.getIs());

			Assert.assertEquals(actualFileChecksum, getExpectedFileCheckSum(fileNameCheckSumPair, attachment));
		}
	}

	private String getExpectedFileCheckSum(
			Map<String, String> fileNameCheckSumPair, Attachment attachment) {
		return fileNameCheckSumPair.get(attachment.getAttachmentName());
	}

	private Map<String, String> generateFileNameCheckSumPair() {
		Map<String, String> fileNameCheckSumPair = new HashMap<String, String>();
		fileNameCheckSumPair.put("abstract_0011.jpg", "2e2cc0e126c4dd0544110e32511703bb418bd709");
		fileNameCheckSumPair.put("abstract_0037.jpg","f9fd79c35cb8425b21ee1f2d28b88658fc186760");
		fileNameCheckSumPair.put("computer_0047.jpg","cffde636ee9920411e9fc4dbffb784145d10c2e3");
		return fileNameCheckSumPair;
	}

	private void assertHtmlIsCorrectlyParsed(Attachment htmlBody) throws IOException {
		String htmlBodyChecksum = generateCheckSum(htmlBody.getIs());
		Assert.assertEquals(htmlBodyChecksum,"7ec72df5d3e0e68ae7f53f84f8ecfdf4296bd7e9");
	}

	@Test
	public void validateChineseFileNameAttachmentIsGettingDecoded() throws  MimeException, IOException {
		Email email = getParsedEmailWithChinesFileAttachments();
		List<Attachment> attachments = email.getAttachments();
		StringBuilder strBuild = new StringBuilder();
		for (Attachment emailAttachment : attachments) {
			strBuild.append(emailAttachment.getAttachmentName());
		}
		Assert.assertEquals(strBuild.toString(), getExpectedDecodedFileNames());
	}

	public String getExpectedDecodedFileNames(){
//		Check RFC 2047 for non ASCII character encoding
		String mimeWordEncodedString = "=?gb2312?B?ztLKx9bQufrIyy50eHQ=?="
				+ "\r\n " + "=?gb2312?B?ztKyu8rH1tCH+LmyrmEudHh0?=";
		return MimeWordDecoder.decodeEncodedWords(mimeWordEncodedString,DecodeMonitor.SILENT);

	}

	private Email getParsedEmailWithChinesFileAttachments() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("chineseFileAttachmentsFromOutLook.eml",contentHandler);
		return email;
	}

	@Test
	public void validateChineseSubjectIsGettingDecoded() throws  MimeException, IOException{
		Email email = getParsedEmailWithChinesEmailSubject();
		String actual = email.getEmailSubject();
		String expected = getExpectedDecodedSubject();
		Assert.assertEquals(actual, expected);
	}

	private Email getParsedEmailWithChinesEmailSubject() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("chineseEmailSubject.eml",contentHandler);
		return email;
	}

	private String getExpectedDecodedSubject() throws IOException {
		String messageFileName = "results/expectedChineseSubjectString.txt";
		URL url = this.getClass().getClassLoader().getResource(messageFileName);
		InputStream chinesSubjectInStream = new FileInputStream(new File(url.getFile()));
		String mimeWordEncodedString = convertStreamToString(chinesSubjectInStream, "GB18030");
		return MimeWordDecoder.decodeEncodedWords(mimeWordEncodedString,DecodeMonitor.SILENT);
	}

	@Test
	public void assertEmailSize() throws  MimeException, IOException{
		Email email = getParsedEmailWithLargeImageAttachments();
		List<Attachment> attachments = email.getAttachments();

		int actualEmailSize = 0; //in Bytes
		int expectedEmailSize = 2405993;

		actualEmailSize += email.getHTMLEmailBody().getAttachmentSize();
		actualEmailSize += email.getPlainTextEmailBody().getAttachmentSize();
		for (Attachment attachment : attachments) {
			actualEmailSize += attachment.getAttachmentSize();
		}
		Assert.assertEquals(actualEmailSize, expectedEmailSize);
	}

	@Test
	public void assertEmailAttachmentGettingReplacedInHtmlEmailBody() throws  MimeException, IOException{
		Email email = getParsedEmailWithLargeImageAttachments();
		Assert.assertTrue(email.isAttachmentReplacedInHtmlBody());
	}

	@Test
	public void assertEmailContainingChineseInHtmlBody() throws  MimeException, IOException{
		Email email = getParsedEmailWithChineseInHtmlBody();
		String actual = generateCheckSum(email.getHTMLEmailBody().getIs());
		String expected = "8eb0828cb53d0ce5cc853282bb6c46b38ad9d191";
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void assertEmailContainingTraditionalChineseInHtmlBody() throws  MimeException, IOException{
		Email email = getParsedEmailWithTraditionalChineseInHtmlBody();
		String actual = generateCheckSum(email.getHTMLEmailBody().getIs());
		String expected = "c3a8ec4cd790362af3cb146275819301774167b3";
		Assert.assertEquals(actual, expected);
	}

	private Email getParsedEmailWithTraditionalChineseInHtmlBody() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("traditionalChineseInHtmlBody.eml",contentHandler);
		return email;
	}

	@Test
	public void assertAttachmentNameForBadAttachmentHeader() throws  MimeException, IOException{
		Email email = getParsedEmailWithBadAttachmentHeader();
		Set<String> attachmentNames = new HashSet<String>();
		for (Attachment attachment : email.getAttachments()) {
			attachmentNames.add(attachment.getAttachmentName());
		}
		assertAttachmentNames(attachmentNames);
	}

	private void assertAttachmentNames(Set<String> attachmentNames) {
		Set<String> expectedAttachmentNames = new HashSet<String>();
		expectedAttachmentNames.add("image002.jpg");
		expectedAttachmentNames.add("image001.png");
		Assert.assertTrue(attachmentNames.containsAll(expectedAttachmentNames));
	}

	private Email getParsedEmailWithBadAttachmentHeader() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("emailWithBadAttachmentHeader.eml",contentHandler);
		return email;
	}

	private Email getParsedEmailWithChineseInHtmlBody() throws  MimeException, IOException {
		ContentHandler imageAttachmentContentHandler = getContentHandler();
		Email email = getParsedEmail("chineseContentInHtmlBody.eml",imageAttachmentContentHandler);
		return email;
	}

	@Test
	public void assertEmailParsingForExtraPlainPartInEmail() throws  MimeException, IOException{
		Email email = getParsedEmailWithExtraPlainPart();
		int expectedSize;
		assertAttachmentsSize(email.getPlainTextEmailBody().getAttachmentSize(),expectedSize = 422);
	}

	private void assertAttachmentsSize(int actualSize, int expectedSize) {
		Assert.assertEquals(actualSize, expectedSize);
	}

	private Email getParsedEmailWithExtraPlainPart() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("emailWithExtraPlainTextPart.eml",contentHandler);
		return email;
	}

	@Test
	public void assertEmailParsingForExtraHTMLPartInEmail() throws  MimeException, IOException{
		Email email = getParsedEmailWithExtraHTMLPart();
		int expectedSize;
		assertAttachmentsSize(email.getHTMLEmailBody().getAttachmentSize(),expectedSize = 1350);
	}


	private Email getParsedEmailWithExtraHTMLPart() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("emailWithExtraHtmlPart.eml",contentHandler);
		return email;
	}

	@Test
	public void assertEmailParsingForInlineNonImageAttachments() throws  MimeException, IOException{
		Email email = getParsedEmailWithInlineNonImageAttachments();
		int expectedAttachmentCount;
		assertAttachmentsCount(email.getAttachments().size(), expectedAttachmentCount = 2);
	}


	private void assertAttachmentsCount(int actualAttachmentCount, int expectedAttachmentCount) {
		Assert.assertEquals(actualAttachmentCount, expectedAttachmentCount);
	}

	private Email getParsedEmailWithInlineNonImageAttachments() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("emailWithInlineNonImageAttachments.eml",contentHandler);
		return email;
	}

	@Test
	public void assertEmailParsingForCorruptFileNameAttachments() throws  MimeException, IOException{
		Email email = getParsedEmailWithCorruptFileNameAttachments();
		assertCorruptFileName(email.getAttachments());
	}


	private void assertCorruptFileName(List<Attachment> attachmentList) {
		Assert.assertEquals(attachmentList.get(0).getAttachmentName().length(),73);
	}

	private Email getParsedEmailWithCorruptFileNameAttachments() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("emailWithCorruptFileName.eml",contentHandler);
		return email;
	}

	@Test
	public void ignoreUnidentifiedAttachment() throws  MimeException, IOException{
		Email email = getParsedEmailWithUnIdentifiedAttachments();
		List<Attachment> attachments = email.getAttachments();
		assertUnidentifiedAttachmentIgnored(attachments);
	}

	private void assertUnidentifiedAttachmentIgnored(
			List<Attachment> attachments) {
		Assert.assertTrue(attachments.size() == 0);
	}

	private Email getParsedEmailWithUnIdentifiedAttachments() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("unIdentifiedAttachmentEmail.eml",contentHandler);
		return email;
	}

	@Test
	public void addCalendarAttachment() throws  MimeException, IOException{
		Email email = getParsedEmailWithCalendarAttachments();
		assertCalendarBody(email);
	}

	private void assertCalendarBody(Email email) {
		Assert.assertEquals(email.getCalendarBody().getAttachmentName(), "calendarBody.ics");
	}

	private Email getParsedEmailWithCalendarAttachments() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("emailWithCalendar.eml",contentHandler);
		return email;
	}

	@Test
	public void decodeAppleFileNameHeader() throws  MimeException, IOException{
		Email email = getParsedEmailWithAppleFileNameHeader();
		assertDecodedFileName(email);
	}

	private void assertDecodedFileName(Email email) {
		Assert.assertEquals(email.getAttachments().get(0).getAttachmentName(), "T & L HOLDING CORP'S 2011 TAX RETURN - TaxACT 2011 Preparer's 1120S.pdf");
	}

	private Email getParsedEmailWithAppleFileNameHeader() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("appleHeaderEmail.eml",contentHandler);
		return email;
	}

	@Test
	public void parseEmailWithAppleFileAttachment() throws  MimeException, IOException{
		Email email = getParsedEmailWithAppleFileAttachment();
		Assert.assertTrue(email.getAttachments().size() == 1);
	}

	private Email getParsedEmailWithAppleFileAttachment() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("emailWithAttachedAppleFile.eml",contentHandler);
		return email;
	}

	@Test
	public void getDecodedEmailSize() throws  MimeException, IOException{
		Email email = getParsedEmailWithDecodedAttachments();
		assertDecodedEmailSize(email);
		assertDisplayEmailSize(email);
	}

	private void assertDisplayEmailSize(Email email) {
		Assert.assertEquals(FileUtils.byteCountToDisplaySize(email.getEmailSize()), "131 KB");
	}

	private void assertDecodedEmailSize(Email email) {
		Assert.assertEquals(FileUtils.byteCountToDisplaySize(email.getDecodedEmailSize()), "98 KB");
	}

	private Email getParsedEmailWithDecodedAttachments() throws  MimeException, IOException {
		ContentHandler contentHandler = getContentHandler();
		Email email = getParsedEmail("inlineEmailWithPenguins.eml",contentHandler);
		return email;
	}

	public String convertStreamToString(InputStream is, String charset)	throws IOException {
		return IOUtils.toString(is, charset);
	}

    @Test
    public void decodeWin8ClientFileNameHeader() throws  MimeException, IOException {
        Email email = getParsedEmailFromWin8();
        String[] attachmentNames = new String[email.getAttachments().size()];
        assertDecodedAttachedFileNames(email, attachmentNames);
    }

    private Email getParsedEmailFromWin8() throws  MimeException, IOException {
        ContentHandler contentHandler = getContentHandler();
        Email email = getParsedEmail("aolWin8.eml", contentHandler);
        return email;
    }

    private void assertDecodedAttachedFileNames(Email email, String[] attachmentNames) throws IOException {
        for (int i = 0; i < email.getAttachments().size(); i++) {
            Attachment attachment = email.getAttachments().get(i);
            attachmentNames[i] = attachment.getAttachmentName();
        }
        Arrays.sort(attachmentNames);
        String concatenatedfileNames = StringUtils.join(attachmentNames);
        Assert.assertEquals(generateCheckSum(concatenatedfileNames), "c961d4a532f7dded1e467816535237b30c79f85b");
    }
}