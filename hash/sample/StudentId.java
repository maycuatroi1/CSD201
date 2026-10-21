package hash.sample;

public final class StudentId {

    private final String code;

    public StudentId(String code) {
        this.code = code;
    }

    public boolean equals(Object o) {
        if (!(o instanceof StudentId)) {
            return false;
        }
        return code.equals(((StudentId) o).code);
    }

    public int hashCode() {
        return code.hashCode();
    }

    public String toString() {
        return code;
    }
}
