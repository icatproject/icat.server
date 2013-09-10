#!/usr/bin/env python

from suds.client import Client

import logging
import sys

logging.basicConfig(level=logging.CRITICAL)

args = sys.argv
if len(args) < 3 or len(args) % 2 != 1:
    print >> sys.stderr, "\nThis must have two fixed arguments: url and plugin mnemonic\nfollowed by pairs of arguments to represent the credentials. For example\n\n    ", args[0], "https://example.com:8181 db username root password guess\n"
    sys.exit(1)

url = args[1]
plugin = args[2]

client = Client(url + "/ICATService/ICAT?wsdl")
service = client.service

for entity in service.getEntityNames():
    print entity
    
