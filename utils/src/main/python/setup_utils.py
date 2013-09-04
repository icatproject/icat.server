import os
import re
import subprocess
import StringIO
import threading
import shlex
import sys
import shutil
import filecmp

def abort(msg):
    """Print to stderr and stop with exit 1"""
    print >> sys.stderr, msg
    sys.exit(1)
    
class Actions(object):
    
    def __init__(self, verbosity, overwrite):
        self.verbosity = verbosity
        self.overwrite = overwrite
        self.asadminCommand = None
        self.clashes = 0
        
    def setAsadminCommand(self, command):
        self.asadminCommand = command     
    
    def getProperties(self, fileName, needed):
        """Read properties files and check that the properties in the needed list are present"""
    
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
        
        if self.verbosity:
            print "\n" + fileName + ":"
            for item in props.items():
                print "  ", item[0], ":", item[1]
        
        return props
    
    def execute(self, cmd):
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
        
        return out, err, rc
    
    def asadmin(self, command, tolerant=False, printOutput=False):
        cmd = self.asadminCommand + " " + command
        if self.verbosity: print "\nexecute: " + cmd 
        out, err, rc = self.execute(cmd)
        if self.verbosity > 1 or printOutput:
            if out: print out
            if err: print err
     
        if not tolerant and rc:
            if not self.verbosity: print cmd, " ->"
            abort(err)
    
    def installFile(self, file, dir):
        dest = os.path.join(dir, file)
        if os.path.exists(dest):
            diff = not filecmp.cmp(file, dest)
            if diff:
                if self.overwrite: 
                    shutil.copy(file , dir)
                    print "\n", dest, "has been overwritten"
                else:
                    print "\nWarning:", file, "and the one in", dir, "are different and --force not specified"
                    self.clashes += 1
        else:
            shutil.copy(file , dir)
            if self.verbosity:
                print "\n", file, "copied to", dir
            
    def removeFile(self, file, dir):
        dest = os.path.join(dir, file)
        if os.path.exists(dest): 
            os.remove(dest)
            if self.verbosity:
                print "\n", file, "removed from", dir
            
    def getAppName(self, app):
        cmd = self.asadminCommand + " " + "list-applications"
        out, err, rc = self.execute(cmd)
        if rc: abort(err)
        for line in out.split("\n"):
            if (line.startswith(app + "-")):
                return line.split()[0]
            
    def getAsadminProperty(self, name):
        cmd = self.asadminCommand + " get " + name
        if self.verbosity: print "\nexecute: " + cmd 
        out, err, rc = self.execute(cmd)
        if rc: abort(err)
        return out.split("\n")[0].split("=")[1]
    
    def setAsadminProperty(self, name, value):
        cmd = self.asadminCommand + " set " + name + "=" + value
        if self.verbosity: print "\nexecute: " + cmd 
        out, err, rc = self.execute(cmd)
        if rc: abort(err)

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
