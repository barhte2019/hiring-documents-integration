package com.redhat.gpte.rhte2019.ba.document.integration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

@Component
public class SwiftPutObjectProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String authToken = exchange.getIn().getHeader("SwiftAuthToken", String.class);
        String storageUrl = exchange.getIn().getHeader("SwiftStorageUrl", String.class);
        String container = exchange.getIn().getHeader("SwiftContainer", String.class);
        String object = exchange.getIn().getHeader("object", String.class);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ByteArrayInputStream is = (ByteArrayInputStream) exchange.getIn().getBody();
            HttpUriRequest request = RequestBuilder.put()
                    .setUri(storageUrl + "/" + container + "/" + object)
                    .setHeader("X-Auth-Token", authToken)
                    .setEntity(new InputStreamEntity(is))
                    .build();
            ResponseHandler<Void> responseHandler = response -> {
                final StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() != 201) {
                    throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                } else {
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);
                    exchange.getIn().setBody("", String.class);
                }
                return null;
            };
            client.execute(request, responseHandler);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
