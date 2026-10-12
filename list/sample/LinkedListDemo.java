package list.sample;

import list.LinkedList;
import list.Node;

public class LinkedListDemo {
    public static void main(String[] args) {
        LinkedList<String> playlist = new LinkedList<>();
        playlist.append("Sunrise");
        playlist.append("Neon Drive");
        playlist.append("Echo Park");
        System.out.println("append x3            " + playlist);

        playlist.addFirst("Intro");
        System.out.println("addFirst(Intro)      " + playlist);

        playlist.insertAt(2, "Aurora");
        System.out.println("insertAt(2, Aurora)  " + playlist);

        Node<String> neonDrive = playlist.getByIndex(3);
        System.out.println("Previous button on " + neonDrive.getData() + " plays " + neonDrive.getPrev().getData());

        String removed = playlist.deleteAt(playlist.getSize() - 1);
        System.out.println("deleteAt(last)       " + playlist + ", removed " + removed
                + ", tail is " + playlist.getTail().getData());

        playlist.deleteAt(0);
        System.out.println("deleteAt(0)          " + playlist + ", head is " + playlist.getHead().getData());

        System.out.print("for-each            ");
        for (String track : playlist) {
            System.out.print(" " + track);
        }
        System.out.println();
    }
}
