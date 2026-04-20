---
title: "Open J Proxy (OJP): Rethinking JDBC with Smart Connection Management for Quarkus"
layout: event
date: 2026-04-23 16:00:00 -0300
youtubeLive: https://www.youtube.com/watch?v=XawiVocjGTo
description: |
  Modern Quarkus services often face database bottlenecks when microservices workloads scale elastically. Traditional JDBC connection pools work well inside a single service but break down when many of the pools compete for database connections.
  
  This session introduces Open J Proxy (OJP), an open-source, database-agnostic proxy that also has as a drop-in JDBC driver for convenience. OJP centralizes connection management and adds resilience features, including: 
  
  Backpressure to prevent connection storms Circuit Breaker to stop failing queries from hammering the database
  We’ll walk through OJP’s architecture and demo integration with Quarkus. 
speakers: [rogeriorobetti]
language: en-GB
draft: false
---
