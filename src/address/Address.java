package address;
public class Address {
    private final int id;
    private final String city;
    private final String country;
    private final String created_at;

    public Address(int id, String city, String country, String createdAt) {
        this.id = id;
        this.city = city;
        this.country = country;
        created_at = createdAt;
    }

    public int getId() {
        return id;
    }
    @Override
    public String toString() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getCreated_at() {
        return created_at;
    }
}
