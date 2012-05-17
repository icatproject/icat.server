#!/bin/sh

port=8020

wget http://localhost:$port/ICATService/ICAT?wsdl -O ICAT.wsdl
wget http://localhost:$port/ICATService/ICAT?xsd=1 -O ICAT.xsd

sed  s#http://localhost:$port/ICATService/ICAT?xsd=1#ICAT.xsd# ICAT.wsdl > ICAT.wsdl.new && mv ICAT.wsdl.new ICAT.wsdl
sed 's#ref="tns:\([a-z]*\)"#name="\1" type="tns:\1"#' ICAT.xsd > ICAT.xsd.new && mv ICAT.xsd.new ICAT.xsd

wget http://localhost:$port/ICATCompatService/ICATCompat?wsdl -O ICATCompat.wsdl
wget http://localhost:$port/ICATCompatService/ICATCompat?xsd=1 -O ICATCompat1.xsd
wget http://localhost:$port/ICATCompatService/ICATCompat?xsd=2 -O ICATCompat2.xsd

sed  s#http://localhost:$port/ICATCompatService/ICATCompat?xsd=1#ICATCompat1.xsd# ICATCompat.wsdl > ICATCompat.wsdl.new && mv ICATCompat.wsdl.new ICATCompat.wsdl
sed  s#http://localhost:$port/ICATCompatService/ICATCompat?xsd=2#ICATCompat2.xsd# ICATCompat.wsdl > ICATCompat.wsdl.new && mv ICATCompat.wsdl.new ICATCompat.wsdl
sed 's#ref="tns:\([a-z]*\)"#name="\1" type="tns:\1"#' ICATCompat1.xsd > ICATCompat1.xsd.new && mv ICATCompat1.xsd.new ICATCompat1.xsd
sed  s#http://localhost:$port/ICATCompatService/ICATCompat?xsd=2#ICATCompat2.xsd# ICATCompat1.xsd > ICATCompat1.xsd.new && mv ICATCompat1.xsd.new ICATCompat1.xsd
sed 's#ref="tns:\([a-z]*\)"#name="\1" type="tns:\1"#' ICATCompat2.xsd > ICATCompat2.xsd.new && mv ICATCompat2.xsd.new ICATCompat2.xsd
sed  s#http://localhost:$port/ICATCompatService/ICATCompat?xsd=1#ICATCompat1.xsd# ICATCompat2.xsd > ICATCompat2.xsd.new && mv ICATCompat2.xsd.new ICATCompat2.xsd
