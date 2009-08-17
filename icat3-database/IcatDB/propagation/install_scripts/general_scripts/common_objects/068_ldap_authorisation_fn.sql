CREATE OR REPLACE FUNCTION ldap_authorisation(
  p_username IN VARCHAR2,
  p_password IN VARCHAR2) RETURN BOOLEAN AS

/*

Author:
  James Healy, January 2007

Description:
  used in APEX applications, authenticates the username and password on the
  LDAP server.
  gets the Distinguished Name form the username, which means user does not have
  to specify their business unit.
  Note this allows unified access for the apex applications - i.e.
  they can use fed-id/password for access

*/

  LDAP_HOST CONSTANT VARCHAR2(15) := 'fed.cclrc.ac.uk';
  LDAP_PORT CONSTANT VARCHAR2(3) := '389';
  LDAP_BASE CONSTANT VARCHAR2(27) := 'dc=fed,dc=cclrc,dc=ac,dc=uk';

  l_session dbms_ldap.session;
  l_message dbms_ldap.message;
  l_attributes dbms_ldap.string_collection ;

  user_dn VARCHAR2(256);

  x PLS_INTEGER;
BEGIN
  -- need to treat null passwords as a special case as simple_bind_s
  -- WORKS for a null password!!
  IF p_username IS NULL OR p_password IS NULL THEN
    RETURN FALSE;
  END IF;


  l_attributes(1) := '1.1'; -- specify no attributes to be returned
  dbms_ldap.use_exception := TRUE;

  l_session := dbms_ldap.init(LDAP_HOST, LDAP_PORT);

  x :=
    dbms_ldap.search_s(
      ld       => l_session,
      base     => LDAP_BASE,
      scope    => dbms_ldap.SCOPE_SUBTREE,
      filter   => '(CN=' || p_username || ')',
      attrs    => l_attributes,
      attronly => 0, -- attribute types and values are to be returned
      res      => l_message);

  l_message := dbms_ldap.first_entry(l_session, l_message);

  user_dn := dbms_ldap.get_dn(l_session, l_message);

  -- now that we have the user's DN, try to bind with that and the password
  x := dbms_ldap.simple_bind_s(l_session, user_dn, p_password);

  x := dbms_ldap.unbind_s(l_session);

  RETURN TRUE;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.init;
    log_pkg.write_exception(
      'LDAP Auth Error. '||
      'Username: '||p_username||' Message: '||SQLERRM);
    BEGIN
      x := dbms_ldap.unbind_s(l_session);
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;

    RETURN FALSE;
END ldap_authorisation;
/
