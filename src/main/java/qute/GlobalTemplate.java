package qute;

import io.quarkus.qute.TemplateGlobal;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@TemplateGlobal
public class GlobalTemplate {

    public static DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.of("pt", "BR"));
}
