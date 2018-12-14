![Arangodb Project](https://jnosql.github.io/img/logos/ArangoDB.png)


**Arangodb**: ArangoDB is a native multi-model database with flexible data models for documents, graphs, and key-values. Build high performance applications using a convenient SQL-like query language or JavaScript extensions.


### How To Install

Integration tests depends on a docker installation to run the ArangoDB container.

![Docker](https://www.docker.com/sites/default/files/horizontal_large.png)


1. Install docker: https://www.docker.com/

1. Execute the maven install `mvn clean install`


### Install without testing


If you won't run the tests the database is not required, so just run the maven skipping the tests.

1. Execute the test `mvn clean install -DskipTests`

### Adding dependencies

If you are not using a Java EE application server, you must add the following dependencies:

Maven
```xml
<dependency>
    <groupId>org.eclipse</groupId>
    <artifactId>yasson</artifactId>
    <version>1.0</version>
</dependency>

<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>javax.json</artifactId>
    <version>1.1</version>
</dependency>
```
Gradle
```groovy
compile('org.eclipse:yasson:1.0')
compile('org.glassfish:javax.json:1.1')
```
