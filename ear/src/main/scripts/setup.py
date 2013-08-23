#!/usr/bin/env python
from optparse import OptionParser
import sys
import shutil
import os
import subprocess
import StringIO
import threading
import shlex
import re
import filecmp

def abort(msg):
    print >> sys.stderr, msg
    sys.exit(1)
    
def getProperties(fileName, needed):
    
    if not os.path.exists(fileName): 
        abort (fileName + " file not found - please use " + fileName 
               + ".example as an example of what it should look like.")
    
    p = re.compile(r"")
    f = open(fileName)
    props = {}
    for line in f:
        line = line.strip()
        if line and not line.startswith("#") and not line.startswith("!"):
            nfirst = len(line)
            for sep in [r"\s*=\s*", r"\s*:\s*", r"\s+"]:
                match = re.search(sep, line)
                if match and match.start() < nfirst: 
                    nfirst = match.start()
                    nlast = match.end()
            if nfirst == len(line): abort (line, "found in " + fileName + " has no recognised separator between key and value") 
            key = line[:nfirst]
            value = line[nlast:]
            props[key] = value
    f.close()
    
    for item in needed:
        if (item not in props):
            abort(item + " must be specified in " + fileName)
    
    if verbosity:
        print "\n" + fileName + ":"
        for item in props.items():
            print "  ", item[0], ":", item[1]
    
    return props

class Tee(threading.Thread):
    
    def __init__(self, inst, *out):
        threading.Thread.__init__(self)
        self.inst = inst
        self.out = out
        
    def run(self):
        while 1:
            line = self.inst.readline()
            if not line: break
            for out in self.out:
                out.write(line)

def asadmin(command, tolerant=False, printOutput=False):
    cmd = asadminCommand + " " + command
    if verbosity: print "\nexecute: " + cmd
  
    cmd = shlex.split(cmd)
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stringOut = StringIO.StringIO()
   
    mstdout = Tee(proc.stdout, stringOut)
    mstdout.start()
    stringErr = StringIO.StringIO()
    mstderr = Tee(proc.stderr, stringErr)
    mstderr.start()
    rc = proc.wait()

    mstdout.join()
    mstderr.join()
    
    out = stringOut.getvalue()
    stringOut.close()
    
    err = stringErr.getvalue()
    stringErr.close()
    
    
    if verbosity > 1 or printOutput:
        if out: print out
        if err: print err
 
    if not tolerant and rc: abort(err)
    
def installFile(file, dir):
    global clashes
    dest = os.path.join(dir, file)
    if os.path.exists(dest):
        diff = not filecmp.cmp(file, dest)
        if diff:
            if overwrite: 
                shutil.copy(file , dir)
                print dest, "has been overwritten"
            else:
                print "Warning:", file, "and the one in", dir, "are different and --force not specified"
                clashes += 1
    else:
        shutil.copy(file , dir)
        if verbosity:
            print "\n", file, "copied to", dir
            
def removeFile(file, dir):
    dest = os.path.join(dir, file)
    if os.path.exists(dest): 
        os.remove(dest)
        if verbosity:
            print "\n", file, "removed from", dir   
       
root = os.getuid() == 0 

parser = OptionParser("usage: %prog [options] install | uninstall")

if root: default = '/usr/bin'
else: default = '~/bin'
parser.add_option("--binDir", "-b", help="location to store executables [" + default + "]", default=default)

parser.add_option("--verbose", "-v", help="produce more output - this may appear twice to get even more", action="count")
parser.add_option("--force", "-f", help="overwrite existing configuration files", action="store_true", default=False)

options, args = parser.parse_args()

if len(args) != 1:abort("Must have one argument: 'install' or 'uninstall'")

cmd = args[0].upper()
if cmd not in ["INSTALL", "UNINSTALL"]: abort("Must have one argument: 'install' or 'uninstall'")
verbosity = options.verbose
overwrite = options.force

binDir = os.path.expanduser(options.binDir)

