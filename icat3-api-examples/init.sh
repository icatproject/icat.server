#!/bin/bash
#
# $Id: init.sh 953 2011-08-11 15:38:44Z abm65@FED.CCLRC.AC.UK $
#
if [ -z $ICAT_UTIL_HOME ] ; then
        echo "Please set ICAT_UTIL_HOME variable."
        echo ""
        echo "The following may be useful..."
        echo ""
        echo export ICAT_UTIL_HOME=../../icat_cmd_util
        echo export PATH=\$PATH:scripts
        exit 1;
fi

cd $ICAT_UTIL_HOME

result=`icat_cmd.sh createDataSet -name test_name_$RANDOM -investigationId 1 -datasetType GS | grep "id="`
datasetId=`echo $result | awk '{ split($1, a, "="); print a[2] }'`

result=`icat_cmd.sh createDataFile -name test_file_$RANDOM -datasetId $datasetId | grep "id="`
datafileId=`echo $result | grep "id=" | awk '{ split($1, a, "="); print a[2] }'`

cd - > /dev/null

echo "#Please add the following entries to example.properties:"
echo datasetId=$datasetId
echo datafileId=$datafileId

#
# - the end -
#

