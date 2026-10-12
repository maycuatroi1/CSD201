package queue.sample;

import queue.ArrayQueue;
import queue.Queue;

public class QueueDemo {
    public static void main(String[] args) {
        Queue<String> orders = new ArrayQueue<>(new String[5]);
        orders.enqueue("A");
        orders.enqueue("B");
        String a = orders.dequeue();
        orders.enqueue("C");
        String b = orders.peek();
        System.out.println("a = " + a + ", b = " + b + ", size = " + orders.getSize() + ", front to back " + orders);
        System.out.println();

        Queue<String> ring = new ArrayQueue<>(new String[5]);
        for (String order : new String[] {"A", "B", "C", "D", "E"}) {
            ring.enqueue(order);
        }
        ring.dequeue();
        ring.dequeue();
        ring.enqueue("F");
        ring.enqueue("G");
        System.out.println("array slots    " + ring.getStorage());
        System.out.println("front to back  " + ring);
        try {
            ring.enqueue("H");
        } catch (IllegalStateException e) {
            System.out.println("enqueue(H)     " + e.getMessage() + ", still " + ring);
        }
        System.out.println();

        Queue<Student> line = new ArrayQueue<>(new Student[10]);
        line.enqueue(new Student("Alice", "S001"));
        line.enqueue(new Student("Bob", "S002"));
        line.enqueue(new Student("Charlie", "S003"));
        System.out.println("front to back  " + line);
        while (!line.isEmpty()) {
            System.out.println("dequeue()      " + line.dequeue());
        }
        System.out.println("dequeue()      " + line.dequeue() + ", peek() " + line.peek());
    }
}
