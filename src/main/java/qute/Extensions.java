package qute;

import io.quarkus.qute.TemplateExtension;

import java.time.ZonedDateTime;

@TemplateExtension
public class Extensions {

    static Boolean isAfterNow(ZonedDateTime dateTime) {
        return dateTime.isAfter(ZonedDateTime.now());
    }

    static Boolean hasTime(ZonedDateTime dateTime) {
        return dateTime.getHour() != 0 || dateTime.getMinute() != 0 || dateTime.getSecond() != 0;
    }

}
