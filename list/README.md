# Danh sách liên kết (Linked Lists)

| Package | Class | Syllabus | Giáo trình |
|---|---|---|---|
| `list` | `Node` | 1.4 Doubly Linked Lists | 3.4 |
| `list` | `List` | 1.2 Singly Linked Lists, 1.4 Doubly Linked Lists | 3.2, 3.4 |
| `list` | `LinkedList` | 1.4 Doubly Linked Lists | 3.4 |
| `list.sample` | `LinkedListDemo` | chạy thử | |

`LinkStack` trong package [`stack`](../stack/README.md) dùng `LinkedList` làm chỗ chứa.

## Cây kế thừa

```
List<T>                 head, tail, size; getSize, isEmpty, getHead, getTail, iterator, toString
└── LinkedList<T>       append, addFirst, addLast, insertAt, deleteAt, getByIndex
```

## Các class chính

**`Node<T>`**: một phần tử và hai tham chiếu `next`, `prev`. Ngoài package `list`, code chỉ đọc
node qua `getData`, `getNext`, `getPrev`. Setter của `next` và `prev` chỉ dùng được trong package,
nên muốn nối hay gỡ node thì phải gọi method của `LinkedList`.

**`List<T>`**: lớp trừu tượng giữ `head`, `tail`, `size` và cách duyệt từ `head` tới `tail`. Lớp con
chỉ phải cài `append`. Vòng `for (T x : list)` dùng iterator của lớp này; gọi `next()` khi đã hết
phần tử thì ném `NoSuchElementException`. `toString` có dạng `[Sunrise <-> Neon Drive <-> Echo Park]`.

**`LinkedList<T>`**: danh sách liên kết đôi, `head.prev` và `tail.next` luôn là `null`.

| Method | Việc làm | Chi phí |
|---|---|---|
| `append(x)`, `addLast(x)` | nối `x` sau `tail` | O(1) |
| `addFirst(x)` | chèn `x` trước `head` | O(1) |
| `getByIndex(i)` | đi từ `head` qua `i` node, trả về node | O(i) |
| `insertAt(i, x)` | chèn `x` trước node thứ `i`; `i == size` thì nối cuối | O(i) |
| `deleteAt(i)` | gỡ node thứ `i`, sửa `next` của node trước và `prev` của node sau, trả về phần tử đã gỡ | O(i) |

Chỉ số nằm ngoài `0..size-1` (với `insertAt` là `0..size`) làm method ném
`IndexOutOfBoundsException`. Xoá node cuối cùng thì cả `head` lẫn `tail` về `null`; bản viết trên
lớp quên trường hợp này nên `deleteAt(0)` trên danh sách một phần tử ném `NullPointerException`.

Ví dụ trong `LinkedListDemo` và `LinkedListTest` lấy từ playlist của app "Sóng" trong slide bài 1:
`insertAt(2, "Aurora")` trên `Intro, Sunrise, Neon Drive, Echo Park`, và xoá `Echo Park` ở cuối
`Sunrise, Neon Drive, Echo Park` để `tail` lùi về `Neon Drive`.

## Chạy

Với JDK 22 trở lên, đứng ở thư mục gốc của repo:

```bash
java list/sample/LinkedListDemo.java
```

Với JDK 8 đến 21:

```bash
mkdir -p out
javac -d out $(find list -name "*.java")
java -cp out list.sample.LinkedListDemo
```
