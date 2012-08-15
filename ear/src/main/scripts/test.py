#!/usr/bin/env python

from suds.client import Client

import logging
import sys

logging.basicConfig(level=logging.CRITICAL)

args = sys.argv
if len(args) != 3:
    print >> sys.stderr, "This must have four arguments: hostname:port, plugin mnemonic, username and password"
    sys.exit(1)

hostAndPort = args[1]
plugin = args[2]
username = args[3]
password = args[4]

client = Client("https://" + hostAndPort + "/ICATService/ICAT?wsdl")
service = client.service
factory = client.factory

credentials = factory.create("credentials")
entry = factory.create("credentials.entry")
entry.key = "username"
entry.value = username
credentials.entry.append(entry)
entry = factory.create("credentials.entry")
entry.key = "password"
entry.value = password
credentials.entry.append(entry)

sessionId = service.login(plugin, credentials,)

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
