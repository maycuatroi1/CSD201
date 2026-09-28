package csd201.tree.binary;

import csd201.tree.TreeNode;
import java.util.ArrayList;
import java.util.List;

public interface BinaryNode<E> extends TreeNode<E> {

    BinaryNode<E> left();

    BinaryNode<E> right();

    default List<BinaryNode<E>> children() {
        List<BinaryNode<E>> children = new ArrayList<>(2);
        if (left() != null) {
            children.add(left());
        }
        if (right() != null) {
            children.add(right());
        }
        return children;
    }
}
