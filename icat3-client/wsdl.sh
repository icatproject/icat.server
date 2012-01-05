#!/bin/sh
wget http://localhost:8080/ICATService/ICAT?wsdl -O ICAT.wsdl
wget http://localhost:8080/ICATService/ICAT?xsd=1 -O ICAT.xsd

sed  s#http://localhost:8080/ICATService/ICAT?xsd=1#ICAT.xsd# ICAT.wsdl > ICAT.wsdl.new && mv ICAT.wsdl.new ICAT.wsdl
sed  's#ref="tns:dataset"#name="dataset" type="tns:dataset"#' ICAT.xsd > ICAT.xsd.new && mv ICAT.xsd.new ICAT.xsd
sed  's#ref="tns:investigation"#name="investigation" type="tns:investigation"#' ICAT.xsd > ICAT.xsd.new && mv ICAT.xsd.new ICAT.xsd
sed  's#ref="tns:datafile"#name="datafile" type="tns:datafile"#' ICAT.xsd > ICAT.xsd.new && mv ICAT.xsd.new ICAT.xsd




