CREATE OR REPLACE PROCEDURE change_case_pr(p_owner IN  varchar2)   IS

cursor c_get_columns IS
SELECT table_name, column_name
from   all_tab_columns
where  DATA_TYPE = 'VARCHAR2'
and    DATA_LENGTH = 1
and    OWNER =p_owner;


l_value  varchar2(1);
str      varchar2(4000);

begin



	FOR r_get_columns  in  c_get_columns LOOP

       l_value := 'Y';

	str := 'update '||r_get_columns.table_name||' set '
		||r_get_columns.column_name||' = '||''''||l_value||''''
		||' where '||r_get_columns.column_name||' = '||''''||lower(l_value)||'''';

	execute immediate str;

        --dbms_output.put_line(str);

	l_value := 'N';

	str := 'update '||r_get_columns.table_name||' set '
		||r_get_columns.column_name||' = '||''''||l_value||''''
		||' where '||r_get_columns.column_name||' = '||''''||lower(l_value)||'''';

	execute immediate str;

        --dbms_output.put_line(str);

	END LOOP;

EXCEPTION
WHEN OTHERS THEN
RAiSE;

end;
/