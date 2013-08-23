#!/usr/bin/env python

from suds.client import Client

import logging
import sys

logging.basicConfig(level=logging.CRITICAL)

args = sys.argv
if len(args) < 3 or len(args) % 2 != 1:
    print >> sys.stderr, "\nThis must have two fixed arguments: url and plugin mnemonic\nfollowed by pairs of arguments to represent the credentials. For example\n\n    ", args[0], "example.com 8181 db username root password guess\n"
    sys.exit(1)

url = args[1]
plugin = args[2]

client = Client(url + "/ICATService/ICAT?wsdl")
service = client.service
factory = client.factory

credentials = factory.create("credentials")
for i in range (3, len(args), 2):
    entry = factory.create("credentials.entry")
    entry.key = args[i]
    entry.value = args[i + 1]
    credentials.entry.append(entry)

sessionId = service.login(plugin, credentials,)

rules = service.search(sessionId, "Rule INCLUDE Grouping")
f = open("rules.authz", "w")
for rule in rules:
    print >> f, "adduser", rule.grouping.name, rule.what, rule.crudFlags
f.close()
print len(rules), "rules have been written to rules.authz"
