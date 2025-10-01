package managers;

public class Manager {
    private final int manger_id;
    private final String email;
    private final String name;
    public Manager(int manger_id, String email, String name) {
        this.manger_id = manger_id;
        this.email = email;
        this.name = name;
    }
    public int getManger_id() {
        return manger_id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
    @Override
    public String toString() {
        return name;
    }


}
