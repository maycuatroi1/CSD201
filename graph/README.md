# Đồ thị (Graphs)

| Package | Class | Syllabus | Giáo trình |
|---|---|---|---|
| `graph` | `AbstractGraph`, `Edge` | 5.1 Graphs, 5.3 Graph Traversals | 14.1, 14.3 |
| `graph` | `AdjacencyListGraph` | 5.2.2 Adjacency List Structure | 14.2.2 |
| `graph` | `AdjacencyMatrixGraph` | 5.2.3 Adjacency Matrix Structure | 14.2.4 |
| `graph` | `DijkstraShortestPath` | 5.4 Shortest Paths, 5.4.2 Dijkstra's Algorithm | 14.6.2 |
| `graph` | `PrimMST` | 5.5.1 Prim-Jarník Algorithm | 14.7.1 |
| `graph` | `KruskalMST` | 5.5.2 Kruskal's Algorithm | 14.7.2, 14.7.3 |
| `graph` | `GraphAlgorithmObserver` | móc nối để theo dõi từng bước của ba thuật toán | |
| `graph` | `GraphVisualizer` | vẽ đồ thị, phát lại DFS và BFS | |
| `graph` | `WeightedGraphVisualizer` | phát lại Dijkstra, Prim, Kruskal từng bước | |
| `graph.sample` | `SampleGraphDemo` | chạy thử danh sách kề, ma trận kề, DFS, BFS | |
| `graph.sample` | `ShortestPathDemo` | chạy thử Dijkstra | |
| `graph.sample` | `MinimumSpanningTreeDemo` | chạy thử Prim và Kruskal | |
| `graph.sample` | `SampleWeightedGraph` | đồ thị có trọng số dùng chung cho hai demo | |

## Cây kế thừa

```
AbstractGraph<T>                 addVertex, addEdge, hasVertex, getVertices, getEdges, getIncidentEdges
│                                + getNeighbors, depthFirstTraversal, breadthFirstTraversal
├── AdjacencyListGraph<T>        mỗi đỉnh giữ danh sách cạnh của nó
└── AdjacencyMatrixGraph<T>      ma trận trọng số V x V

DijkstraShortestPath<T>          đường đi ngắn nhất từ một nguồn, trọng số không âm
PrimMST<T>                       mọc một cây từ đỉnh xuất phát, hàng đợi ưu tiên giữ cạnh rời cây
KruskalMST<T>                    thử cạnh từ nhẹ đến nặng, union-find chặn chu trình
```

Ba thuật toán trên nhận thêm một `GraphAlgorithmObserver` không bắt buộc, báo lại từng quyết định
(`started`, `edgeQueued`, `edgeAdded`, `edgeSkipped`, `vertexSettled`, `staleEntrySkipped`,
`edgeRelaxed`, `edgesSorted`). `WeightedGraphVisualizer` ghi lại các báo cáo đó thành danh sách
bước để phát lại. Mọi method của observer đều có thân rỗng sẵn, nên chỉ cần viết method mình cần.

## Các class chính

**`Edge<T>`**: hai đầu `src`, `dest` và trọng số `weight`. Tạo xong thì không sửa được.
`toString` có dạng `a-c(1)`.

**`AbstractGraph<T>`**: đồ thị vô hướng có trọng số. Lớp này cài đặt DFS (đệ quy), BFS (dùng
queue) và `getNeighbors` một lần, chỉ dựa vào `getIncidentEdges`. Hai lớp con khác nhau ở cách lưu
cạnh và cách liệt kê cạnh kề, nên thứ tự cạnh kề của mỗi lớp quyết định thứ tự duyệt DFS và BFS,
và cả thứ tự Prim chọn cạnh khi có trọng số bằng nhau.

`getIncidentEdges(v)` trả cạnh kề của `v` với `getSrc()` luôn là `v`, nên thuật toán đi theo cạnh
biết mình vừa tới đỉnh nào: `getDest()` là đầu xa. `getNeighbors(v)` chỉ là `getDest()` của danh
sách đó.

