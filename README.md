# email-mime-parser
A mime4j based simplified email mime parser for java

The provided email parser is based on Mime4j (https://james.apache.org/mime4j/index.html)

**Usage**

Add the following dependencies to your project:

For maven project :

```
<dependency>
  <groupId>tech.blueglacier</groupId>
  <artifactId>email-mime-parser</artifactId>
  <version>1.0.1</version>
</dependency>

```

For gradle project :

```
compile([group: 'tech.blueglacier', name: 'email-mime-parser', version: '1.0.1'])
```

**Sample code :**
```
ContentHandler contentHandler = new CustomContentHandler();

MimeConfig mime4jParserConfig = MimeConfig.DEFAULT;
BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();
MimeStreamParser mime4jParser = new MimeStreamParser(mime4jParserConfig,DecodeMonitor.SILENT,bodyDescriptorBuilder);
mime4jParser.setContentDecoding(true);
mime4jParser.setContentHandler(contentHandler);

InputStream mailIn = 'Provide email mime stream here';
mime4jParser.parse(mailIn);

Email email = ((CustomContentHandler) contentHandler).getEmail();

List<Attachment> attachments =  email.getAttachments();
		
Attachment calendar = email.getCalendarBody();
Attachment htmlBody = email.getHTMLEmailBody();
Attachment plainText = email.getPlainTextEmailBody();
		
String to = email.getToEmailHeaderValue();
String cc = email.getCCEmailHeaderValue();
String from = email.getFromEmailHeaderValue();
```

For more info check the test case file **'src\test\java\tech\blueglacier\parser\ParserTest.java'**



