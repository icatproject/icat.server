#!/bin/bash
for i in `ls * | grep -v notes.txt | grep -v devigo_to_do.txt | grep  -v tidy.sh \
          | grep -v create_db | grep -v create_doc.sh | grep -v data_dictionary.pdf | grep -v data_dictionary.tex`; do rm -f $i; done
