package qute;

import io.quarkus.qute.TemplateExtension;

import java.time.ZonedDateTime;

@TemplateExtension
public class Extensions {

    static Boolean isAfterNow(ZonedDateTime dateTime) {
        return dateTime.isAfter(ZonedDateTime.now());
    }

}
