package csd201.tree;

import java.util.List;

public interface TreeNode<E> {

    E element();

    List<? extends TreeNode<E>> children();

    default boolean isLeaf() {
        return children().isEmpty();
    }
}