if not os.path.exists ("setup.py"): abort ("This must be run from the unpacked distribution directory")

files = ["testicat", "icatadmin"]

props = getProperties("icat-setup.properties", ["icatProperties", "glassfish", "driver", "port", "domain"])

domain_path = os.path.join(props["glassfish"], "glassfish", "domains", props["domain"])
if not os.path.exists(domain_path): abort("Domain directory " + domain_path + " does not exist")
config_path = os.path.join(domain_path, "config")

asadminCommand = os.path.join(props["glassfish"], "bin", "asadmin") + " --port " + props["port"]

icatProperties = getProperties("icat.properties", ["lifetimeMinutes", "rootUserNames", "authn.list", "notification.list", "log.list"])
log4jProperties = icatProperties.get("log4j.properties")  
      
if cmd == "INSTALL":
    
    clashes = 0
    
    for v in icatProperties["authn.list"].split():
        if "authn." + v + ".jndi" not in icatProperties:
            abort ("authn.list included " + v + " but authn." + v + ".jndi is not defined") 
    
    for v in icatProperties["notification.list"].split():
        if "notification." + v not in icatProperties:
            abort ("notification.list included " + v + " but notification." + v + " is not defined") 
   
    for v in icatProperties["log.list"].split():
        if "log." + v not in icatProperties:
            abort ("log.list included " + v + " but log." + v + " is not defined")
     
    if  log4jProperties:
        dir, file = os.path.split(log4jProperties)
        if not os.path.exists(file): abort("log4j.properties file " + file + " not found")
        
    installFile("icat.properties", config_path)
    if log4jProperties:
        dir, file = os.path.split(log4jProperties)
        if dir:
            installFile(file, dir)
        else:
            installFile(file, config_path)
    
    try:
        
        asadmin("delete-jdbc-resource jdbc/icat", tolerant=True)
        asadmin("delete-jdbc-connection-pool icat", tolerant=True)
        asadmin("delete-jms-resource jms/ICATTopicConnectionFactory", tolerant=True)
        asadmin("delete-jms-resource jms/ICATTopic", tolerant=True)
        
        asadmin('create-jdbc-connection-pool --datasourceclassname ' + props["driver"] + 
                ' --restype javax.sql.DataSource --failconnection=true --steadypoolsize 2' + 
                ' --maxpoolsize 8 --ping --property ' + props["icatProperties"] + ' icat', printOutput=True)
        
        
        asadmin("create-jdbc-resource --connectionpoolid icat jdbc/icat")

        asadmin("create-jms-resource --restype javax.jms.TopicConnectionFactory jms/ICATTopicConnectionFactory")

        asadmin("create-jms-resource --restype javax.jms.Topic jms/ICATTopic")
        
        for file in files:   
            os.chmod(file, 0755)    
            shutil.copy(file , binDir)
            if verbosity:
                print "\n", file, "copied to", binDir
                
    except Exception, e:
        abort(str(e))
        
    if clashes:
        print "*****************************************************************************************************************"
        print "There have been", clashes, "instances of files not copied where one was already present and --force not specified"
        print "*****************************************************************************************************************"
        
else:  # UNINSTALL
    
    removeFile("icat.properties", config_path)
    if log4jProperties:
        dir, file = os.path.split(log4jProperties)
        if dir:
            removeFile(file, dir)
        else:
            removeFile(file, config_path)
    
    try:
        
        asadmin("delete-jdbc-resource jdbc/icat", tolerant=True)
        asadmin("delete-jdbc-connection-pool icat", tolerant=True)
        asadmin("delete-jms-resource jms/ICATTopicConnectionFactory", tolerant=True)
        asadmin("delete-jms-resource jms/ICATTopic", tolerant=True)
        
        
        for file in files:
            path = os.path.join(binDir, file)    
            if os.path.exists(path): 
                os.remove(path)
                if verbosity:
                    print file, "removed from", binDir
     
    
    except Exception, e:
        abort(str(e))       
    
            
    
