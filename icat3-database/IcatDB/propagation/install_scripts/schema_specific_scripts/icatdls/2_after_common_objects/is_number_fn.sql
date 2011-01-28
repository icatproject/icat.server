CREATE OR REPLACE function IS_NUMBER(str in varchar2) return number IS
dummy number;
begin
dummy := TO_NUMBER(str);
return (0);
Exception WHEN OTHERS then
return (1);
end;
/