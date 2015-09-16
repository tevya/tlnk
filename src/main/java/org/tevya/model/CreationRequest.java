package org.tevya.model;

/**
 * Created by Eric on 9/14/2015.
 */
public class CreationRequest {
    private String serviceDomain;
    private String url;

    public String getServiceDomain() {
        return serviceDomain;
    }

    public void setServiceDomain(String serviceDomain) {
        this.serviceDomain = serviceDomain;
    }

    public String getUrl () {
        return url;
    }

    public void setUrl (String url){
        this.url = url;
    }
}
