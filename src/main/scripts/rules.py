#!/usr/bin/env python3
from __future__ import print_function

import json
try:
    import httplib                                  # Python 2
except ModuleNotFoundError:
    import http.client as httplib                   # Python 3
try:
    from urllib.parse import urlparse, urlencode    # Python 3
except ImportError:
    from urllib import urlencode                    # Python 2
    from urlparse import urlparse                   # Python 2
import sys
import getpass
import sys
             
    
        
def fatal(msg):
    print(msg, file=sys.stderr)
    sys.exit(1)
    
def getConn(relativeUrl, method, parameters=None):
    path = icatpath + relativeUrl
    if parameters and method != "POST":
        path = path + "?" + parameters
    urllen = 4 + len(path) + len(host)
    if secure:
        conn = httplib.HTTPSConnection(host)
        urllen += 5
    else:
        conn = httplib.HTTPConnection(host)
        urllen += 4
    if urllen > 2048: fatal("Generated URI is of length " + urllen + " which exceeds 2048")
          
    conn.putrequest(method, path, skip_accept_encoding=True)
    conn.putheader("Cache-Control", "no-cache")
    conn.putheader("Pragma", "no-cache")
    conn.putheader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
    conn.putheader("Connection", "keep-alive")
    return conn
    
def getResponse(conn):
    response = conn.getresponse()
    rc = response.status
    if (rc // 100 != 2):
        try:
            responseContent = response.read()
            om = json.loads(responseContent)
        except Exception:
            fatal("InternalException " + responseContent)
        code = om["code"]
        message = om["message"]
        fatal(code + " " + message)
    return response 

start = sys.argv[1:]
 
if len(start) > 3:
    action, url, plugin = start[:3]
    creds = start[3:]
       
    o = urlparse(url)
    secure = o.scheme == "https"
    host = o.netloc
    icatpath = o.path
    if not icatpath.endswith("/"): icatpath = icatpath + "/"
    icatpath = icatpath + "icat/"
else:
    fatal("First arguments must be [dump|load] url and plugin mnemonic followed by pairs of arguments to represent the\ncredentials (with '-' to be prompted)")
   
if (len(creds) % 2 != 0):
    fatal ("Must have even number of credential parameters")
                        
credList = []  
for i in range (0, len(creds), 2):
    key = creds[i]   
    if creds[i + 1] == "-":
        value = getpass.getpass()
    else:
        value = creds[i + 1]
    credList.append({key:value})
    
jsonDump = {}
jsonDump["plugin"] = plugin
jsonDump["credentials"] = credList
parameters = urlencode({"json": json.dumps(jsonDump)})
conn = getConn("session", "POST")
conn.putheader('Content-Type', 'application/x-www-form-urlencoded') 
conn.putheader('Content-Length', str(len(parameters)))
conn.endheaders()
conn.send(parameters)
result = json.loads(getResponse(conn).read())       
sessionId = result["sessionId"]

if action == "dump":
    jsonDump = {}
    jsonDump["sessionId"] = sessionId
    jsonDump["query"] = "Rule"
    jsonDump["attributes"] = "ALL"
    parameters = urlencode({"json": json.dumps(jsonDump)})
    conn = getConn("port", "GET", parameters)
    conn.endheaders()
    print(getResponse(conn).read())
 
elif action == "load":
    jsonDump = {}
    jsonDump["sessionId"] = sessionId
    jsonDump["attributes"] = "ALL"

    LIMIT = '----------lImIt_of_THE_fIle_eW_$'
    CRLF = '\r\n'
    L = []
    L.append('--' + LIMIT)  
    L.append('Content-Type: text/plain')
    L.append('Content-Disposition: form-data; name="json"')
    L.append('')
    L.append(json.dumps(jsonDump))
    L.append('--' + LIMIT)
    L.append('Content-Type: application/octet-stream')
    L.append('Content-Disposition: form-data; name="file"; filename="file"')
    L.append('')
    L.append(sys.stdin.read())
    L.append('--' + LIMIT + '--')
    L.append('')
    body = CRLF.join(L)
    
    conn = getConn("port", "POST")
    conn.putheader('content-length', str(len(body)))
    conn.putheader('Content-Type', 'multipart/form-data; boundary=' + LIMIT) 
    conn.endheaders()
    conn.send(body)
    if getResponse(conn).read():
        fatal("Should have been no response")      
    
else:
    fatal("First argument must be dump or load")
   
    
    
