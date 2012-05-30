#!/usr/bin/env python

from suds.client import Client

import logging
import sys

logging.basicConfig(level=logging.CRITICAL)

args = sys.argv
if len(args) != 3:
    print >> sys.stderr, "This must have two arguments: hostname:port and password"
    sys.exit(1)

hostAndPort = args[1]
password = args[2]

client = Client("https://" + hostAndPort + "/ICATService/ICAT?wsdl")
service = client.service
factory = client.factory

sessionId = service.login("root", password)

groups = service.search(sessionId, "Group[name='annoying animals']")
if len(groups): 
    print "Groups 'annoying animals' already exist - they will be deleted"
    for group in groups:
        service.delete(sessionId, group)
        
group = factory.create("group")
group.name = "annoying animals"
service.create(sessionId, group)

groups = service.search(sessionId, "Group[name='annoying animals']")
if len(groups) != 1:
    print >> sys.stderr, "There are now", len(groups), "groups instead of 1 - something is wrong"
    sys.exit(1)
    
for group in groups:
    service.delete(sessionId, group)
    
groups = service.search(sessionId, "Group[name='annoying animals']")
if len(groups):
    print >> sys.stderr, "There are now", len(groups), "groups instead of 0 - something is wrong"
    sys.exit(1)
    
service.logout(sessionId)
    
print "Login, search, create, delete and logout operations were all successful."
