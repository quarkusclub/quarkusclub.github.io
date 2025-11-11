---
title: "Martin Stefanko - Is Loom really the future of enterprise Java?"
layout: event
youtubeLive: https://www.youtube.com/watch?v=l05WW788zlQ
date: 2024-05-17 00:00:00 -0300
description: | 
 Project Loom, or virtual threads, promised fast, lightweight user-space threads that are very cheap to block. While this is true, everything in life comes at a price. Virtual threads allow users to not care about it. It is the job of the underlying libraries that all our applications use. Issues that can still occur with virtual threads, such as pinning, monopolization, or large thread-local objects, still present real-world problems many libraries still need to account for. Especially in enterprises, these issues might only be noticed once the system reaches the peak loads, which is usually too late. In this session, we explain the virtual thread model execution and compare it to the event loop/reactive model utilized in Quarkus. We will also dive into individual problems that virtual threads might encounter and demonstrate how you can verify that your code doesn’t run into them. By the end of the talk, you’ll understand these (from the user point of view invisible) potential issues with Project Loom.
speakers: [xstefank]
draft: false
---