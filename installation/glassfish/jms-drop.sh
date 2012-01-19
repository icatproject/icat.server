#!/bin/sh
asadmin delete-jms-resource jms/icat/QueueConnectionFactory
asadmin delete-jms-resource jms/advanced/Queue
asadmin delete-jms-resource jms/login/Queue
asadmin delete-jms-resource jms/logout/Queue
asadmin delete-jms-resource jms/keywords/Queue
asadmin delete-jms-resource jms/runnumber/Queue
asadmin delete-jms-resource jms/download/Queue
asadmin delete-jms-resource jms/sample/Queue
asadmin delete-jms-resource jms/datafile/Queue
asadmin delete-jms-resource jms/dataset/Queue
asadmin delete-jms-resource jms/investigations/Queue
asadmin delete-jms-resource jms/myinvestigations/Queue

asadmin delete-jms-resource jms/ICATQueueConnectionFactory
asadmin delete-jms-resource jms/ICATTopicConnectionFactory
asadmin delete-jms-resource jms/ICATQueue
asadmin delete-jms-resource jms/ICATTopic
