#!/bin/sh
asadmin create-jms-resource --restype javax.jms.QueueConnectionFactory jms/icat/QueueConnectionFactory
asadmin create-jms-resource --restype javax.jms.Queue jms/advanced/Queue
asadmin create-jms-resource --restype javax.jms.Queue jms/login/Queue
asadmin create-jms-resource --restype javax.jms.Queue jms/logout/Queue
asadmin create-jms-resource --restype javax.jms.Queue jms/keywords/Queue
asadmin create-jms-resource --restype javax.jms.Queue jms/runnumber/Queue
asadmin create-jms-resource --restype javax.jms.Queue jms/download/Queue
asadmin create-jms-resource --restype javax.jms.Queue jms/sample/Queue
asadmin create-jms-resource --restype javax.jms.Queue jms/datafile/Queue
asadmin create-jms-resource --restype javax.jms.Queue jms/dataset/Queue
asadmin create-jms-resource --restype javax.jms.Queue jms/investigations/Queue
asadmin create-jms-resource --restype javax.jms.Queue jms/myinvestigations/Queue

asadmin create-jms-resource  --restype javax.jms.QueueConnectionFactory jms/ICATQueueConnectionFactory
asadmin create-jms-resource  --restype javax.jms.TopicConnectionFactory jms/ICATTopicConnectionFactory
asadmin create-jms-resource --restype javax.jms.Queue jms/ICATQueue
asadmin create-jms-resource --restype javax.jms.Topic jms/ICATTopic
