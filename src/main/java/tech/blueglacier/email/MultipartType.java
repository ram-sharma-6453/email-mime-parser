package tech.blueglacier.email;

import org.apache.james.mime4j.stream.BodyDescriptor;

public class MultipartType {
	
	private BodyDescriptor bodyDiscriptor;

	public MultipartType(BodyDescriptor bodyDescriptor){
		this.bodyDiscriptor = bodyDescriptor; 
	}

	public BodyDescriptor getBodyDescriptor() {
		return bodyDiscriptor;
	}	
}
