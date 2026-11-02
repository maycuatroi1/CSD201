# Xử lý văn bản (Text Processing)

| Package | Class | Syllabus | Giáo trình |
|---|---|---|---|
| `text` | `StringFinder` | 8.2 Pattern-Matching Algorithms | 13.2 |
| `text` | `BruteForceStringFinder` | 8.2.1 Brute Force | 13.2.1 |
| `text` | `KMPStringFinder` | 8.2.2 The Knuth-Morris-Pratt Algorithm | 13.2.3 |
| `text.sample` | `StringFinderDemo` | chạy thử, so số phép so của hai thuật toán | |

## Cây kế thừa

```
StringFinder                  findString, sameChar, getCompareCount
├── BruteForceStringFinder    sai thì start tăng 1, so lại từ đầu pattern
└── KMPStringFinder           sai thì dịch theo bảng LPS, giữ phần đã khớp
```

- `findString(text, search)` trả về mọi vị trí bắt đầu khớp, kể cả các lần khớp chồng lên nhau, và
  list rỗng nếu không có. Code trên slide dừng ở lần khớp đầu và trả về -1 nếu không thấy.
- `search` rỗng khớp ở mọi vị trí từ 0 đến `text.length()`.
- Hai lớp con chỉ so ký tự qua `sameChar`, nên `getCompareCount()` là số phép so thật, kể cả lúc KMP
  dựng bảng LPS. Mỗi đối tượng finder giữ bộ đếm riêng và cộng dồn qua các lần gọi, muốn đếm lại
  thì tạo đối tượng mới.

## Cách KMP dịch pattern

| Biến | Ý nghĩa |
|---|---|
| `start` | vị trí trong text mà pattern đang đặt vào |
| `matched` | số ký tự của pattern đang khớp |
| `keep = lps[matched - 1]` | số ký tự đã khớp được giữ lại, không so lại |
| `delta = matched - keep` | số ô pattern dịch sang phải |

So với brute force chỉ khác hai dòng: `start++` thành `start += delta`, và `matched = 0` thành
`matched = keep`. Khi `start` tăng `delta` thì `matched` giảm đúng `delta`, nên `start + matched`, vị
trí đang đọc trong text, không bao giờ lùi. Nhờ đó phần tìm tốn O(n), cộng O(m) dựng bảng LPS thành
O(n + m), còn brute force là O(nm).

Ví dụ của slide: text `10101011`, pattern `101011`, LPS `0, 0, 1, 2, 3, 1`. Năm ký tự đầu khớp, rồi
`text[5]` khác `search[5]`. Lúc đó `keep = lps[4] = 3`, `delta = 5 - 3 = 2`, nên `start` lên 2 và
`matched` về 3. Phép so tiếp theo vẫn đọc `text[5]`, chỉ còn ba cặp mới phải so.

- `matched == 0`: không có `lps[-1]`, `start` tăng 1.
- Khớp đủ pattern: vẫn dịch theo `keep` và `delta`, nên tìm được các lần khớp chồng lên nhau, ví dụ
  `AA` trong `AAAA` ở 0, 1, 2.

## Ví dụ trong Demo

`StringFinderDemo` chạy các ví dụ của slide 8-TextProcessing rồi tới một text toàn chữ A, in bảng LPS,
vị trí tìm thấy và số phép so của từng thuật toán:

| Text | Pattern | Vị trí | Brute force | KMP |
|---|---|---|---|---|
| `abcabaabcabac` | `abaa` | 3 | 21 | 19 |
| `aaaaaaaa` | `aaab` | không có | 20 | 17 |
| `10101011` | `101011` | 2 | 13 | 16 |
| `1010001010110` | `101011` | 6 | 19 | 21 |
| 2000 dòng toàn A, có ba chữ B | 19 chữ A rồi B | 39941, 95941, 151941 | 2,819,240 | 317,958 |

Số của KMP đã gồm cả phép so lúc dựng LPS, nên trên text ngắn KMP có thể tốn hơn brute force. Trên
text toàn chữ A, brute force so gần đủ 20 ký tự ở mỗi vị trí, còn KMP chỉ dịch 1 ô rồi so tiếp.

```bash
java -cp out text.sample.StringFinderDemo
java -cp out text.sample.StringFinderDemo sample-text.txt BENVOLIO
```

Lệnh thứ hai tìm trong một file UTF-8 của bạn, đường dẫn tính từ thư mục đang đứng. File không tồn
tại thì chương trình dừng với `NoSuchFileException`.
