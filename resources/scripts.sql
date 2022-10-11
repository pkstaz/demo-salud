create table hl7_message 
    ( 
    first_name varchar(10)
    , last_name varchar(10)
    , genre varchar(10)
    , processing_id varchar(10)
    , message_type varchar(10)
    , message_processing_id varchar(10)
    , message_version_id varchar(10)
    , message_trigger_event varchar(10)
    , sending_application varchar(10)
    , receiving_application varchar(10)
    , message_control_id varchar(10)
    , sending_facility varchar(10)
    , receiving_facility varchar(10)
    , message_timestamp varchar(20)
    );

GRANT ALL PRIVILEGES ON DATABASE hl7db to demo;
GRANT ALL PRIVILEGES ON TABLE hl7_message TO demo;

-- insert into hl7_message (first_name, last_name, genre, processing_id, message_type, message_processing_id, message_version_id, message_trigger_event, sending_application, receiving_application, message_control_id, sending_facility, receiving_facility, message_timestamp) values (:#first_name, :#last_name, :#genre, :#processing_id, :#message_type, :#message_processing_id, :#message_version_id, :#message_trigger_event, :#sending_application, :#receiving_application, :#message_control_id, :#sending_facility, :#receiving_facility, :#message_timestamp);

-- DELETE FROM hl7_message;
-- SELECT * FROM hl7_message;