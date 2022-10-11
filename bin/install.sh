#!/bin/sh

# aws cli - create s3
aws s3api delete-bucket --bucket salud-hl7-demo --region us-east-1
aws s3api create-bucket --bucket salud-hl7-demo --region us-east-1

# Create project
oc new-project integrations-demo
oc project integrations-demo

# Create secret to demo
oc delete secret env-cfg-demo
oc create secret generic --from-env-file=../config/env.cfg env-cfg-demo

# Deploy mongodb
oc delete svc mongodb-hl7-demo
oc delete deploy mongodb-hl7-demo
oc new-app mongo MONGO_INITDB_ROOT_USERNAME=demo MONGO_INITDB_ROOT_PASSWORD=demo --name=mongodb-hl7-demo -l app.openshift.io/runtime=mongodb

# Deploy postgresql
oc delete svc postgres-hl7-demo
oc delete deploy postgres-hl7-demo
oc new-app --name=postgres-hl7-demo -e POSTGRESQL_USER=demo -e POSTGRESQL_PASSWORD=demo -e POSTGRESQL_DATABASE=hl7db postgresql:10-el8 -l app.openshift.io/runtime=postgresql

# Deploy wsdl-emulator
# oc delete route soap-service
# oc delete svc soap-service
# oc delete deploy soap-service
# oc new-app docker.io/castlemock/castlemock --name=soap-service -l app.openshift.io/runtime=tomcat 
# oc expose svc soap-service

# Deploy hl7-to-json
cd ./hl7-to-json/
quarkus build -Dquarkus.kubernetes.deploy=true
cd ..

# Deploy get-data-from-postgresql
cd ./get-data-from-postgresql/
npx nodeshift --deploy.port 8080 --expose
cd ..

# Deploy get-data-from-postgresql
cd ./get-data-from-mongodb/
npx nodeshift --deploy.port 8080 --expose
cd ..

