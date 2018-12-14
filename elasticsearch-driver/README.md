![Elasticsearch Project](https://jnosql.github.io/img/logos/elastic.svg)


**Elasticsearch**: Elasticsearch is a search engine based on Lucene. It provides a distributed, multitenant-capable full-text search engine with an HTTP web interface and schema-free JSON documents. Elasticsearch is developed in Java and is released as open source under the terms of the Apache License. Elasticsearch is the most popular enterprise search engine followed by Apache Solr, also based on Lucene.


### How To Install

Once this a communication layer to Elasticsearch, we're using integration test, so you need to install Elasticsearch. The recommended way is using Docker.

![Docker](https://www.docker.com/sites/default/files/horizontal_large.png)

1. Execute the maven install `mvn clean install`


### Install without testing


If you won't run the tests the database is not required, so just run the maven skipping the tests.

1. Execute the test `mvn clean install -DskipTests`
