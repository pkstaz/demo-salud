package com.redhat.salud;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.Version;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.ValidationContext;
import ca.uhn.hl7v2.validation.builder.ValidationRuleBuilder;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;
import org.apache.camel.component.hl7.HL7DataFormat;
import org.apache.camel.component.hl7.HL7MLLPNettyDecoderFactory;
import org.apache.camel.component.hl7.HL7MLLPNettyEncoderFactory;
import org.apache.camel.component.hl7.Hl7Terser;
import org.apache.camel.spi.DataFormat;

public class HL7Producer {

    @ApplicationScoped
    @Named("hl7encoder")
    public HL7MLLPNettyEncoderFactory hl7MLLPNettyEncoderFactory() {
        HL7MLLPNettyEncoderFactory factory = new HL7MLLPNettyEncoderFactory();
        factory.setConvertLFtoCR(true);
        return factory;
    }

    @ApplicationScoped
    @Named("hl7decoder")
    public HL7MLLPNettyDecoderFactory hl7MLLPNettyDecoderFactory() {
        HL7MLLPNettyDecoderFactory factory = new HL7MLLPNettyDecoderFactory();
        factory.setConvertLFtoCR(true);
        return factory;
    }

    @ApplicationScoped
    @Named
    public DataFormat hl7DataFormat() {
        return new HL7DataFormat();
    }

    @ApplicationScoped
    @Named
    public TerserBean terserBean() {
        return new TerserBean();
    }

    @ApplicationScoped
    @Named
    public Parser parser() {
        ValidationRuleBuilder builder = new ValidationRuleBuilder() {
            @Override
            protected void configure() {
                // Configure a fake validation scenario where the patient id should match a specific value
                forVersion(Version.V22)
                        .message("ADT", "*")
                        .terser("PID-2", isEqual("00009999"));
            }
        };

        ValidationContext customValidationContext = ValidationContextFactory.fromBuilder(builder);
        HapiContext customContext = new DefaultHapiContext(customValidationContext);
        return new GenericParser(customContext);
    }

    static class TerserBean {
        public String patientId(@Hl7Terser(value = "PID-3-1") String patientId) {
            return patientId;
        }
    }
}