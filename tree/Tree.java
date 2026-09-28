package tree;

import java.util.List;

public interface Tree<T> {

    Node<T> getRoot();

    boolean isEmpty();

    int size();

    int height();

    List<T> preOrder();

    List<T> inOrder();

    List<T> postOrder();

    List<T> levelOrder();

    Position<T> search(T value);

    int getSearchCount();

    void printTree();
}
