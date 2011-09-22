#!/bin/sh
#
# $Id: test_em.sh 953 2011-08-11 15:38:44Z abm65@FED.CCLRC.AC.UK $
#
export total=0
export return_code=0
for i in `cat list_of_examples.txt`
do
   echo __________ test: $i __________________
   ./test_one.sh $i
   return_code=$?
   total=`expr $total + $return_code`
   echo __________ return_code: $return_code __________________
done
[ $total = 0 ] && echo Result: success || echo Result: failure
exit $total
#
# - the end - 
#

