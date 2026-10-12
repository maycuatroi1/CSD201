package list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;

public class LinkedListTest {

    @Test
    public void appendLinksEveryNodeBothWays() {
        LinkedList<String> playlist = listOf("Sunrise", "Neon Drive", "Echo Park");
        assertEquals(Arrays.asList("Sunrise", "Neon Drive", "Echo Park"), headToTail(playlist));
        assertEquals(Arrays.asList("Echo Park", "Neon Drive", "Sunrise"), tailToHead(playlist));
        assertNull(playlist.getHead().getPrev());
        assertNull(playlist.getTail().getNext());
        assertEquals(3, playlist.getSize());
    }

    @Test
    public void insertAtPutsTheTrackBeforeTheOneAtThatIndex() {
        LinkedList<String> playlist = listOf("Intro", "Sunrise", "Neon Drive", "Echo Park");
        playlist.insertAt(2, "Aurora");
        assertEquals(Arrays.asList("Intro", "Sunrise", "Aurora", "Neon Drive", "Echo Park"), headToTail(playlist));
        assertEquals(Arrays.asList("Echo Park", "Neon Drive", "Aurora", "Sunrise", "Intro"), tailToHead(playlist));
        assertEquals(5, playlist.getSize());
    }

    @Test
    public void addFirstAndInsertAtSizeMoveTheEnds() {
        LinkedList<String> playlist = listOf("Sunrise");
        playlist.addFirst("Intro");
        playlist.insertAt(playlist.getSize(), "Echo Park");
        assertEquals("Intro", playlist.getHead().getData());
        assertEquals("Echo Park", playlist.getTail().getData());
        assertEquals(Arrays.asList("Echo Park", "Sunrise", "Intro"), tailToHead(playlist));
    }

    @Test
    public void addFirstOnAnEmptyListSetsHeadAndTail() {
        LinkedList<String> playlist = new LinkedList<>();
        playlist.addFirst("Sunrise");
        assertSame(playlist.getHead(), playlist.getTail());
        assertEquals(1, playlist.getSize());
    }

    @Test
    public void deleteAtTheTailMovesTailOneStepBack() {
        LinkedList<String> playlist = listOf("Sunrise", "Neon Drive", "Echo Park");
        assertEquals("Echo Park", playlist.deleteAt(2));
        assertEquals("Neon Drive", playlist.getTail().getData());
        assertNull(playlist.getTail().getNext());
        assertEquals(Arrays.asList("Neon Drive", "Sunrise"), tailToHead(playlist));
    }

    @Test
    public void deleteAtTheMiddleRepairsBothLinks() {
        LinkedList<String> playlist = listOf("Sunrise", "Neon Drive", "Echo Park");
        assertEquals("Neon Drive", playlist.deleteAt(1));
        assertEquals(Arrays.asList("Sunrise", "Echo Park"), headToTail(playlist));
        assertEquals(Arrays.asList("Echo Park", "Sunrise"), tailToHead(playlist));
    }

    @Test
    public void deleteAtTheHeadClearsThePrevOfTheNewHead() {
        LinkedList<String> playlist = listOf("Sunrise", "Neon Drive", "Echo Park");
        assertEquals("Sunrise", playlist.deleteAt(0));
        assertEquals("Neon Drive", playlist.getHead().getData());
        assertNull(playlist.getHead().getPrev());
    }

    @Test
    public void deletingTheOnlyNodeLeavesHeadAndTailNull() {
        LinkedList<String> playlist = listOf("Sunrise");
        assertEquals("Sunrise", playlist.deleteAt(0));
        assertTrue(playlist.isEmpty());
        assertNull(playlist.getHead());
        assertNull(playlist.getTail());
        playlist.append("Echo Park");
        assertSame(playlist.getHead(), playlist.getTail());
        assertEquals("Echo Park", playlist.getHead().getData());
    }

    @Test
    public void aDeletedNodeKeepsNoLinks() {
        LinkedList<String> playlist = listOf("Sunrise", "Neon Drive", "Echo Park");
        Node<String> neonDrive = playlist.getByIndex(1);
        playlist.deleteAt(1);
        assertNull(neonDrive.getPrev());
        assertNull(neonDrive.getNext());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getByIndexAtTheSizeThrows() {
        listOf("Sunrise", "Neon Drive").getByIndex(2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void insertAtANegativeIndexThrows() {
        listOf("Sunrise").insertAt(-1, "Intro");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void deleteAtOnAnEmptyListThrows() {
        new LinkedList<String>().deleteAt(0);
    }

    @Test(expected = NoSuchElementException.class)
    public void theIteratorStopsAtTheTail() {
        Iterator<String> tracks = listOf("Sunrise").iterator();
        tracks.next();
        tracks.next();
    }

    @Test
    public void forEachWalksFromHeadToTail() {
        ArrayList<String> visited = new ArrayList<>();
        for (String track : listOf("Sunrise", "Neon Drive", "Echo Park")) {
            visited.add(track);
        }
        assertEquals(Arrays.asList("Sunrise", "Neon Drive", "Echo Park"), visited);
    }

    @Test
    public void toStringShowsTheTracksInOrder() {
        assertEquals("[Sunrise <-> Neon Drive]", listOf("Sunrise", "Neon Drive").toString());
        assertEquals("[]", new LinkedList<String>().toString());
    }

    private static LinkedList<String> listOf(String... tracks) {
        LinkedList<String> playlist = new LinkedList<>();
        for (String track : tracks) {
            playlist.append(track);
        }
        return playlist;
    }

    private static ArrayList<String> headToTail(LinkedList<String> playlist) {
        ArrayList<String> tracks = new ArrayList<>();
        for (Node<String> node = playlist.getHead(); node != null; node = node.getNext()) {
            tracks.add(node.getData());
        }
        return tracks;
    }

    private static ArrayList<String> tailToHead(LinkedList<String> playlist) {
        ArrayList<String> tracks = new ArrayList<>();
        for (Node<String> node = playlist.getTail(); node != null; node = node.getPrev()) {
            tracks.add(node.getData());
        }
        return tracks;
    }
}
