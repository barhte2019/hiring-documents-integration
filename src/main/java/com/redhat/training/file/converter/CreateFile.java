package com.redhat.training.file.converter;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

public class CreateFile implements Processor{

	@Override
	public void process(Exchange arg0) throws Exception {
		java.io.File body = arg0.getIn().getBody(java.io.File.class);
		
		File fileMetadata = new File();
        fileMetadata.setTitle(body.getName());
        FileContent mediaContent = new FileContent(null, body);
        
        Map<String, Object> headers2 = arg0.getIn().getHeaders();
        //headers2.put("CamelGoogleDrive.fileId",body.getName()+"_copy");
        headers2.put("CamelGoogleDrive.content", fileMetadata);
        headers2.put("CamelGoogleDrive.mediaContent", mediaContent);
		
		arg0.getOut().setHeaders(headers2);
		arg0.getOut().setBody(null);
		
	}

}
