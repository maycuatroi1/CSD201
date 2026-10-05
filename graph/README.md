# Đồ thị (Graphs)

| Package | Class | Syllabus | Giáo trình |
|---|---|---|---|
| `graph` | `AbstractGraph`, `Edge` | 5.1 Graphs, 5.3 Graph Traversals | 14.1, 14.3 |
| `graph` | `AdjacencyListGraph` | 5.2.2 Adjacency List Structure | 14.2.2 |
| `graph` | `AdjacencyMatrixGraph` | 5.2.3 Adjacency Matrix Structure | 14.2.4 |
| `graph.sample` | `SampleGraphDemo` | chạy thử | |

## Cây kế thừa

```
AbstractGraph<T>                 addVertex, addEdge, hasVertex, getVertices, getEdges, getNeighbors
│                                + depthFirstTraversal, breadthFirstTraversal
├── AdjacencyListGraph<T>        mỗi đỉnh giữ danh sách cạnh của nó
└── AdjacencyMatrixGraph<T>      ma trận trọng số V x V
```

## Các class chính

**`Edge<T>`**: hai đầu `src`, `dest` và trọng số `weight`. Tạo xong thì không sửa được.
`toString` có dạng `a-c(1)`.

**`AbstractGraph<T>`**: đồ thị vô hướng có trọng số. Lớp này cài đặt DFS (đệ quy) và BFS (dùng
queue) một lần, chỉ dựa vào `getNeighbors`. Hai lớp con khác nhau ở cách lưu cạnh và cách liệt kê
đỉnh kề, nên thứ tự đỉnh kề của mỗi lớp quyết định thứ tự duyệt DFS và BFS.

**`AdjacencyListGraph<T>`**: một `LinkedHashMap` từ mỗi đỉnh tới danh sách `Edge` đi ra từ đỉnh
đó. Cạnh vô hướng `a-c` nằm trong danh sách của cả `a` lẫn `c`. `getNeighbors` trả đỉnh kề theo
thứ tự thêm cạnh.

**`AdjacencyMatrixGraph<T>`**: ô `[i][j]` là trọng số cạnh nối đỉnh thứ `i` với đỉnh thứ `j`, ô
bằng 0 nghĩa là không có cạnh. Ma trận đối xứng qua đường chéo. Một `HashMap` giữ chỉ số của từng
đỉnh để `addEdge` không phải tìm tuần tự. `getNeighbors` quét cả một hàng, nên đỉnh kề ra theo
thứ tự đỉnh. `addVertex` cấp ma trận mới lớn hơn một hàng, một cột rồi chép dữ liệu cũ sang.

## Quy ước

- `addEdge(a, b, w)` và `addEdge(b, a, w)` là cùng một cạnh. Thêm lại cạnh đã có thì trọng số mới
  thay trọng số cũ.
- `getEdges` liệt kê mỗi cạnh đúng một lần, đầu `src` đứng trước `dest` theo thứ tự đỉnh.
- Khuyên `a-a` tính là một cạnh, và `a` là đỉnh kề của chính nó.
- Đỉnh trùng bị bỏ qua, cả trong constructor lẫn `addVertex`.
- `addEdge`, `getNeighbors` hoặc phép duyệt gặp đỉnh chưa có trong đồ thị thì ném
  `IllegalArgumentException`.
- `AdjacencyMatrixGraph` dùng 0 để đánh dấu không có cạnh, nên không lưu được cạnh trọng số 0.

## Độ phức tạp

V là số đỉnh, E là số cạnh, deg(v) là bậc của đỉnh v.

| Thao tác | `AdjacencyListGraph` | `AdjacencyMatrixGraph` |
|---|---|---|
| bộ nhớ | O(V + E) | O(V²) |
| `hasVertex` | O(1) | O(1) |
| `addVertex` | O(1) | O(V²) |
| `addEdge(u, v)` | O(deg(u) + deg(v)), vì phải dò cạnh trùng | O(1) |
| `getNeighbors(v)` | O(deg(v)) | O(V) |
| `getEdges` | O(V + E) | O(V²) |
| DFS, BFS | O(V + E) | O(V²) |

## Sample

`SampleGraphDemo` dựng cùng một đồ thị 7 đỉnh `a` đến `g`, 8 cạnh, bằng cả hai cách biểu diễn.
Đỉnh `g` không nối với đỉnh nào. Demo in danh sách cạnh, đỉnh kề của từng đỉnh, rồi duyệt từ `a`:

```
DFS from a: [a, c, f, d, b, e]
BFS from a: [a, c, d, f, b, e]
```

Demo thêm cạnh theo thứ tự đỉnh nên hai cách biểu diễn cho cùng kết quả. Test
`listKeepsNeighborsInInsertionOrderMatrixInVertexOrder` thêm cạnh `a-c` trước `a-b`, và lúc đó
DFS của hai lớp ra hai thứ tự khác nhau.

Chạy từ thư mục gốc của repo (JDK 22 trở lên):

```bash
java graph/sample/SampleGraphDemo.java
```

Với JDK cũ hơn, biên dịch trước:

```bash
mkdir -p out
javac -d out $(find graph -name "*.java")
java -cp out graph.sample.SampleGraphDemo
```

Cách chạy test và mở bằng IDE: xem [README ở thư mục gốc](../README.md).
