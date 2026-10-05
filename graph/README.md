# Đồ thị (Graphs)

| Package | Class | Syllabus | Giáo trình |
|---|---|---|---|
| `graph` | `AbstractGraph`, `Edge` | 5.1 Graphs, 5.3 Graph Traversals | 14.1, 14.3 |
| `graph` | `AdjacencyListGraph` | 5.2.2 Adjacency List Structure | 14.2.2 |
| `graph` | `AdjacencyMatrixGraph` | 5.2.3 Adjacency Matrix Structure | 14.2.4 |
| `graph` | `GraphVisualizer` | vẽ đồ thị, phát lại DFS và BFS | |
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
`null` nghĩa là không có cạnh. Ma trận kiểu `Integer[][]` thay cho `int[][]` để cạnh trọng số 0
vẫn lưu được. Ma trận đối xứng qua đường chéo. Một `HashMap` giữ chỉ số của từng
đỉnh để `addEdge` không phải tìm tuần tự. `getNeighbors` quét cả một hàng, nên đỉnh kề ra theo
thứ tự đỉnh. `addVertex` cấp ma trận mới lớn hơn một hàng, một cột rồi chép dữ liệu cũ sang.

## GraphVisualizer

Cửa sổ Swing vẽ đồ thị trên một vòng tròn, mỗi thành phần liên thông một màu. Khung bên phải ghi
số đỉnh, số cạnh, mật độ, tổng bậc bằng hai lần số cạnh, đỉnh cô lập, đỉnh bậc lẻ và đồ thị có
đường đi hay chu trình Euler không. Bảng Vertices ghi bậc, đỉnh kề và thành phần của từng đỉnh.
Bấm vào một đỉnh để tô các cạnh của nó, kéo đỉnh để đổi vị trí.

```java
GraphVisualizer.show(graph, "My graph");
GraphVisualizer.show(graph, "DFS from a", graph.depthFirstTraversal('a'), GraphVisualizer.Traversal.DFS);
```

Khi nhận thêm một thứ tự duyệt, cửa sổ phát lại từng bước: cạnh cây mọc dần thành mũi tên, khung
Traversal ghi stack (DFS) hoặc queue (BFS) ở bước đang xem. Các nút Restart, Prev, Play, Next và
thanh Delay điều khiển việc phát lại.

Truyền `null` thay cho `Traversal.DFS` hay `Traversal.BFS` thì visualizer tự đoán. Với mỗi đỉnh
trong thứ tự, cha của nó là đỉnh gần đỉnh stack nhất (DFS) hoặc gần đầu queue nhất (BFS) có cạnh
nối tới nó, và một đỉnh chỉ rời stack hay queue khi mọi đỉnh kề của nó đã được thăm. Thứ tự hợp
với cả hai cách thì visualizer chọn DFS, ví dụ `a, c, f, d, b, e` trên đồ thị mẫu. Thứ tự không hợp
với cách nào thì cửa sổ không vẽ cạnh cây.

## Quy ước

- `addEdge(a, b, w)` và `addEdge(b, a, w)` là cùng một cạnh. Thêm lại cạnh đã có thì trọng số mới
  thay trọng số cũ.
- `getEdges` liệt kê mỗi cạnh đúng một lần, đầu `src` đứng trước `dest` theo thứ tự đỉnh.
- Khuyên `a-a` tính là một cạnh, và `a` là đỉnh kề của chính nó.
- Đỉnh trùng bị bỏ qua, cả trong constructor lẫn `addVertex`.
- `addEdge`, `getNeighbors` hoặc phép duyệt gặp đỉnh chưa có trong đồ thị thì ném
  `IllegalArgumentException`.

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

Demo thêm cạnh theo thứ tự đỉnh nên hai cách biểu diễn cho cùng kết quả. Nếu máy có màn hình, demo
mở thêm hai cửa sổ `GraphVisualizer` phát lại DFS và BFS từ `a`. Trên GitHub Actions không có màn
hình, demo chỉ in ra console. Test
`listKeepsNeighborsInInsertionOrderMatrixInVertexOrder` thêm cạnh `a-c` trước `a-b`, và lúc đó
DFS của hai lớp ra hai thứ tự khác nhau.

Chạy từ thư mục gốc của repo (JDK 22 trở lên):

```bash
java graph/sample/SampleGraphDemo.java
```

Muốn thử đồ thị khác thì truyền file cạnh, và nếu cần thêm file đỉnh. File cạnh ghi mỗi dòng một
cạnh dạng `a-c`, hoặc `a-c 4` khi có trọng số. File đỉnh ghi mỗi dòng một đỉnh, chỉ cần cho đỉnh
cô lập và để quyết định thứ tự đỉnh, vì demo tự thêm hai đầu của mọi cạnh. Demo duyệt từ đỉnh
đầu tiên.

```bash
java graph/sample/SampleGraphDemo.java edges.txt vertices.txt
```

Với JDK cũ hơn, biên dịch trước:

```bash
mkdir -p out
javac -d out $(find graph -name "*.java")
java -cp out graph.sample.SampleGraphDemo
```

Cách chạy test và mở bằng IDE: xem [README ở thư mục gốc](../README.md).
