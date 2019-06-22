# Fuse REST service to manage HR documents

This project builds out a document management REST service using Camel routes in Spring Boot via a Spring XML configuration file.

The application utilizes the Spring [`@ImportResource`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/ImportResource.html) annotation to load a Camel Context definition via a [camel-context.xml](src/main/resources/spring/camel-context.xml) file on the classpath.

### Running the application on OpenShift Cluster

The following set of instructions will guide you on how to deploy the service in an OCP 4.0 cluster. An OCP cluster has been provisioned and you may oc login as follows

```
$ oc login https://master.${GUID}.openshift.opentlc.com -u user1 -p r3dh4t1!
```

+ Create your project namespace:
```
$ oc new-project MY_PROJECT_NAME
```

+ Build and deploy the project to the Kubernetes / OpenShift cluster:
```
$ mvn clean -DskipTests fabric8:deploy -Popenshift

$ oc get svc
....
NAME                           TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
rest-split-transform-amq-lab   ClusterIP   172.30.211.103   <none>        8080/TCP   45s
....

$ oc expose service rest-split-transform-amq-lab

$ curl -k http://`oc get route rest-split-transform-amq-lab -o template --template {{.spec.host}}`/rest/service/offers -X POST  -d 'Senor Fuerte, We are hereby pleased to offer you a salary of SALARY and a bonus of BONUS' -H 'content-type: text/html'

$ curl -k http://`oc get route rest-split-transform-amq-lab -o template --template {{.spec.host}}`/rest/service/offers/10001 -X GET  -H 'content-type: text/html'
```

