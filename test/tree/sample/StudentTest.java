package tree.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import tree.AVLTree;
import tree.BinaryTree;
import java.util.Arrays;
import org.junit.Test;

public class StudentTest {

    @Test
    public void studentsAreIdentifiedByIdOnly() {
        assertEquals(new Student("Minh", 42), Student.withId(42));
        assertEquals(new Student("Minh", 42).hashCode(), Student.withId(42).hashCode());
        assertEquals(0, new Student("Minh", 42).compareTo(Student.withId(42)));
        assertNotEquals(new Student("Minh", 42), Student.withId(43));
    }

    @Test
    public void everyTreeFindsAStudentById() {
        assertFindsMinhById(new BinaryTree<Student>());
        assertFindsMinhById(new AVLTree<Student>());
    }

    private static void assertFindsMinhById(BinaryTree<Student> tree) {
        tree.buildTree(Arrays.asList(new Student("Alice", 1), new Student("Minh", 42), new Student("An", 43)));
        assertEquals("Minh", tree.search(Student.withId(42)).getNode().getData().getName());
    }
}
