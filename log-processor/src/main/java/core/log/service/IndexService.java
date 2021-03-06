package core.log.service;

import core.framework.api.search.ElasticSearch;
import core.framework.api.util.ClasspathResources;

import javax.inject.Inject;

/**
 * @author neo
 */
public class IndexService {
    @Inject
    ElasticSearch search;

    public void createIndexTemplates() {
        search.createIndexTemplate("action", ClasspathResources.text("index/action-index-template.json"));
        search.createIndexTemplate("trace", ClasspathResources.text("index/trace-index-template.json"));
        search.createIndexTemplate("stat", ClasspathResources.text("index/stat-index-template.json"));
    }
}
