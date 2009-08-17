#!/bin/bash
#Author Shoaib Sufi - June 2008 -  Changes for Devigo contract - use to generate icat data dictionary
#				   deliverable
#Author Shoaib Sufi - October 2007 - initial version

#pick out table names - just useful perhaps
#for i in `cat comments.txt | awk  'BEGIN {RS=";"} $1 ~ /CREATE/ { print $0 ";" }' -  | awk  'BEGIN {RS=";"} $2 ~ /TABLE/ { print $3 }' -`
#do
#echo $i
#done

#some names of files
export TMPF=data_dictionary.tmp
export FINAL_REP=data_dictionary.tex

#database connection information (assuming running script on the host)
export db_user=ICATISIS_TEST
export db_pass=fish4t

#init the result file
# top matter

#echo "\documentclass{article}" > $TMPF
#as report need to use chapeter->section->subsection not section->subsection->subsubsection
echo "\documentclass{report}" > $TMPF
echo "\usepackage{times}" >> $TMPF
echo "\usepackage{supertabular}" >> $TMPF
echo "\usepackage[a4paper]{geometry}" >> $TMPF

echo "\begin{document}" >> $TMPF

# title
echo "\title{ICAT 3.3 Data Dictionary}" >> $TMPF
echo "\author{Shoaib Sufi\\\\" >> $TMPF
echo " Devigo,\\\\" >> $TMPF
echo " Daresbury Science and Innovation Centre,\\\\" >> $TMPF
echo "  \texttt{shoaib.sufi@devigo.com}}" >> $TMPF
echo "\date{\today}" >> $TMPF
echo "\maketitle" >> $TMPF


#abstract
echo "\begin{abstract}" >> $TMPF
echo "This document is the data dictionary for ICAT 3.3.x; explaining the purpose of the tables and columns used in the schema." >> $TMPF
echo "\end{abstract}" >> $TMPF

#Introduction
echo "\chapter{Introduction}" >> $TMPF
echo "This document is created from the comments written during the development of the ICAT 3.3.x schema. The ICAT 3.3.x schema was developed using Oracle JDeveloper; the JDeveloper project files contain data definition, contraint definition, index definition, comments and a schema diagram. \
The JDeveloper project files for ICAT 3.3.x are available \
at time of writing from \\\\ https://esc-cvs.dl.ac.uk/svn/dl/metadata/icat/trunk/jdeveloper/icat, please contact STFC database services for access to \
these files by e-mailing databaseservices@stfc.ac.uk. " >> $TMPF

echo "\chapter{Tables}" >> $TMPF
#pick out table names

#so no line breaks and therefore spaces in output need to set lin may as well set long also.
sqlplus -S $db_user/$db_pass  > tables.txt <<EOF 
set head off  
set pages 1000 
set long 4000
set lin 4000
select distinct table_name from user_cons_columns order by table_name asc ; 
exit 
EOF

#all columns with comments for tables

for i in `cat tables.txt | grep -v 'rows selected' | grep -v '^BIN'`
do
echo $i
sqlplus -S $db_user/$db_pass  > columns_$i.txt <<EOF
set head off
set pages 1000
set long 4000
set lin 4000
rem select tc.column_name || '---' || trim(cc.comments) || '.' from user_tab_columns tc, user_col_comments cc where tc.table_name = '$i' and tc.column_name=cc.column_name and tc.table_name = cc.table_name ;
select column_name from user_tab_columns where table_name = '$i' order by column_id asc  ;
exit
EOF
echo "\section{$i}"  >> $TMPF

        #can page breaks be specified between new subsections
       
        #get comments pertaining to this particular table
        #echo "\paragraph{}" >> $TMPF -- made no different to space issue
	#sqlplus -S $db_user/$db_pass | sed 's/>/\\textgreater{}/g' | sed 's/</\\textless{}/g' >> $TMPF  <<-EOF --does not work puts
        # set ... etc in output - perhaps need ora function or just re-direct via a file.
	sqlplus -S $db_user/$db_pass  >> $TMPF  <<-EOF
        set head off
        set pages 1000
	set long 4000
	set lin 4000
        select nvl(trim(comments), 'None Available')||'\\\\' from user_tab_comments where table_name='$i' ;
        exit
	EOF

        # need some kind of spacing between start of the table and end of the paragraph before - solved with rik help
        # not liked give error : echo "\\\\" >> $TMPF 

        #fill array with column names
        #exclude oracle return message and audit columns - audit columns are to be in one place
        #export col_exclude_process='grep -v "rows selected" | grep -v "^MOD_" | grep -v "^CREATE_" | grep -v "^FACILITY_" | grep -v "DELETED"'
	export num_in_array=0
        declare -a columns
	for j in `cat columns_$i.txt | grep -v "rows selected" | grep -v "^MOD_" | grep -v "^CREATE_" | grep -v "^FACILITY_" | grep -v "DELETED" | grep -v "SEQ_NUMBER"`
	do
 		columns[${num_in_array}]=$j
		num_in_array=`expr $num_in_array + 1`
	done

        #for all the column names pull out the comments
        #echo "\begin{center}" >> $TMPF
        #echo "\begin{tabular*}{0.75\textwidth}{@{\extracolsep{\fill}} | l | l |  }" >> $TMPF

        if [[ "$i" == "INVESTIGATION" ]]
        then echo "\begin{supertabular}{|l|l|}" >> $TMPF
        else echo "\begin{tabular}{|l|l|}" >> $TMPF
        fi
        echo "\hline" >> $TMPF
        echo "Column Name & Comments \\\\ \hline" >> $TMPF

	export loop_var=0
	while [ $loop_var -lt $num_in_array ]
	do
		#echo "columns $loop_var value is -- ${columns[${loop_var}]}"
		sqlplus -S $db_user/$db_pass >  $i_${columns[${loop_var}]}.txt <<-EOF
		set head off 
                set pages 1000
		set long 4000
		set lin 4000
                select NVL(trim(comments), 'None Available') from user_col_comments where table_name = '$i' and column_name = '${columns[${loop_var}]}' ;
                exit 
		EOF
                echo "${columns[${loop_var}]} & \multicolumn{1}{p{100mm}|}{`cat $i_${columns[${loop_var}]}.txt | sed -e 's/&/\\\\&/g'`} \\\\ \hline" \
			| sed 's/>/\\textgreater{}/g' | sed 's/</\\textless{}/g' >> $TMPF
		loop_var=`expr $loop_var + 1`
                
	done
        #end of table for this set of columns
        if [[ "$i" == "INVESTIGATION" ]]
        then echo "\end{supertabular}" >> $TMPF
        else echo "\end{tabular}" >> $TMPF 
        fi

done # processed all tables

###################

##################


echo "\end{document}" >> $TMPF

#do some escaping of offending characters to please latex -- possible issue & in formatting vs & in text latter needs escaping former does not
#what do do - well have to process comments as we get them
sed -e 's/_/\\_/g' $TMPF  > $FINAL_REP

#create the pdf file
latex $FINAL_REP
export FR_NO_EXT=`echo $FINAL_REP | cut -d. -f1`
dvipdf $FR_NO_EXT.dvi $FR_NO_EXT.pdf


