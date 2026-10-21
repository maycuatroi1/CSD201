# Bảng băm (Hash Tables)

| Package | Class | Syllabus | Giáo trình |
|---|---|---|---|
| `hash` | `OpenAddressingHashTable` | 7.3 Collision-Handling: open addressing, xoá bằng ô đánh dấu | 10.2.2 |
| `hash` | `LinearProbingHashTable` | 7.3 linear probing, p(i) = i | 10.2.2 |
| `hash` | `QuadraticProbingHashTable` | 7.3 quadratic probing, p(i) = i² | 10.2.2 |
| `hash` | `DoubleHashingHashTable` | 7.3 double hashing, h'(k) = q - (k mod q) | 10.2.2 |
| `hash` | `ChainingHashTable` | 7.3 separate chaining | 10.2.2 |
| `hash` | rehash trong cả hai loại bảng | 7.4 Load Factors, Rehashing, and Efficiency | 10.2.3 |
| `hash.sample` | `HashTableDemo`, `JavaHashMapDemo`, `StudentId` | 7.5 Java Hash Table Implementation | 10.2.4 |

## Cây kế thừa

```
OpenAddressingHashTable<K, V>     get, put, remove, rehash; ô đã xoá giữ dấu DEL
├── LinearProbingHashTable        probe(home, i) = (home + i) mod M
├── QuadraticProbingHashTable     probe(home, i) = (home + i²) mod M
└── DoubleHashingHashTable        probe(home, i) = (home + i * step(key)) mod M
ChainingHashTable<K, V>           mỗi ô là một LinkedList<Entry<K, V>>
```

- Hàm băm là `Math.floorMod(key.hashCode(), M)`, nên với khoá `Integer` thì `h(x) = x mod M` đúng như
  slide, kể cả khi `hashCode()` âm.
- Bảng open addressing rehash khi `(size + số ô DEL) > maxLoadFactor * M`, mặc định 0.5. Bảng chaining
  rehash khi `size > maxLoadFactor * M`, mặc định 0.75. Kích thước mới là số nguyên tố nhỏ nhất không
  nhỏ hơn `2M`.
- `lastProbes()` trả về các ô mà lần `get`, `put` hay `remove` gần nhất đã xét, theo đúng thứ tự, để
  so với bảng vết làm tay.

## Ví dụ trong Demo

`HashTableDemo` chạy đúng các ví dụ của slide 7-Hashing:

| Phần | Dữ liệu | Kết quả |
|---|---|---|
| Linear, quadratic, double hashing | 89, 18, 49, 58, 69 vào M = 10, `h'(x) = 7 - x mod 7` | `[49, 58, 69, ...]`, `[49, -, 58, 69, ...]`, `[69, -, -, 58, -, -, 49, ...]` |
| Tìm bằng linear probing | `get(69)`, `get(79)` | xét 9, 0, 1, 2 rồi thấy; xét 9, 0, 1, 2, 3 rồi dừng ở ô trống |
| Xoá | A1, A4, A2, B4, B1 là khoá 1, 4, 2, 14, 11; xoá A4 | B4 vẫn tìm được qua ô DEL |
| Rehash | 15, 22, 8, 1 vào M = 7 | λ vượt 0.5, bảng mới M = 17 |
| Separate chaining | A5, A2, A3, B5, A9, B2, B9, C2 là 5, 2, 3, 15, 9, 12, 19, 22 | ô 2 chứa 2, 12, 22 |

`JavaHashMapDemo` dùng `HashSet` tìm từ lặp, `HashMap` đếm số lần xuất hiện của từ, và `StudentId`
(có `equals` và `hashCode`) làm khoá của `HashMap`.

```bash
java -cp out hash.sample.HashTableDemo
java -cp out hash.sample.JavaHashMapDemo
```
