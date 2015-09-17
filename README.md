# tlnk
Tiny Link

## API:  

### Link creation:
POST to `/{accesskey}/tlnk` `[?alias=alias-name-goes-here]`
with body  
```
{ "serviceDomain": "domain-used-to-call-the-service",
"url": "http://the.target.url.com" }
```
	The accesskey is a string extracted from the server's log file. It changes every time the service is restarted and is usually about 4-5 characters long.  It always starts with a "k".
	
	returns the generated link URL.

### To follow a link just GET the URL  (e.g. from a browser)
### To delete a link just DELETE to the shortened URL.


	