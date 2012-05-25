#!/usr/bin/env python

from suds.client import Client, WebFault

import logging

logging.basicConfig(level=logging.CRITICAL)

client = Client("http://127.0.0.1:8080/ICATService/ICAT?wsdl")
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

beans = []
for i in range(5):
    facility = factory.create("facility")
    facility.name = "cucumber" + str(i)
    beans.append(facility)
dft = factory.create("datafileFormat")
beans.append(dft)
#beans =[]
#beans.append(factory.create("facility"));
#print type(beans)
#for bean in beans:
#    print bean
#    service.create(sessionId, bean)
try:
    service.createMany(sessionId, beans)
except WebFault, e:
    print e

for i in range(5):
    pk = "cucumber" + str(i)
    service.delete(sessionId,(service.get(sessionId, "Facility", pk)))

print "Finally ", len(service.search(sessionId, "Facility")), "facilities"

try:
    pk = 42
    ds = service.get(sessionId, "Dataset", pk)
    print "ds", ds
except WebFault, e:
    print "There is not likely to be a dataset with ID 42"
    print e



    




