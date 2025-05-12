---
title: Next events
layout: :theme/page
image: quarkusclub.jpeg
description: See all upcoming events hosted by Quarkus Club
---

# Next events

{#for event in cdi:next-events.events}
## {event.title}

Speakers:

{#for speaker in event.get("speakers") }
* {speaker.name} {#if speaker.title??} - {speaker.title??}{/if}
{/for}

| Date         | Location         | Link                                                                               | Language         |
|--------------|------------------|------------------------------------------------------------------------------------|------------------|
| {event.date} | {event.location} | <a href="{event.link}" target="_blank" rel="noopener noreferrer">{event.title}</a> | {event.language} |

{#if event.slides??}
<i class="fa-solid fa-person-chalkboard"></i> Link para os slides <a href="{event.slides}" target="_blank" rel="noopener noreferrer">aqui</a>
{/if}
{/for}

