#!/bin/sh

props=./glassfish.props
if [ ! -f $props ]; then
    echo There is no $props file
    exit 1
fi

. $props

for key in icatProperties driver port; do
    eval val='$'$key
    if [ -z "$val" ]; then
        echo $key must be set in $props file
        exit 1
    fi
done

asadmin="$glassfish/bin/asadmin --port $port"

$asadmin set server.http-service.access-log.format="common"
$asadmin set server.http-service.access-logging-enabled=true

$asadmin create-jdbc-connection-pool \
   --datasourceclassname ${driver} --restype javax.sql.DataSource \
   --failconnection=true --steadypoolsize 2 --maxpoolsize 8 --ping \
   --property ${icatProperties} \
   icat
$asadmin create-jdbc-resource --connectionpoolid icat jdbc/icat

$asadmin create-jms-resource --restype javax.jms.QueueConnectionFactory jms/ICATQueueConnectionFactory
$asadmin create-jms-resource --restype javax.jms.TopicConnectionFactory jms/ICATTopicConnectionFactory
$asadmin create-jms-resource --restype javax.jms.Queue jms/ICATQueue
$asadmin create-jms-resource --restype javax.jms.Topic jms/ICATTopic

$asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=128
