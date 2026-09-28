package tree.general;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import tree.general.GeneralTree.Node;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class GeneralTreeTest {

    private final GeneralTree<String> tree = new GeneralTree<>();
    private final Node<String> cs16 = tree.addRoot("cs16/");
    private final Node<String> homeworks = tree.addChild(cs16, "homeworks/");
    private final Node<String> h1c = tree.addChild(homeworks, "h1c.doc");
    private final Node<String> h1nc = tree.addChild(homeworks, "h1nc.doc");
    private final Node<String> programs = tree.addChild(cs16, "programs/");
    private final Node<String> ddr = tree.addChild(programs, "DDR.java");
    private final Node<String> stocks = tree.addChild(programs, "Stocks.java");
    private final Node<String> robot = tree.addChild(programs, "Robot.java");
    private final Node<String> todo = tree.addChild(cs16, "todo.txt");

    @Test
    public void sizeCountsEveryNode() {
        assertEquals(9, tree.size());
        assertFalse(tree.isEmpty());
    }

    @Test
    public void rootHasNoParent() {
        assertTrue(tree.isRoot(cs16));
        assertFalse(tree.parent(cs16).isPresent());
        assertEquals(programs, tree.parent(stocks).get());
    }

    @Test
    public void childrenKeepInsertionOrder() {
        assertEquals(Arrays.asList(ddr, stocks, robot), tree.children(programs));
        assertEquals(3, tree.numChildren(cs16));
    }

    @Test
    public void depthCountsAncestors() {
        assertEquals(0, tree.depth(cs16));
        assertEquals(1, tree.depth(todo));
        assertEquals(2, tree.depth(stocks));
    }

    @Test
    public void heightIsMeasuredInsideTheSubtree() {
        assertEquals(2, tree.height());
        assertEquals(2, tree.height(cs16));
        assertEquals(1, tree.height(homeworks));
        assertEquals(0, tree.height(todo));
    }

    @Test
    public void leafMeansNoChildrenNotLowestLevel() {
        assertTrue(tree.isLeaf(todo));
        assertTrue(tree.isLeaf(h1nc));
        assertFalse(tree.isLeaf(homeworks));
    }

    @Test
    public void preOrderVisitsParentBeforeChildren() {
        assertEquals(
                Arrays.asList("cs16/", "homeworks/", "h1c.doc", "h1nc.doc", "programs/",
                        "DDR.java", "Stocks.java", "Robot.java", "todo.txt"),
                tree.preOrder());
    }

    @Test
    public void postOrderVisitsChildrenBeforeParent() {
        assertEquals(
                Arrays.asList("h1c.doc", "h1nc.doc", "homeworks/", "DDR.java", "Stocks.java",
                        "Robot.java", "programs/", "todo.txt", "cs16/"),
                tree.postOrder());
    }

    @Test
    public void levelOrderVisitsLevelByLevel() {
        assertEquals(
                Arrays.asList("cs16/", "homeworks/", "programs/", "todo.txt", "h1c.doc",
                        "h1nc.doc", "DDR.java", "Stocks.java", "Robot.java"),
                tree.levelOrder());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void childrenListIsReadOnly() {
        tree.children(cs16).add(h1c);
    }

    @Test(expected = IllegalStateException.class)
    public void secondRootIsRejected() {
        tree.addRoot("another/");
    }

    @Test
    public void emptyTreeHasNoRootAndHeightMinusOne() {
        GeneralTree<String> empty = new GeneralTree<>();
        assertTrue(empty.isEmpty());
        assertNull(empty.root());
        assertEquals(0, empty.size());
        assertEquals(-1, empty.height());
        assertEquals(Collections.emptyList(), empty.preOrder());
        assertEquals(Collections.emptyList(), empty.postOrder());
    }
}
