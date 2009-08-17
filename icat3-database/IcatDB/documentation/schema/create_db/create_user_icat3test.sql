# drop user and all objects

DROP USER ICAT3TEST CASCADE;

-- recreate user

create user icat3test identified by icat34all
default tablespace users 
quota unlimited on users;

grant resource to icat3test;
grant connect to icat3test;