**`AdjacencyListGraph<T>`**: một `LinkedHashMap` từ mỗi đỉnh tới danh sách `Edge` đi ra từ đỉnh
đó. Cạnh vô hướng `a-c` nằm trong danh sách của cả `a` lẫn `c`. `getNeighbors` trả đỉnh kề theo
thứ tự thêm cạnh.

**`AdjacencyMatrixGraph<T>`**: ô `[i][j]` là trọng số cạnh nối đỉnh thứ `i` với đỉnh thứ `j`, ô
`null` nghĩa là không có cạnh. Ma trận kiểu `Integer[][]` thay cho `int[][]` để cạnh trọng số 0
vẫn lưu được. Ma trận đối xứng qua đường chéo. Một `HashMap` giữ chỉ số của từng
đỉnh để `addEdge` không phải tìm tuần tự. `getNeighbors` quét cả một hàng, nên đỉnh kề ra theo
thứ tự đỉnh. `addVertex` cấp ma trận mới lớn hơn một hàng, một cột rồi chép dữ liệu cũ sang.

## Đường đi ngắn nhất

**`DijkstraShortestPath<T>`**: khoảng cách ngắn nhất từ một đỉnh nguồn tới mọi đỉnh, với đồ thị
trọng số không âm. Hàng đợi ưu tiên giữ cặp (đỉnh, khoảng cách). `PriorityQueue` của JDK không hạ
được khoá của phần tử đang nằm trong hàng đợi, nên tìm được đường ngắn hơn thì thêm hẳn một cặp
mới; cặp cũ lấy ra sau sẽ bị bỏ vì đỉnh của nó đã chốt. Đỉnh rời hàng đợi là đỉnh có khoảng cách
nhỏ nhất trong số chưa chốt, và vì không có cạnh âm nên không đường nào tới nó ngắn hơn được nữa.

```java
DijkstraShortestPath<String> paths = new DijkstraShortestPath<>(graph, "A");
paths.distanceTo("E");   // 20
paths.pathTo("E");       // [A, C, F, E]
paths.hasPathTo("S");    // false khi S ở thành phần khác
```

Không có đường thì `distanceTo` trả `Integer.MAX_VALUE` và `pathTo` trả danh sách rỗng. Gặp cạnh
âm thì constructor ném `IllegalArgumentException` ngay, vì thuật toán sẽ cho kết quả sai: đường đi
qua cạnh âm có thể ngắn hơn sau khi đỉnh đã chốt. Đồ thị có cạnh âm cần Bellman-Ford, không có
trong môn này.

## Cây khung nhỏ nhất

Cây khung nhỏ nhất (minimum spanning tree) của đồ thị liên thông có trọng số là tập cạnh nối mọi
đỉnh, không có chu trình, và tổng trọng số nhỏ nhất. Hai thuật toán dưới đây nhận một
`AbstractGraph` và trả về danh sách cạnh của cây cùng tổng trọng số. Chúng không sửa đồ thị.

**`PrimMST<T>`**: mọc một cây duy nhất từ đỉnh xuất phát. Hàng đợi ưu tiên giữ các cạnh rời cây;
cạnh nhẹ nhất mà đầu xa còn ở ngoài sẽ nhập cây, rồi các cạnh của đỉnh mới vào hàng đợi. Cạnh lấy
ra mà đầu xa đã kịp vào cây thì bỏ đi, nên không phải giảm khoá của phần tử đang nằm trong hàng
đợi, chỉ cần `PriorityQueue` của JDK. `getEdges` trả cạnh theo thứ tự nhập cây, `getVertices` trả
đỉnh theo thứ tự nhập cây, và chỉ gồm thành phần liên thông của đỉnh xuất phát.

```java
PrimMST<String> prim = new PrimMST<>(graph, "A");
prim.getEdges();     // [A-B(7), A-C(9), C-F(2), F-E(9), E-D(6)]
prim.getWeight();    // 33
prim.getVertices();  // [A, B, C, F, E, D]
```

