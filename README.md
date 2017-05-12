# Smile Liferay Elasticsearch

## Requirements

This plugin requires the following to run:

  * [Liferay](https://www.liferay.com) 6.1+
  * [Elasticsearch](https://www.elastic.co/products/elasticsearch) 5.x+


## How it works

### ElasticSearch Client

This module provides the main and useful objects for interfacing Liferay and ElasticSearch.

### Liferay ElasticSearch Web

**Smile-liferay-elasticsearch-web** serves as a link between Liferay and ElasticSearch and is the main module you have to care about.

1) Ensure the **spring/elasticsearch-client.xml** is accessible through the classpath.

2) Edit the following properties in your **portal-ext.properties**.

```
elasticsearch.clusterName=#ELASTICSEARCH_CLUSTERNAME#
elasticsearch.index=#ELASTICSEARCH_INDEX#
elasticsearch.node=#ELASTICSEARCH_NODES#
elasticsearch.homeFile=#ELASTICSEARCH_HOME#
elasticsearch.settings.path=#ELASTICSEARCH_SETTINGS_FILEPATH#
elasticsearch.mappings.path=#ELASTICSEARCH_MAPPINGS_FILEPATH#
```

Example

```
elasticsearch.clusterName=elasticsearch
elasticsearch.index=liferay
elasticsearch.node=localhost:9300
elasticsearch.homeFile=/opt/elasticsearch
```

In this sample, `elasticsearch.settings.path` and `elasticsearch.mappings.path` are not used.
In this case, the **smile-liferay-elasticsearch-web** module loads the default files stored in its resource folder.


3) Deploy **smile-liferay-elasticsearch-web** in Liferay.

4) Reindex all search indexes from the control panel.

### Liferay Search Hook

The hook module overrides the Liferay Search Portlet and links it to the services dedicated to ElasticSearch.

### ElasticSearch Management

This module **is still in development**. Its goal is to provide a portlet to manage the index as update the settings or the mappings.

## Contributing

- Matthieu REMY (author)
- Edouard CATTEZ

## To do

- Search Service in **elasticsearch-client**
- Continue the management portlet in **elasticsearch-management**