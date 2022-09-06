#!/usr/bin/env python3
from __future__ import print_function
import sys
import os
from string import Template
import glob
import shutil
from zipfile import ZipFile
import subprocess

if len(sys.argv) != 4:
    raise RuntimeError("Wrong number of arguments")

containerHome = sys.argv[1]
icat_url = sys.argv[2]
lucene_url = sys.argv[3]

subst = dict(os.environ)

for f in glob.glob("src/test/install/*.war"):
    os.remove(f)

shutil.copy("src/main/config/run.properties.example",
            "src/test/install/run.properties.example")

if not os.path.exists("src/test/install/run.properties"):
    with open("src/test/install/run.properties", "w") as f:
        contents = [
            "lifetimeMinutes = 120",
            "rootUserNames = db/root",
            "maxEntities = 10000",
            "maxIdsInQuery = 500",
            "importCacheSize = 50",
            "exportCacheSize = 50",
            "authn.list = db",
            "authn.db.url = %s" % icat_url,
            "notification.list = Dataset Datafile",
            "notification.Dataset = CU",
            "notification.Datafile = CU",
            "log.list = SESSION WRITE READ INFO",
            "lucene.url = %s" % lucene_url,
            "lucene.populateBlockSize = 10000",
            "lucene.directory = %s/data/lucene" % subst["HOME"],
            "lucene.backlogHandlerIntervalSeconds = 60",
            "lucene.enqueuedRequestIntervalSeconds = 3",
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
