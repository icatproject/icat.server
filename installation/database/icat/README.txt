To install the icat schemas:

1. cd to the directory containing the file install_icat.sql.
2. Run sqlplus without connecting to the database (sqlplus /nolog) 
3. Run the script install_icat.sql (@install_icat.sql).

The installation script will request the sys password so that it can connect to the database and create the schemas (icat, icatuser) and the directory needed for the creation of the external tables.  The installation script will ask for a password to be assigned to each of the newly created schema users (icat, icatuser and dataportal)

e.g.

/tmp/icat_download_bundle/database/icat>sqlplus /nolog

SQL*Plus: Release 10.2.0.1.0 - Production on Fri Oct 10 12:10:51 2008

Copyright (c) 1982, 2005, Oracle.  All rights reserved.

SQL> @install_icat.sql

I C A T I S I S   I N S T A L L

This script will create ICATISIS schema objects in a named schema in a
specified database.

Enter Database Name             : XE
Enter SYS password              : mysyspasswd
Enter icat password       	: myicatpasswd
Enter icatuser   password       : myicatuserpasswd
Enter icatlog    password       : myicatlogpasswd
Enter External tables location 	: /tmp/extloc

====================================================================
creating user icat

...
...
