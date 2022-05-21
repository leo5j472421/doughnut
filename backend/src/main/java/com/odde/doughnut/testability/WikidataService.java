package com.odde.doughnut.testability;

public class WikidataService {
  private Integer servicePort;
  private String serviceUrl = "https://www.wikidata.org/wiki/Special:EntityData/";

  public WikidataService() {}

  public WikidataService(Integer servicePort) {
    this.servicePort = servicePort;
  }

  public String getApiUrl() {
    if (servicePort != null) {
      serviceUrl = "http://localhost:" + Integer.toString(servicePort) + "/wiki/";
    }
    return serviceUrl;
  }
}
