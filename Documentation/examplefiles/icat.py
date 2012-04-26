#!/usr/bin/env python

from suds.client import Client, WebFault

from suds.plugin import MessagePlugin

import logging

import re

class GetPlugin(MessagePlugin):
    """
    Needed to patch the outgoing message for the get calls. 
    
    It will currently not work if a string key is compatible with a non-negative integer.
    """ 

    intPat = re.compile("\d+$")

    def marshalled(self, context):
        body = context.envelope.getChild('Body')
        method = body[0]
        if method.name == "get":
            key = method[2]
            if self.intPat.match(key.getText()):
                key.set("xsi:type", "xs:long")
            else:
                key.set("xsi:type", "xs:string")
            key.set("xmlns:xs", "http://www.w3.org/2001/XMLSchema")

logging.basicConfig(level=logging.CRITICAL)

client = Client("http://127.0.0.1:8080/ICATService/ICAT?wsdl", plugins=[GetPlugin()])
service = client.service
factory = client.factory

sessionId = service.login("root", "password")

print "Initially ", len(service.search(sessionId, "Facility")), "facilities"

try:
    facility = factory.create("facility")
    facility.name = "cucumber"
    service.create(sessionId, facility)
except WebFault, e:
    print e
    
print "Then ", len(service.search(sessionId, "Facility")), "facilities"
    
pk = "cucumber"
fac = service.get(sessionId, "Facility", pk)
fac.daysUntilRelease = 28
service.update(sessionId, fac)

print "New facility is now", service.get(sessionId, "Facility", pk)

service.delete(sessionId,(service.get(sessionId, "Facility", pk)))

print "Finally ", len(service.search(sessionId, "Facility")), "facilities"

try:
    pk = 42
    ds = service.get(sessionId, "Dataset", pk)
    print "ds", ds
except WebFault, e:
    print "There is not likely to be a dataset with ID 42"
    print e



    




