# email-mime-parser
A simplified email mime parser for java

The provided email parser is based on Mime4j (https://james.apache.org/mime4j/index.html)

**Usage**

Add the following maven dependencies to your project:

```
dependencies {
    compile([group: 'org.mockito', name: 'mockito-all', version: '1.10.19'],
            [group: 'javax.mail', name: 'mail', version: '1.4.7'],
            [group: 'net.freeutils', name: 'jcharset', version: '2.0'],
            [group: 'org.apache.james', name: 'apache-mime4j', version: '0.7.2'],
            [group: 'commons-codec', name: 'commons-codec', version: '1.10'],
            [group: 'org.apache.commons', name: 'commons-lang3', version: '3.4'],
            [group: 'commons-io', name: 'commons-io', version: '2.5'],
            [group: 'commons-configuration', name: 'commons-configuration', version: '1.10'],
            [group: 'ch.qos.logback', name: 'logback-core', version: '1.1.7'],
            [group: 'org.slf4j', name: 'slf4j-api', version: '1.7.21'],
            ['commons-collections:commons-collections:3.2.2']
    )
    }
```

Sample code :
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

 For more info check the test case file 'ParserTest.java'



