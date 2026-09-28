package tree.sample;

public final class Student implements Comparable<Student> {
    private final String name;
    private final int id;

    public Student(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public static Student withId(int id) {
        return new Student("", id);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int compareTo(Student other) {
        return Integer.compare(id, other.id);
    }

    public boolean equals(Object other) {
        return other instanceof Student && id == ((Student) other).id;
    }

    public int hashCode() {
        return Integer.hashCode(id);
    }

    public String toString() {
        return name + "(" + id + ")";
    }
}
