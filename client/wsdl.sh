#!/bin/sh
wget http://localhost:8020/ICATService/ICAT?wsdl -O ICAT.wsdl
wget http://localhost:8020/ICATService/ICAT?xsd=1 -O ICAT.xsd

sed  s#http://localhost:8020/ICATService/ICAT?xsd=1#ICAT.xsd# ICAT.wsdl > ICAT.wsdl.new && mv ICAT.wsdl.new ICAT.wsdl
sed 's#ref="tns:\([a-z]*\)"#name="\1" type="tns:\1"#' ICAT.xsd > ICAT.xsd.new && mv ICAT.xsd.new ICAT.xsd
