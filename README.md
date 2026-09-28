# CSD201: Data Structures and Algorithms

Code mẫu cho môn CSD201 (Cấu trúc dữ liệu và giải thuật). Giáo trình chính: Goodrich, Tamassia,
Goldwasser, *Data Structures and Algorithms in Java*, 6th edition.

## Nội dung

| Bài | Thư mục | Syllabus |
|---|---|---|
| Cây: cây tổng quát, cây nhị phân, duyệt cây, BST, AVL | [`java/trees`](java/trees) | 4.1 đến 4.7 |

## Cách tổ chức

Mỗi bài là một thư mục độc lập, chỉ gồm file `.java`:

```
java/<bài>/
  src/    code mẫu, các class *Demo có hàm main để chạy thử
  test/   unit test JUnit 4 cho code trong src/
```

Repo không kèm Maven, Gradle hay file project của IDE. Code chỉ dùng cú pháp Java 8, nên biên dịch
được trên NetBeans 8 với JDK 1.8 (môi trường thi PE) và cả các JDK mới hơn.

## Mở trong IDE

- **NetBeans**: File > New Project > Java (NetBeans 8) hoặc Java with Ant (bản mới) > Java Project
  with Existing Sources. Thêm `src` vào Source Package Folders, `test` vào Test Package Folders.
- **Eclipse**: tạo Java Project mới, bỏ chọn *Use default location* và trỏ tới thư mục bài
  (ví dụ `java/trees`). Nếu `test` chưa là source folder: chuột phải > Build Path >
  Use as Source Folder. Thêm JUnit 4 vào Build Path.
- **IntelliJ IDEA, VS Code**: mở thư mục bài, đánh dấu `src` là source root và `test` là
  test source root.

File project do IDE sinh ra đã nằm trong `.gitignore`.

## Chạy bằng dòng lệnh

macOS, Linux hoặc Git Bash trên Windows, đứng trong thư mục bài (ví dụ `java/trees`):

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out csd201.tree.sample.StudentTreeDemo
```

Chạy test cần hai file jar của JUnit 4:

```bash
mkdir -p lib
curl -sSLo lib/junit-4.13.2.jar https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
curl -sSLo lib/hamcrest-core-1.3.jar https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
javac -d out -cp "out:lib/*" $(find test -name "*.java")
java -cp "out:lib/*" org.junit.runner.JUnitCore $(cd test && find . -name "*Test.java" | sed 's|^\./||; s|\.java$||; s|/|.|g')
```

GitHub Actions chạy đúng các bước trên với JDK 8 và JDK 21 ở mỗi lần push.

## Quy ước viết code

- Không comment trong code, không annotation trang trí. Tên class, method, biến phải tự nói lên ý
  nghĩa. Annotation duy nhất là `@Test` của JUnit.
- Mỗi class làm một việc. Bên ngoài chỉ đọc được `Node` (getter public), còn setter chỉ dùng được
  trong package của cây, nên không ai phá được thứ tự của BST mà không qua method công khai.
- Phép duyệt trả về `List` thay vì in ra màn hình, để test được và để Demo tự quyết cách in.
- Ví dụ trong các Demo lấy từ slide và giáo trình, để sinh viên đối chiếu kết quả.
