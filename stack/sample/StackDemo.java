package stack.sample;

import java.util.EmptyStackException;
import stack.ArrayStack;
import stack.LinkStack;
import stack.Stack;

public class StackDemo {
    public static void main(String[] args) {
        predict(new ArrayStack<>(new Integer[4]));
        predict(new LinkStack<Integer>());
        System.out.println();

        Stack<Student> trolley = new ArrayStack<>(new Student[6]);
        trolley.push(new Student("Alice", "S001"));
        trolley.push(new Student("Bob", "S002"));
        trolley.push(new Student("Charlie", "S003"));
        trolley.push(new Student("David", "S004"));
        trolley.push(new Student("Binh", "S005"));
        System.out.println("top to bottom  " + trolley);
        System.out.println("pop()          " + trolley.pop());
        System.out.println("peek()         " + trolley.peek());
        System.out.println("array slots    " + trolley.getStorage());

        while (!trolley.isEmpty()) {
            trolley.pop();
        }
        try {
            trolley.pop();
        } catch (EmptyStackException e) {
            System.out.println("pop() on an empty stack throws EmptyStackException");
        }
    }

    private static void predict(Stack<Integer> stack) {
        stack.push(10);
        stack.push(20);
        int a = stack.pop();
        stack.push(30);
        int b = stack.peek();
        System.out.println(stack.getClass().getSimpleName() + ": a = " + a + ", b = " + b
                + ", top to bottom " + stack);
    }
}
