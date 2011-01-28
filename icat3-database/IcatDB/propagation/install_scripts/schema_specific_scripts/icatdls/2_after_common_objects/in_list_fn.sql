CREATE OR REPLACE function in_list( p_string in varchar2 ) return myTableType
    as
            type rc is ref cursor;
        l_cursor     rc;
        l_tmp        long;
        l_data       myTableType := myTableType();
    begin
      open l_cursor for p_string;
     loop
         fetch l_cursor into l_tmp;
         exit when l_cursor%notfound;
         l_data.extend;
         l_data(l_data.count) := l_tmp;
    end loop;
    close l_cursor;
    return l_data;
   end;
/