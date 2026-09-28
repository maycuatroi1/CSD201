# Cây (Trees)

| Package | Class | Syllabus | Giáo trình |
|---|---|---|---|
| `tree` | `Tree`, `AbstractTree`, `Node`, `Position` | 4.1.2 Tree ADT, 4.3 Implementing Trees | 8.1.2, 8.3 |
| `tree` | `BinaryTree`, `ExpressionTree` | 4.2 Binary Trees, 4.4 Tree Traversal | 8.2, 8.4 |
| `tree` | `BinarySearchTree` | 4.5 Binary Search Trees | 11.1 |
| `tree` | `AVLTree` | 4.6 Balanced Search Trees, 4.7 AVL Trees | 11.2, 11.3 |
| `tree.general` | `GeneralTree` | 4.1 General Trees | 8.1 |
| `tree.sample` | `Student` và các class `*Demo` | chạy thử | |

## Cây kế thừa

```
Tree<T>                              interface: getRoot, isEmpty, size, height,
│                                    preOrder, inOrder, postOrder, levelOrder,
│                                    search, getSearchCount, printTree
└── AbstractTree<T>                  cài đặt chung cho mọi cây nhị phân
    ├── ExpressionTree               evaluate, toInfix
    └── BinaryTree<T>                buildTree, insert, remove, getSibling (điền theo từng tầng)
        └── BinarySearchTree<T>      + deleteByCopying, deleteByMerging, mergeTrees, balance, min, max
            └── AVLTree<T>           + balanceFactor, tự cân bằng sau insert và remove
```

`GeneralTree` (cây tổng quát, mỗi nút có nhiều con) không có `inOrder` và không có con trái,
con phải, nên đứng riêng, không cài đặt `Tree`.

## Các class chính

**`Node<T>`**: dữ liệu, con trái, con phải và chiều cao của cây con tại nút đó. Bên ngoài chỉ đọc
được qua getter. Setter chỉ dùng được trong package `tree`, nên code bên ngoài không phá
được thứ tự của BST. Chiều cao được lưu sẵn trong nút và cập nhật sau mỗi lần thêm, xoá hay
quay, nên `height()` chỉ tốn O(1).

**`Position<T>`**: một vị trí trong cây, gồm nút, vị trí cha và nút đó là con trái hay con phải.
`search` trả về `Position`. Nếu không tìm thấy thì `isEmpty()` là `true`. Riêng BST còn cho biết
khoá đó sẽ được chèn vào đâu (vị trí cha và phía trái hay phải).

**`AbstractTree<T>`**: bốn phép duyệt, `size`, `height`, `printTree` (vẽ cây từ trên xuống bằng
`/` và `\`), `toString` (dạng `8(3(1,6(4,7)),10(-,14(13,-)))`) và `search` tuần tự. Các phép
duyệt đều dùng stack hoặc queue thay cho đệ quy, nên cây suy biến (chuỗi) vẫn không tràn stack.

**`BinaryTree<T>`**: cây nhị phân thường, không có thứ tự. `buildTree` điền từ trên xuống, trái
sang phải, tốn O(n). `search` phải duyệt hết cây, O(n). `remove` chép phần tử của nút sâu nhất
(nút cuối cùng theo level order) vào nút cần xoá, rồi bỏ nút sâu nhất đi.

**`BinarySearchTree<T>`**: `search` và `insert` chỉ đi một đường từ gốc xuống, O(h), viết bằng
vòng lặp. `remove` là xoá bằng copying (dùng predecessor). `deleteByMerging` giữ lại để so sánh
với slide. `balance` lấy dãy inorder đã sắp xếp, rồi chèn phần tử ở giữa trước.

**`AVLTree<T>`**: `insert` và `remove` đệ quy xuống rồi cân bằng lại từng nút trên đường về gốc,
bằng quay đơn hoặc quay kép. Cả `deleteByCopying` lẫn `deleteByMerging` đều đi qua `remove` có
cân bằng lại, vì merging để nguyên sẽ phá điều kiện AVL.

**`getSearchCount()`**: số nút đã được so sánh trong lần tìm kiếm gần nhất. `insert`, `remove` và
`balanceFactor` của BST và AVL cũng tìm vị trí trước, nên cũng cập nhật con số này.
`SearchCostDemo` in bảng so sánh ba loại cây trên cùng 1000 khoá:

| 1000 khoá | Chiều cao | Tìm 1 | Tìm 500 | Tìm 1000 | Tìm 1001 (không có) |
|---|---|---|---|---|---|
| `BinaryTree`, khoá tăng dần | 9 | 1 | 980 | 981 | 1000 |
| `BinarySearchTree`, khoá tăng dần | 999 | 1 | 500 | 1000 | 1000 |
| `BinarySearchTree`, khoá ngẫu nhiên | 19 | 5 | 16 | 9 | 9 |
| `AVLTree`, khoá tăng dần | 9 | 10 | 8 | 10 | 10 |

## Độ phức tạp

| Thao tác | `BinaryTree` | `BinarySearchTree` | `AVLTree` |
|---|---|---|---|
| `search` | O(n) | O(h), xấu nhất O(n) | O(log n) |
| `insert` | O(n) | O(h) | O(log n) |
| `remove` | O(n) | O(h) | O(log n) |
| `buildTree` n phần tử | O(n) | O(n·h) | O(n log n) |
| `height` | O(1) | O(1) | O(1) |
| duyệt, `size` | O(n) | O(n) | O(n) |

## Quy ước (giống slide)

- Cây một nút cao 0, cây rỗng cao -1.
- Hệ số cân bằng: `bf = height(right) - height(left)`. Cây AVL giữ `bf` trong khoảng -1 đến 1.
  Khi một nút mất cân bằng, nếu nút con phía cao hơn lệch ngược chiều (`bf` khác dấu) thì quay kép,
  còn lại quay đơn.
- Xoá bằng copying chép khoá của predecessor, tức nút phải nhất của cây con trái.
- Khoá trùng không được chèn: `insert` trả về `false`.

## Sample

| Class | Nội dung |
|---|---|
| `StudentTreeDemo` | Cùng 7 sinh viên dựng bằng `BinaryTree`, `BinarySearchTree`, `AVLTree`: vẽ cây, bốn phép duyệt, tìm id 42 kèm số lần so sánh, xoá id 43 |
| `SearchCostDemo` | Bảng số lần so sánh ở trên |
| `BinarySearchTreeDemo` | Các trace BST trên slide: 8 3 10 1 6 14 4 7 13, xoá bằng merging và copying, balance |
| `AVLTreeDemo` | Chèn 54 (quay kép), xoá 32 (quay đơn), chèn 0..9 theo thứ tự |
| `ExpressionTreeDemo` | Cây biểu thức trong giáo trình: vẽ cây, ký pháp tiền tố và hậu tố, giá trị -13 |
| `FileSystemDemo` | Cây thư mục `cs16/`: preorder in danh sách, postorder cộng dung lượng (5K, 55K, 61K) |

`Student` so sánh và `equals` theo `id`, nên muốn tìm theo id thì dùng
`tree.search(Student.withId(42))`.

Chạy từ thư mục gốc của repo:

```bash
java tree/sample/StudentTreeDemo.java
java tree/sample/SearchCostDemo.java
```

Lệnh trên cần JDK 22 trở lên. Với JDK cũ hơn, biên dịch trước:

```bash
mkdir -p out
javac -d out $(find tree -name "*.java")
java -cp out tree.sample.StudentTreeDemo
```

Cách chạy test và mở bằng IDE: xem [README ở thư mục gốc](../README.md).
