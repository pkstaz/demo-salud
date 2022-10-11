################################################## 
##################################################
###                                            ### 
###                Integrations                ###      
###                                            ###
##################################################
##################################################

# Deploy s3-to-kafka integration 
oc delete integration s3-to-kafka
oc apply -f ../s3-to-kafka/s3-to-kafka.yaml

# Deploy kafka-to-nosql 
oc delete integration kafka-to-mongodb
oc apply -f ../kafka-to-nosql/kafka-to-mongodb.yaml

# Deploy kafka-to-sql
oc delete integration kafka-to-postgresql
oc apply -f ../kafka-to-sql/kafka-to-postgresql.yaml
