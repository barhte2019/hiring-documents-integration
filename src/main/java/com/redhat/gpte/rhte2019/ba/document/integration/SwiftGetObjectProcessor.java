package com.redhat.gpte.rhte2019.ba.document.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

@Component
public class SwiftGetObjectProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String authToken = exchange.getIn().getHeader("SwiftAuthToken", String.class);
        String storageUrl = exchange.getIn().getHeader("SwiftStorageUrl", String.class);
        String container = exchange.getIn().getHeader("SwiftContainer", String.class);
        String object = exchange.getIn().getHeader("object", String.class);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(storageUrl + "/" + container + "/" + object)
                    .setHeader("X-Auth-Token", authToken)
                    .build();
            ResponseHandler<Void> responseHandler = response -> {
                final StatusLine statusLine = response.getStatusLine();
                final HttpEntity entity = response.getEntity();
                if (statusLine.getStatusCode() == 404) {
                    EntityUtils.consume(entity);
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/plain");
                } else if (statusLine.getStatusCode() >= 300) {
                    EntityUtils.consume(entity);
                    throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    entity.writeTo(baos);
                    exchange.getIn().setBody(baos.toByteArray());
                }
                return null;
            };
            client.execute(request, responseHandler);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
