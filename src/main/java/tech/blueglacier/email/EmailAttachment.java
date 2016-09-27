package tech.blueglacier.email;

import tech.blueglacier.util.Common;
import org.apache.james.mime4j.stream.BodyDescriptor;

import java.io.InputStream;


/**
 * To Do: Persist large files to filesystem for efficiency
 */
public class EmailAttachment extends Attachment {
	
	public EmailAttachment(BodyDescriptor bd, InputStream is){
		super(bd, is);
	}
	
	@Override
	public String getAttachmentName(){
		return Common.getAttachmentName(bd);		
		}		
	}
