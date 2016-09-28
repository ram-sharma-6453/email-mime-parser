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
  <version>1.0.0</version>
</dependency>

```

For gradle project :

```
group: 'tech.blueglacier', name: 'email-mime-parser', version: '1.0.0'
```

**Sample code :**
```
ContentHandler contentHandler = new CustomContentHandler();

MimeConfig mime4jParserConfig = new MimeConfig();
BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();
MimeStreamParser mime4jParser = new MimeStreamParser(mime4jParserConfig,DecodeMonitor.SILENT,bodyDescriptorBuilder);
mime4jParser.setContentDecoding(true);
mime4jParser.setContentHandler(contentHandler);


InputStream mailIn = 'Provide email mime stream here';
mime4jParser.parse(mailIn);

Email email = ((CustomContentHandler) contentHandler).getEmail();
```

 The 'email' object provides the logical email entities via convenience methods now.

 For more info check the test case file **'ParserTest.java'**



