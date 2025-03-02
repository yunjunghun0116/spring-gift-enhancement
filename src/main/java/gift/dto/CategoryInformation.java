package gift.dto;

public record CategoryInformation(Long id, String name) {
    public static CategoryInformation of(Long id, String name) {
        return new CategoryInformation(id, name);
    }
}
