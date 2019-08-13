### Fuse REST service to manage documents stored in Openstack Swift

Installation on OpenShift:
* Using `oc` client, login into OpenShift
* Create a namespace for the application
```
$ oc new-project swift-integration
```
* Give the default systemaccount in the namespace cluster view permissions
```
$ oc adm policy add-role-to-user view -n default
```
* Create a file ```application.properties``` with the following contents (adapt as necessary):
```
swift.host=swift.example.com:8080
swift.username=test:tester
swift.password=testing
swift.container=TEST
```
* Create a configmap ```swift-integration``` in the namespace
```
$ oc create configmap swift-integration --from-file=application.properties
```
* Deploy the application with the Fabric8 maven plugin:
```
$ mvn clean package fabric8:deploy -Popenshift
```

Usage:
* Put an object in Swift:
```
$ curl -X PUT -T /tmp/swifttest.txt http://$APP_ROUTE_HOST/api/put?object=test/swifttest.txt
```
* Retrieve an object from Swift:
```
$ curl http://$APP_ROUTE_HOST/api/get?object=test/swifttest.txt
```