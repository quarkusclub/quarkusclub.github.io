---
title: About
description: |
  Roq is a powerful static site generator that combines the best features of tools like Jekyll and Hugo, but within the Java ecosystem. It offers a modern approach with Quarkus at its core, requiring zero configuration to get started —ideal for developers who want to jump right in, while still being flexible enough for advanced users to hook into Java for deeper customization.
layout: :theme/page
---

## About Quarkus Club

Quarkus Club is a community of developers passionate about modern Java, cloud-native architectures, and the Quarkus ecosystem.
## Authors

<div class="authors">
  <!-- authors.yml is in the data/ -->
  {#for id in cdi:authors.fields}
    {#let author=cdi:authors.get(id)}
    <!-- the author-card tag is defined in the default Roq theme -->
    {#author-card name=author.name avatar=author.avatar nickname=author.nickname profile=author.profile /}
  {/for}
</div>

