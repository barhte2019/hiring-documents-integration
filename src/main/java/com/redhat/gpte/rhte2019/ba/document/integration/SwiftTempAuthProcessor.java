package com.redhat.gpte.rhte2019.ba.document.integration;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

@Component
public class SwiftTempAuthProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String[] result= authenticateTempAuth(
                exchange.getIn().getHeader("SwiftHost", String.class),
                exchange.getIn().getHeader("SwiftUsername", String.class),
                exchange.getIn().getHeader("SwiftPassword", String.class));
        exchange.getIn().setHeader("SwiftAuthtoken", result[0]);
        exchange.getIn().setHeader("SwiftStorageurl", result[1]);
    }

    public String[] authenticateTempAuth(String host, String user, String password) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String uri = new URIBuilder().setScheme("http").setHost(host).setPath("auth/v1.0").toString();
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("X-Storage-User", user)
                    .setHeader("X-Storage-Pass", password)
                    .build();
            ResponseHandler<String[]> responseHandler = response -> {
                final StatusLine statusLine = response.getStatusLine();
                final HttpEntity entity = response.getEntity();
                if (statusLine.getStatusCode() >= 300) {
                    EntityUtils.consume(entity);
                    throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                }
                return new String[]{response.getFirstHeader("X-Auth-Token").getValue(), response.getFirstHeader("X-Storage-Url").getValue()};
            };
            return client.execute(request, responseHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