**`KruskalMST<T>`**: thử các cạnh từ nhẹ đến nặng, giữ cạnh khi hai đầu thuộc hai cây khác nhau, và
việc giữ cạnh gộp hai cây đó thành một. Union-find trả lời câu hỏi "hai đầu cùng một cây chưa":
`find` leo từ một đỉnh lên gốc cây của nó, `union` treo gốc này dưới gốc kia. Hai mẹo làm cho mỗi
lần hỏi gần như O(1): `find` nén đường đi (mọi đỉnh trên đường trỏ thẳng vào gốc), và `union` treo
cây thấp dưới cây cao, so bằng `rank` là chặn trên của chiều cao. Đồ thị không liên thông thì kết
quả là rừng khung nhỏ nhất, mỗi thành phần một cây.

```java
KruskalMST<String> kruskal = new KruskalMST<>(graph);
kruskal.getEdges();   // [C-F(2), D-E(6), A-B(7), A-C(9), E-F(9)]
kruskal.getWeight();  // 33
```

Hai ví dụ trên chạy trên đồ thị của `MinimumSpanningTreeDemo`: cùng một cây, tìm ra theo hai thứ
tự khác nhau. Prim luôn có đúng một cây đang mọc nên cạnh ra theo thứ tự nhập cây, còn Kruskal
gom dần nhiều cây rời rạc nên cạnh ra theo trọng số tăng dần.

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

## WeightedGraphVisualizer

Cửa sổ Swing phát lại Dijkstra, Prim hoặc Kruskal trên đồ thị có trọng số, mỗi bước một quyết
định. Thanh công cụ đổi thuật toán và đỉnh xuất phát, các nút Restart, Prev, Play, Next và phím
mũi tên trái phải đi tới lui, thanh Delay chỉnh tốc độ. Khung Step viết thuật toán đang nghĩ gì,
khung dưới là hàng đợi ưu tiên (Dijkstra, Prim) hay danh sách cạnh đã sắp xếp (Kruskal), khung
State là bảng khoảng cách và đỉnh trước (Dijkstra) hay trọng số cây và các cây rời rạc (Prim,
Kruskal). Với Dijkstra, bấm vào một đỉnh để tô đường đi ngắn nhất tới nó tìm được tới bước đang
xem. Kéo đỉnh để đổi vị trí, nút Reset layout xếp lại thành vòng tròn.

```java
WeightedGraphVisualizer.show(graph, "Dijkstra from A", WeightedGraphVisualizer.Algorithm.DIJKSTRA, "A", "E");
WeightedGraphVisualizer.show(graph, "Prim from A", WeightedGraphVisualizer.Algorithm.PRIM, "A");
WeightedGraphVisualizer.show(graph, "Kruskal", WeightedGraphVisualizer.Algorithm.KRUSKAL, null);
```

Phần mô hình tách khỏi phần vẽ: `record(graph, algorithm, start)` chạy thuật toán với một observer
và trả về danh sách bước, kiểm tra được mà không mở cửa sổ. `WeightedGraphVisualizerTest` so đúng
tên của từng bước, ví dụ Prim trên đồ thị mẫu ra `Start at A`, năm bước `Add ...` rồi bốn bước
`Skip ...` khi dọn nốt hàng đợi. Đồ thị không chạy được (không có đỉnh, hoặc cạnh âm với Dijkstra)
cho đúng một bước nói rõ lý do, thay vì ném lỗi ra cửa sổ.

## Quy ước

- `addEdge(a, b, w)` và `addEdge(b, a, w)` là cùng một cạnh. Thêm lại cạnh đã có thì trọng số mới
  thay trọng số cũ.
- `getEdges` liệt kê mỗi cạnh đúng một lần, đầu `src` đứng trước `dest` theo thứ tự đỉnh.
- Khuyên `a-a` tính là một cạnh, và `a` là đỉnh kề của chính nó.
- Đỉnh trùng bị bỏ qua, cả trong constructor lẫn `addVertex`.
- `addEdge`, `getIncidentEdges`, `getNeighbors`, phép duyệt, `PrimMST` hoặc `DijkstraShortestPath`
  gặp đỉnh chưa có trong đồ thị thì ném `IllegalArgumentException`. `DijkstraShortestPath` ném cả
  khi đồ thị có cạnh trọng số âm.
