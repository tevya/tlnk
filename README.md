# tlnk
Tiny Link

## API:  

### Link creation:
POST to `/{accesskey}/tlnk` `[?alias={alias-name-goes-here}]`
with body  
```
{ "serviceDomain": "domain-used-to-call-the-service",
"url": "http://the.target.url.com" }
```
The accesskey is a string extracted from the server's log file. It changes every time the service is restarted and is usually about 4-5 characters long.  It always starts with a "k".
	
call returns the generated link URL to the caller as a simple string. 

#### Example:

Creating link:

URL: `http://12.23.45.67/kfe8w/lnk
VERB:  POST
BODY: 
```
{ "serviceDomain": "12.23.45.67", "url": "http://news.google.com" }
```

Creating specific alias:

URL: `http://12.23.45.67/kf46a/lnk?alias=goognews`
VERB:  POST
BODY: 
```
{ "serviceDomain": "12.23.45.67", "url": "http://news.google.com" }
```

### To follow a link just GET the URL  (e.g. from a browser)
### To delete a link just DELETE to the shortened URL.


	