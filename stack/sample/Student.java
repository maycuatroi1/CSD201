package stack.sample;

public final class Student {
    private final String name;
    private final String studentId;

    public Student(String name, String studentId) {
        this.name = name;
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public String getStudentId() {
        return studentId;
    }

    public String toString() {
        return name + " (" + studentId + ")";
    }
}