- Khuyên không bao giờ nhập cây khung: Prim không xếp nó vào hàng đợi, Kruskal thấy hai đầu cùng
  một cây nên bỏ qua.
- Trọng số bằng nhau cho nhiều cây khung nhỏ nhất, hai thuật toán chọn tất định: Prim ưu tiên cạnh
  vào hàng đợi trước, Kruskal giữ thứ tự của `getEdges` vì sắp xếp ổn định. Đổi cách biểu diễn hay
  đổi thứ tự thêm cạnh thì cây đổi theo, tổng trọng số thì không.

## Độ phức tạp

V là số đỉnh, E là số cạnh, deg(v) là bậc của đỉnh v.

| Thao tác | `AdjacencyListGraph` | `AdjacencyMatrixGraph` |
|---|---|---|
| bộ nhớ | O(V + E) | O(V²) |
| `hasVertex` | O(1) | O(1) |
| `addVertex` | O(1) | O(V²) |
| `addEdge(u, v)` | O(deg(u) + deg(v)), vì phải dò cạnh trùng | O(1) |
| `getIncidentEdges(v)`, `getNeighbors(v)` | O(deg(v)) | O(V) |
| `getEdges` | O(V + E) | O(V²) |
| DFS, BFS | O(V + E) | O(V²) |
| `DijkstraShortestPath` | O(E log E) | O(V² + E log E) |
| `PrimMST` | O(E log E) | O(V² + E log E) |
| `KruskalMST` | O(E log E) | O(V² + E log E) |

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

`ShortestPathDemo` và `MinimumSpanningTreeDemo` dùng chung đồ thị 6 đỉnh `A` đến `F`, 9 cạnh có
trọng số của đề Sample PE câu 3 f2, dựng trong `SampleWeightedGraph`.

`ShortestPathDemo` chạy Dijkstra từ `A`:

```
  to D    distance   20  path [A, C, D]
  to E    distance   20  path [A, C, F, E]
  to F    distance   11  path [A, C, F]
```

Đề hỏi đường từ `A` tới `E`: `A-C-F-E` tốn 9 + 2 + 9 = 20, trong khi đường ít cạnh hơn `A-F-E` tốn
14 + 9 = 23. Demo chạy lại trên đồ thị hai thành phần để thấy `no path`, rồi mở
`WeightedGraphVisualizer` phát lại Dijkstra từ `A` với đích `E`.

`MinimumSpanningTreeDemo` chạy Prim từ `A`, Prim từ `D` và Kruskal:

```
Prim from A: weight 33, 5 edges [A-B(7), A-C(9), C-F(2), F-E(9), E-D(6)]
Prim from D: weight 33, 5 edges [D-E(6), E-F(9), F-C(2), C-A(9), A-B(7)]
Kruskal: weight 33, 5 edges [C-F(2), D-E(6), A-B(7), A-C(9), E-F(9)]
```

Ba cây chứa đúng một tập cạnh, chỉ khác thứ tự tìm ra và khác đầu nào là `src`. Demo chạy lại
bằng ma trận kề để thấy cùng kết quả, rồi dựng một đồ thị hai thành phần `P-Q-R` và `S-T`: Prim từ
`P` chỉ với tới `[P, Q, R]`, còn Kruskal trả 3 cạnh, là rừng khung của cả hai thành phần. Nếu máy
có màn hình, demo mở thêm hai cửa sổ `WeightedGraphVisualizer` phát lại Prim và Kruskal.

Chạy từ thư mục gốc của repo (JDK 22 trở lên):

```bash
java graph/sample/ShortestPathDemo.java
java graph/sample/MinimumSpanningTreeDemo.java
```

Với JDK cũ hơn, biên dịch trước:

```bash
mkdir -p out
javac -d out $(find graph -name "*.java")
java -cp out graph.sample.SampleGraphDemo
java -cp out graph.sample.ShortestPathDemo
java -cp out graph.sample.MinimumSpanningTreeDemo
```

Cách chạy test và mở bằng IDE: xem [README ở thư mục gốc](../README.md).
