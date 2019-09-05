package com.redhat.gpte.rhte2019.ba.document.integration;

import java.io.ByteArrayInputStream;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DocumentSwiftGateway extends RouteBuilder {

    @Autowired
    private SwiftGetObjectProcessor getObjectProcessor;

    @Autowired
    private SwiftTempAuthProcessor authProcessor;

    @Autowired
    private SwiftPutObjectProcessor putObjectProcessor;

    @Override
    public void configure() throws Exception {


	restConfiguration()
	    .component("servlet")
	    //.bindingMode(RestBindingMode.auto)
	    .dataFormatProperty("prettyPrint", "true")
	    .apiContextPath("/api-docs")
	    .apiContextRouteId("swagger")
	    .apiProperty("api.title", "Document Service API").apiProperty("api.version", "1.0.0");

        rest()
            .get("/get")
            .param().name("object").type(RestParamType.query).dataType("string").endParam()
            .produces("application/octet-stream")
            .route()
                .setHeader("SwiftHost", simple("{{swift.host}}"))
                .setHeader("SwiftUsername", simple("{{swift.username}}"))
                .setHeader("SwiftPassword", simple("{{swift.password}}"))
                .setHeader("SwiftContainer", simple("{{swift.container}}"))
                .to("direct:swift-get")
            .endRest()

            .put("/put")
            .param().name("object").type(RestParamType.query).dataType("string").endParam()
            .route()
                .setHeader("SwiftHost", simple("{{swift.host}}"))
                .setHeader("SwiftUsername", simple("{{swift.username}}"))
                .setHeader("SwiftPassword", simple("{{swift.password}}"))
                .setHeader("SwiftContainer", simple("{{swift.container}}"))
                .convertBodyTo(ByteArrayInputStream.class)
                .to("direct:swift-put")
            .endRest()
        ;

        from("direct:swift-get")
                .process(authProcessor)
                .process(getObjectProcessor)
                .removeHeaders("Swift*").removeHeader("object");

        from("direct:swift-put")
                .process(authProcessor)
                .process(putObjectProcessor)
                .removeHeaders("Swift*").removeHeader("object");
    }
}
