package data;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

import java.util.List;

@DataMapping(value = "speakers", parentArray = true)
public record Speakers(List<Speaker> list) {

    public record Speaker(String id, String name, String profile, String bio) {}

    public Speaker get(String id) {
        return list.stream().filter(s -> s.id().equals(id)).findFirst().orElse(null);
    }
}
