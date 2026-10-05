# CSD201: Data Structures and Algorithms

Code mẫu cho môn CSD201 (Cấu trúc dữ liệu và giải thuật). Giáo trình chính: Goodrich, Tamassia,
Goldwasser, *Data Structures and Algorithms in Java*, 6th edition.

## Nội dung

| Bài | Package | Syllabus |
|---|---|---|
| Cây: cây tổng quát, cây nhị phân, duyệt cây, BST, AVL | [`tree`](tree) | 4.1 đến 4.7 |
| Đồ thị: danh sách kề, ma trận kề, DFS, BFS | [`graph`](graph) | 5.1 đến 5.3 |

## Cách tổ chức

Mỗi bài là một package nằm ngay ở thư mục gốc, chỉ gồm file `.java`. Unit test để riêng trong
`test/`, cùng tên package:

```
tree/                 code mẫu của bài cây
  sample/             Student và các class *Demo có hàm main để chạy thử
test/
  tree/               unit test JUnit 4 cho package tree
```

Repo không kèm Maven, Gradle hay file project của IDE. Code chỉ dùng cú pháp Java 8, nên biên dịch
được trên NetBeans 8 với JDK 1.8 (môi trường thi PE) và cả các JDK mới hơn.

## Mở trong IDE

Mở thư mục gốc của repo. Source root là chính thư mục gốc (package `tree` nằm ngay bên dưới),
test root là `test/`, và thêm JUnit 4 vào classpath của phần test.

- **NetBeans**: File > New Project > Java (NetBeans 8) hoặc Java with Ant (bản mới) > Java Project
  with Existing Sources. Source Package Folders chọn thư mục gốc, Test Package Folders chọn `test`.
- **Eclipse**: tạo Java Project mới, bỏ chọn *Use default location* và trỏ tới thư mục gốc. Chuột
  phải `test` > Build Path > Use as Source Folder. Eclipse sẽ đề nghị loại `test/` khỏi source
  gốc, chọn đồng ý.
- **IntelliJ IDEA, VS Code**: mở thư mục gốc, đánh dấu thư mục gốc là source root và `test` là test
  source root.

File project do IDE sinh ra đã nằm trong `.gitignore`.

## Chạy bằng dòng lệnh

Đứng ở thư mục gốc của repo. Với JDK 22 trở lên, chạy thẳng từ file nguồn:

```bash
java tree/sample/StudentTreeDemo.java
```

Với JDK 8 đến 21 thì biên dịch trước (macOS, Linux hoặc Git Bash trên Windows):

```bash
mkdir -p out
javac -d out $(find tree -name "*.java")
java -cp out tree.sample.StudentTreeDemo
```

Chạy test cần hai file jar của JUnit 4:

```bash
mkdir -p lib
curl -sSLo lib/junit-4.13.2.jar https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
curl -sSLo lib/hamcrest-core-1.3.jar https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
javac -d out -cp "out:lib/*" $(find test -name "*.java")
java -cp "out:lib/*" org.junit.runner.JUnitCore $(cd test && find . -name "*Test.java" | sed 's|^\./||; s|\.java$||; s|/|.|g')
```

GitHub Actions chạy đúng các bước trên với JDK 8 và JDK 21 ở mỗi lần push, kể cả chạy mọi `*Demo`.

## Quy ước viết code

- Không comment trong code, không annotation trang trí. Tên class, method, biến phải tự nói lên ý
  nghĩa. Annotation duy nhất là `@Test` của JUnit.
- Mỗi class làm một việc. Bên ngoài chỉ đọc được `Node` (getter public), còn setter chỉ dùng được
  trong package của cây, nên không ai phá được thứ tự của BST mà không qua method công khai.
- Phép duyệt trả về `List` thay vì in ra màn hình, để test được và để Demo tự quyết cách in.
- Ví dụ trong các Demo lấy từ slide và giáo trình, để sinh viên đối chiếu kết quả.
