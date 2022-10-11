package com.redhat.salud;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ca.uhn.hl7v2.model.v22.datatype.*;
import ca.uhn.hl7v2.model.v22.message.ADT_A01;
import ca.uhn.hl7v2.model.v22.segment.EVN;
import ca.uhn.hl7v2.model.v22.segment.MSH;
import ca.uhn.hl7v2.model.v22.segment.PID;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;

@Path("/hl7")
@ApplicationScoped
public class HL7Resource {

    @Inject
    CamelContext context;

    @Inject
    ProducerTemplate producerTemplate;

    @Path("/mllp")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject mllp(String message) throws Exception {
        MockEndpoint endpoint = context.getEndpoint("mock:result", MockEndpoint.class);
        //endpoint.expectedMessageCount(1);

        producerTemplate.sendBody(
                "netty:tcp://localhost:{{camel.hl7.test-tcp-port}}?sync=true&encoders=#hl7encoder&decoders=#hl7decoder",
                message);

        endpoint.assertIsSatisfied(5000L);
        Exchange exchange = endpoint.getExchanges().get(0);
        ADT_A01 result = exchange.getMessage().getBody(ADT_A01.class);

        return adtToJsonObject(result);
    }

    @Path("/marshalUnmarshal")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String marshalUnmarshal(String message) throws Exception {
        return producerTemplate.requestBody("direct:marshalUnmarshal", message, String.class);
    }

    @Path("/validate")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response validate(String message) throws Exception {
        Exchange exchange = producerTemplate.request("direct:validate", e -> e.getMessage().setBody(message));
        if (exchange.isFailed()) {
            Exception exception = exchange.getException();
            return Response.serverError().entity(exception.getMessage()).build();
        }
        return Response.ok().build();
    }

    @Path("/validate/custom")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response validateCustom(String message) throws Exception {
        Exchange exchange = producerTemplate.request("direct:validateCustom", e -> e.getMessage().setBody(message));
        if (exchange.isFailed()) {
            Exception exception = exchange.getException();
            return Response.serverError().entity(exception.getMessage()).build();
        }
        return Response.ok().build();
    }

    @Path("/hl7terser")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String hl7terser(String message) throws Exception {
        Exchange exchange = producerTemplate.request("direct:hl7terser", e -> e.getMessage().setBody(message));
        return exchange.getMessage().getHeader("PATIENT_ID", String.class);
    }

    @Path("/hl7terser/bean")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String hl7terserBean(String message) throws Exception {
        return producerTemplate.requestBody("direct:hl7terserBean", message, String.class);
    }

    @Path("/xml")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject hl7Xml(String messageXml) throws Exception {
        ADT_A01 result = producerTemplate.requestBody("direct:unmarshalXml", messageXml, ADT_A01.class);
        return adtToJsonObject(result);
    }

    private JsonObject adtToJsonObject(ADT_A01 result) {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder pidBuilder = Json.createObjectBuilder();
        JsonObjectBuilder mshBuilder = Json.createObjectBuilder();
        JsonObjectBuilder evnBuilder = Json.createObjectBuilder();

        PID pid = result.getPID();
        PN patientName = pid.getPatientName();
        pidBuilder.add("first_name", patientName.getGivenName().getValue());
        pidBuilder.add("last_name", patientName.getFamilyName().getValue());
        pidBuilder.add("birthday_date", pid.getDateOfBirth().getTs1_TimeOfAnEvent().getValue());
        if (pid.getPid3_PatientIDInternalID().length > 1) {
            CM_PAT_ID cm_pat_id = pid.getPid3_PatientIDInternalID(2);
            pidBuilder.add("patient_id", cm_pat_id.getCm_pat_id1_IDNumber().getValue());
            cm_pat_id = pid.getPid3_PatientIDInternalID(1);
            pidBuilder.add("rut", cm_pat_id.getCm_pat_id1_IDNumber().getValue());
            cm_pat_id = pid.getPid3_PatientIDInternalID(0);
            pidBuilder.add("bdup_id", cm_pat_id.getCm_pat_id1_IDNumber().getValue());
        }
        AD patientAddress = pid.getPatientAddress(0);
        pidBuilder.add("address", patientAddress.getAd1_StreetAddress().getValue());
        pidBuilder.add("city", patientAddress.getAd3_City().getValue());
        if (patientAddress.getAd2_OtherDesignation().getValue() != null) {
            pidBuilder.add("comuna", patientAddress.getAd2_OtherDesignation().getValue());
        }
        if (patientAddress.getAd7_Type().getValue() != null) {
            pidBuilder.add("address_type", patientAddress.getAd7_Type().getValue());
        }
        pidBuilder.add("country", patientAddress.getAd6_Country().getValue());
        pidBuilder.add("state", patientAddress.getAd4_StateOrProvince().getValue());
        pidBuilder.add("phone", pid.getPid13_PhoneNumberHome(0).getValue());
        pidBuilder.add("genre", pid.getSex().getValue());

        objectBuilder.add("pid", pidBuilder);

        MSH msh = result.getMSH();
        mshBuilder.add("processing_id", msh.getProcessingID().getValue());
        mshBuilder.add("message_type", msh.getMessageType().getMessageType().getValue());
        mshBuilder.add("message_processing_id", msh.getMsh11_ProcessingID().getValue());
        mshBuilder.add("message_version_id", msh.getMsh12_VersionID().getValue());
        mshBuilder.add("message_trigger_event", msh.getMessageType().getTriggerEvent().getValue());
        mshBuilder.add("sending_application", msh.getMsh3_SendingApplication().getValue());
        mshBuilder.add("receiving_application", msh.getMsh5_ReceivingApplication().getValue());
        mshBuilder.add("message_control_id", msh.getMsh10_MessageControlID().getValue());
        mshBuilder.add("sending_facility", msh.getSendingFacility().getValue());
        mshBuilder.add("receiving_facility", msh.getReceivingFacility().getValue());
        mshBuilder.add("message_timestamp", msh.getMsh7_DateTimeOfMessage().getTs1_TimeOfAnEvent().getValue());
        if (msh.getMessageType().getExtraComponents().getComponent(0).getData() == null) {
            mshBuilder.add("eventType", msh.getMessageType().getExtraComponents().getComponent(0).getData().toString());
        }
        objectBuilder.add("msh", mshBuilder);

        EVN evn = result.getEVN();
        if (evn.getEvn1_EventTypeCode().getValue() != null) {
            evnBuilder.add("eventType_code", evn.getEvn1_EventTypeCode().getValue());
        }
        if (evn.getDateTimeOfEvent().getTs1_TimeOfAnEvent().getValue() != null) {
            evnBuilder.add("event_timestamp", evn.getDateTimeOfEvent().getTs1_TimeOfAnEvent().getValue());
        }
        objectBuilder.add("evn", evnBuilder);

        return objectBuilder.build();
    }
}