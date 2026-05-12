package data;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

import java.util.List;

@DataMapping(value = "authors", parentArray = true)
public record Authors(List<Author> list) {

    public record Author(String id, String name, String job, String nickname, String profile, String avatar, String bio) {}

    public Author get(String id) {
        return list.stream().filter(a -> a.id().equals(id)).findFirst().orElse(null);
    }
}
