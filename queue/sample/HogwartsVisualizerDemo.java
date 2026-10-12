package queue.sample;

import queue.ArrayQueue;
import queue.HogwartsVisualizer;
import queue.Queue;
import stack.LinkStack;
import stack.Stack;

public class HogwartsVisualizerDemo {
    public static void main(String[] args) {
        Queue<Student> studentQueue = HogwartsVisualizer.watch(new ArrayQueue<>(new Student[10]));
        studentQueue.enqueue(new Student("Alice", "S001"));
        studentQueue.enqueue(new Student("Bob", "S002"));
        studentQueue.enqueue(new Student("Charlie", "S003"));
        studentQueue.enqueue(new Student("David", "S004"));
        studentQueue.enqueue(new Student("Binh", "S005"));
        System.out.println(studentQueue);
        System.out.println(studentQueue.dequeue());

        Stack<Student> studentStack = HogwartsVisualizer.watch(new LinkStack<>());
        studentStack.push(new Student("Alice", "S001"));
        studentStack.push(new Student("Bob", "S002"));
        studentStack.push(new Student("Charlie", "S003"));
        studentStack.push(new Student("David", "S004"));
        studentStack.push(new Student("Binh", "S005"));
        System.out.println(studentStack);
        System.out.println(studentStack.pop());
    }
}
