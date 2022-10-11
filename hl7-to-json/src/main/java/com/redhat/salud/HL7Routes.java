package com.redhat.salud;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ca.uhn.hl7v2.model.v22.message.ADT_A01;
import ca.uhn.hl7v2.parser.Parser;
import org.apache.camel.builder.RouteBuilder;

import static org.apache.camel.component.hl7.HL7.hl7terser;

@ApplicationScoped
public class HL7Routes extends RouteBuilder {

    @Inject
    Parser parser;

    @Override
    public void configure() throws Exception {
        from("netty:tcp://localhost:{{camel.hl7.test-tcp-port}}?sync=true&encoders=#hl7encoder&decoders=#hl7decoder")
                .convertBodyTo(ADT_A01.class)
                .to("mock:result");

        from("direct:validate")
                .unmarshal("hl7DataFormat");

        from("direct:validateCustom")
                .unmarshal().hl7(false)
                .marshal().hl7(parser);

        from("direct:marshalUnmarshal")
                .unmarshal("hl7DataFormat")
                .marshal("hl7DataFormat");

        from("direct:hl7terser")
                .setHeader("PATIENT_ID", hl7terser("PID-3-1"));

        from("direct:hl7terserBean")
                .bean("terserBean");

    }
}