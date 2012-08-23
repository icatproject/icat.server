#!/bin/sh

props=./glassfish.props
if [ ! -f $props ]; then
    echo There is no $props file
    exit 1
fi

. $props

for key in glassfish port; do
    eval val='$'$key
    if [ -z "$val" ]; then
        echo $key must be set in $props file
        exit 1
    fi
done

asadmin="$glassfish/bin/asadmin --port $port"

$asadmin delete-jdbc-resource jdbc/icat

$asadmin delete-jdbc-connection-pool icat

$asadmin delete-jms-resource jms/ICATQueueConnectionFactory
$asadmin delete-jms-resource jms/ICATTopicConnectionFactory
$asadmin delete-jms-resource jms/ICATQueue
$asadmin delete-jms-resource jms/ICATTopic


