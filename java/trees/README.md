# Cây (Trees)

| Package | Class | Syllabus | Giáo trình |
|---|---|---|---|
| `csd201.tree.general` | `GeneralTree`, `FileSystemDemo` | 4.1 General Trees | 8.1 |
| `csd201.tree.binary` | `BinaryNode`, `BinaryTree`, `ExpressionTree`, `ExpressionTreeDemo` | 4.2 Binary Trees, 4.3 Implementing Trees, 4.4 Tree Traversal | 8.2 đến 8.4 |
| `csd201.tree.bst` | `BinarySearchTree`, `BinarySearchTreeDemo` | 4.5 Binary Search Trees | 11.1 |
| `csd201.tree.avl` | `AvlTree`, `AvlTreeDemo` | 4.6 Balanced Search Trees, 4.7 AVL Trees | 11.2, 11.3 |

## Nội dung từng phần

**Cây tổng quát.** `GeneralTree` cài đặt Tree ADT: `root`, `parent`, `children`, `numChildren`,
`isRoot`, `isLeaf`, `depth`, `height`, duyệt `preOrder` và `postOrder`. `FileSystemDemo` dùng cây
thư mục `cs16/` trên slide: preorder in ra danh sách thư mục, postorder cộng dung lượng từ dưới lên
(5K, 55K, 61K).

**Cây nhị phân.** `BinaryTree` là lớp cha chung của mọi cây nhị phân trong bài. Nó chứa bốn phép
duyệt (`preOrder`, `inOrder`, `postOrder`, `breadthFirst`), `size`, `height` và `toString`. Lớp con
chỉ cần cho biết nút gốc qua interface `BinaryNode`. `ExpressionTree` là cây biểu thức trong giáo
trình: postorder để tính giá trị (-13), inorder có ngoặc để in biểu thức, preorder và postorder
cho ký pháp tiền tố và hậu tố.

**Cây nhị phân tìm kiếm.** `BinarySearchTree` gồm `contains`, `insert`, `deleteByMerging`,
`deleteByCopying`, `min`, `max` và `balance` (lấy dãy inorder đã sắp xếp, chèn phần tử giữa trước).

**Cây AVL.** `AvlTree` gồm `contains`, `insert`, `delete` và `balanceFactor`. Sau mỗi lần chèn
hoặc xoá, mọi nút trên đường đi về gốc được cân bằng lại bằng phép quay đơn hoặc quay kép.
`AvlTree` không kế thừa `BinarySearchTree`, vì `deleteByMerging` phá điều kiện cân bằng và
không được phép có trên cây AVL.

## Quy ước (giống slide)

- Cây một nút cao 0, cây rỗng cao -1.
- Hệ số cân bằng: `bf = height(right) - height(left)`. Cây AVL giữ `bf` trong khoảng -1 đến 1.
  Khi một nút mất cân bằng, nếu nút con phía cao hơn lệch ngược chiều (`bf` khác dấu) thì quay kép,
  còn lại quay đơn.
- Xoá bằng copying (cả BST lẫn AVL) chép khoá của predecessor, tức nút phải nhất của cây con trái.
- `toString()` in cây theo dạng `8(3(1,6(4,7)),10(-,14(13,-)))`: gốc trước, trong ngoặc là cây
  con trái và cây con phải, `-` là con rỗng.

## Chạy

```bash
javac -d out $(find src -name "*.java")
java -cp out csd201.tree.general.FileSystemDemo
java -cp out csd201.tree.binary.ExpressionTreeDemo
java -cp out csd201.tree.bst.BinarySearchTreeDemo
java -cp out csd201.tree.avl.AvlTreeDemo
```

Cách chạy test và mở bằng IDE: xem [README ở thư mục gốc](../../README.md).
