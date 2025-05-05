---
title: Next events
layout: :theme/page
image: quarkusclub.jpeg
---

# Next events

{#for event in cdi:next-events.events}
## {event.title}

Speakers:

{#for speaker in event.get("speakers") }
* {speaker.name} {#if speaker.title??} - {speaker.title??}{/if}
{/for}

| Date         | Location         | Link                          | Language         |
|--------------|------------------|-------------------------------|------------------|
| {event.date} | {event.location} | [{event.title}]({event.link}) | {event.language} |

{/for}