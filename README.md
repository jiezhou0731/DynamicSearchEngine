## Dynamic Search Engine

This dynamic search engine is based on Solr. It implements Winwin search alogrithm and QCM search algorithm.

## Prerequisites

 * JDK >= 1.8
 * javac >= 1.8

## Setup
In home directory, use

	ant ivy-bootstrap

	ant compile

In home/solr directory, use

	ant server

## Run
In home directory, use

	solr/bin/./solr_org

URL:

	http://localhost:8983/solr/ 

## Dynamic Search
        Use "winwin" as request handler instead of "select"
