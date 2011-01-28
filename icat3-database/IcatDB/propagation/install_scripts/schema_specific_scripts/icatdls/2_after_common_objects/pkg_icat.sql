CREATE OR REPLACE PACKAGE pkg_icat AS

	TYPE v_ref_cursor_type			IS REF CURSOR;

	PROCEDURE send_xml (
		v_xml						IN	CLOB,
		v_errorcode					OUT	NUMBER);

	PROCEDURE add_icat_authorisation (
		v_user_id					IN	icat_authorisation.user_id%TYPE,
		v_role						IN	icat_authorisation.role%TYPE,
		v_element_type				IN	icat_authorisation.element_type%TYPE,
		v_element_id				IN	icat_authorisation.element_id%TYPE,
		v_parent_element_type		IN	icat_authorisation.parent_element_type%TYPE,
		v_parent_element_id			IN	icat_authorisation.parent_element_id%TYPE,
		v_errorcode					OUT	NUMBER);

END pkg_icat;
/