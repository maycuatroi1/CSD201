# Ngăn xếp (Stacks)

| Package | Class | Syllabus | Giáo trình |
|---|---|---|---|
| `stack` | `Stack` | 2.1 Stacks | 6.1.1 |
| `stack` | `ArrayStack` | 2.1 Stacks | 6.1.2 |
| `stack` | `LinkStack` | 2.1 Stacks | 6.1.3 |
| `stack.sample` | `Student` và `StackDemo` | chạy thử | |

`HogwartsVisualizer` nằm trong package [`queue`](../queue/README.md) và vẽ được cả stack.

## Cây kế thừa

```
Stack<T>               push, pop, peek, getSize, isEmpty, getStorage, toList, toString
├── ArrayStack<T>      mảng cố định, đỉnh ở elements[size - 1]
└── LinkStack<T>       list.LinkedList, đỉnh ở head
```

## Hợp đồng của Stack

Theo slide bài Stack: `pop` và `peek` trên stack rỗng ném lỗi.

| Method | Việc làm | Khi stack rỗng |
|---|---|---|
| `push(x)` | đặt `x` lên đỉnh | |
| `pop()` | lấy phần tử ở đỉnh ra và trả về | ném `EmptyStackException` |
| `peek()` | trả về phần tử ở đỉnh, giữ nguyên stack | ném `EmptyStackException` |
| `toList()` | `java.util.List` các phần tử từ đỉnh xuống đáy | `[]` |
| `getStorage()` | chỗ chứa bên trong: mảng gồm cả ô trống, hoặc `LinkedList` | |

`toString` in `toList()`. Câu dự đoán ở slide 6, `push(10); push(20); a = pop(); push(30); b = peek();`,
cho `a = 20`, `b = 30` và stack `[30, 10]`; `StackDemo` chạy câu này trên cả hai lớp.
`getStorage()` dùng để nhìn vào bên trong: `HogwartsVisualizer` vẽ nó thành dải ô dưới cảnh sân ga.

## Hai cách cài đặt

**`ArrayStack<T>`**: `new ArrayStack<>(new Student[5])` tạo stack chứa tối đa 5 phần tử. Stack chép
mảng truyền vào, nên bên gọi sửa mảng đó cũng không ảnh hưởng tới stack. `push` khi đã đầy ném
`IllegalStateException` và stack giữ nguyên. `pop` gán `null` vào ô vừa rời để bộ dọn rác thu hồi
được phần tử.

Java không cho viết `new T[n]`. Bản viết trên lớp dùng `(T[]) new Object[capacity]` với
`T extends Comparable<T>` và ném `ClassCastException` ngay trong constructor: sau khi xoá kiểu,
`elements` có kiểu `Comparable[]`, còn mảng tạo ra là `Object[]`. Bỏ ràng buộc `Comparable` thì
chạy được nhưng javac vẫn cảnh báo unchecked, mà repo biên dịch với `-Xlint:all -Werror`. Vì vậy
bên gọi đưa mảng vào, giống `Collection.toArray(T[])` của JDK.

**`LinkStack<T>`**: chứa phần tử trong một `list.LinkedList`. Đỉnh nằm ở `head`, nên `push`
(`addFirst`) và `pop` (`deleteAt(0)`) đều O(1).

## Chạy

Với JDK 22 trở lên, đứng ở thư mục gốc của repo:

```bash
java stack/sample/StackDemo.java
```

Với JDK 8 đến 21, biên dịch cả package `list` vì `LinkStack` dùng `LinkedList`:

```bash
mkdir -p out
javac -d out $(find stack list -name "*.java")
java -cp out stack.sample.StackDemo
```
