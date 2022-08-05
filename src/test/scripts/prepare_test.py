#!/usr/bin/env python
from __future__ import print_function
import sys
import os
from string import Template
import glob
import shutil
from zipfile import ZipFile
import subprocess

if len(sys.argv) != 6:
    raise RuntimeError("Wrong number of arguments")

containerHome = sys.argv[1]
icat_url = sys.argv[2]
search_engine = sys.argv[3]
lucene_url = sys.argv[4]
opensearch_url = sys.argv[5]

if search_engine == "LUCENE":
    search_urls = lucene_url
elif search_engine == "OPENSEARCH" or search_engine == "ELASTICSEARCH":
    search_urls = opensearch_url
else:
    raise RuntimeError("Search engine %s unrecognised, " % search_engine
                       + "should be one of LUCENE, ELASTICSEARCH, OPENSEARCH")

subst = dict(os.environ)

for f in glob.glob("src/test/install/*.war"):
    os.remove(f)

shutil.copy("src/main/config/run.properties.example",
            "src/test/install/run.properties.example")

with open("src/test/install/run.properties", "w") as f:
    contents = [
        "lifetimeMinutes = 120",
        "rootUserNames = db/root simple/root",
        "maxEntities = 10000",
        "maxIdsInQuery = 500",
        "importCacheSize = 50",
        "exportCacheSize = 50",
        "authn.list = db simple",
        "authn.db.url = %s" % icat_url,
        "authn.simple.url = %s" % icat_url,
        "notification.list = Dataset Datafile",
        "notification.Dataset = CU",
        "notification.Datafile = CU",
        "log.list = SESSION WRITE READ INFO",
        "search.engine = %s" % search_engine,
        "search.urls = %s" % search_urls,
        "search.populateBlockSize = 10000",
        "search.directory = %s/data/search" % subst["HOME"],
        "search.backlogHandlerIntervalSeconds = 60",
        "search.enqueuedRequestIntervalSeconds = 3",
        "search.aggregateFilesIntervalSeconds = 3600",
        "search.maxSearchTimeSeconds = 5",
        "key = wombat"
    ]
    f.write("\n".join(contents))

if not os.path.exists("src/test/install/setup.properties"):
    with open("src/test/install/setup.properties", "w") as f:
        contents = [
            "# Glassfish",
            "secure         = true",
            "container      = Glassfish",
            "home           = %s" % containerHome,
            "port           = 4848",
            "# MySQL",
            "db.driver      = com.mysql.jdbc.jdbc2.optional.MysqlDataSource",
            "db.url         = jdbc:mysql://localhost:3306/icatdb",
            "db.username    = icatdbuser",
            "db.password    = icatdbuserpw"
        ]
        f.write("\n".join(contents))

shutil.copy(glob.glob("target/icat.server-*.war")[0], "src/test/install/")
shutil.copy("src/main/scripts/setup", "src/test/install/")
shutil.copy("src/main/scripts/testicat", "src/test/install/")
shutil.copy("src/main/scripts/icatadmin", "src/test/install/")


with ZipFile(glob.glob("target/icat.server-*-distro.zip")[0]) as z:
    with open("src/test/install/setup_utils.py", "wb") as f:
        f.write(z.read("icat.server/setup_utils.py"))

if not os.path.exists("src/test/install/logback.xml"):
    with open("src/main/resources/logback.xml", "rt") as s:
        with open("src/test/install/logback.xml", "wt") as f:
            t = Template(s.read()).substitute(subst)
            f.write(t)

binDir = subst["HOME"] + "/bin"
if not os.path.exists(binDir):
    os.mkdir(binDir)

p = subprocess.Popen(["./setup", "install"], cwd="src/test/install")
p.wait()
