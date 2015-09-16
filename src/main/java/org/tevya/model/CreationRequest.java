package org.tevya.model;

/**
 * Body of a creation request (POST)
 */
public class CreationRequest {
    private String serviceDomain;  // domain caller used to access the service
    private String url;            // URL to redirect to.

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
