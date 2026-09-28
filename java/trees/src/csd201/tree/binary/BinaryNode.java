package csd201.tree.binary;

public interface BinaryNode<E> {

    E element();

    BinaryNode<E> left();

    BinaryNode<E> right();

    default boolean isLeaf() {
        return left() == null && right() == null;
    }
}
