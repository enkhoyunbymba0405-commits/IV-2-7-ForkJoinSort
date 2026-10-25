# БД1 илгээх заавар — IV-2-7

## 1. GitHub repository үүсгэх

GitHub дээр шинэ repo үүсгэнэ (нэр жишээ нь `IV-2-7-ForkJoinSort`, **Public**,
README-гүйгээр хоосон үүсгэнэ).

Дараа нь төслийн фолдер дотроо:

```bash
cd IV-2-7-ForkJoinSort

git init
git add .
git commit -m "IV-2-7 Fork-Join Sorting Application: код, тест, тайлан, Manual"

git branch -M main
git remote add origin https://github.com/<ХЭРЭГЛЭГЧ>/IV-2-7-ForkJoinSort.git
git push -u origin main
```

**Release/Tag** (9-р долоо хоногийн шаардлага):
```bash
git tag -a v1.0 -m "БД1 эцсийн хувилбар — IV-2-7"
git push origin v1.0
```

GitHub дээр **Releases → Draft a new release → v1.0 → Publish** гэж бас хийнэ.

### README-г засах
`README.md` дотор `<repo-url>` гэсэн гурван газар байгааг жинхэнэ холбоосоор
солино:
```bash
sed -i 's|<repo-url>|https://github.com/<ХЭРЭГЛЭГЧ>/IV-2-7-ForkJoinSort.git|g' README.md
git add README.md && git commit -m "README: repo холбоос" && git push
```

### Багаар хамтран ажилласныг харуулах
Тайлангийн шаардлагад "GitHub дээрх ажиллагааг тайландаа тусгана" гэсэн.
Гишүүн бүр дор хаяж нэг commit хийвэл сайн — жишээ нь тест нэмэх,
README засах, Manual-д жишээ нэмэх.

---

## 2. Teams-ийн БД1 пост

**Сувг:** `БД1 - Submissions and Reviews`
**Гарчиг:**

```
IV-2-7 – Fork-Join Sorting Application
```

**Постын текст:**

```
Код: IV-2-7
Төсөл: Fork-Join Sorting Application (Бүлэг 4, Programming Project 3, х. P-27–P-28)

Товч үр дүн: Java-гийн fork-join API ашиглан Mergesort ба Quicksort хоёр
хувилбарыг RecursiveAction дээр хэрэгжүүлж, рекурсив задралыг интерактив
модоор харууллаа. Зангилаа бүр compute() дуудлага, өнгө нь гүйцэтгэсэн
ажилчин урсгалыг заана; threshold, массивын хэмжээ, урсгалын тоо, өгөгдлийн
хэв маягийг UI-аас өөрчилж дахин турших боломжтой. 58/58 тест давсан,
хоёр цөмт машин дээр 1.95–2.15 дахин хурдасгалт хэмжигдсэн.

GitHub: https://github.com/<ХЭРЭГЛЭГЧ>/IV-2-7-ForkJoinSort

Туршиж үзэх: README-ийн Quick Start хэсэг.
  ./build.sh && ./run.sh        (Linux/macOS)
  build.bat && run.bat          (Windows)
  ./test.sh                     — 58 тест
Дэлгэцгүй орчинд: ./run.sh --headless -n 100000 -t 100

Хавсралт: IV-2-7_BD1_Report.pdf, IV-2-7_Manual.pdf
```

**Хавсаргах файл:**
1. `IV-2-7_BD1_Report.pdf`
2. `IV-2-7_Manual.pdf`

> Баг бүр **зөвхөн нэг** пост хэрэглэнэ.

---

## 3. 10-р долоо хоног — шүүмж

Та **IV-2-10** багийг шүүмжилнэ (The Sleeping Teaching Assistant, Бүлэг 7).

Хийх дараалал (Peer Review удирдамжийн дагуу):
1. Тэдний БД1 постоос Report, Manual, GitHub холбоосыг бүртгэх
2. Тайланг уншиж ойлгох
3. Manual-ийг **өөрчлөхгүйгээр** Build/Run хийх
4. Үндсэн тестийг давтах
5. **Дор хаяж нэг нэмэлт тест** (жишээ нь сандлын тоо = 0, эсвэл 50 оюутан зэрэг)
6. Queue animation нь бодит төлөвтэй нийцэж байгааг шалгах
7. Review PDF бичиж, тэдний постын Reply-д тавих

Файлын нэр: `IV-2-7_Review_IV-2-10.pdf`

> **Чухал:** Хэрэв IV-2-10 баг постоо тавиагүй бол, та өөрийн постоо
> тавьсан нөхцөлд шүүмжийн **бүтэн оноог** автоматаар авна.

---

## 4. Эцсийн шалгах жагсаалт

- [ ] Тайлангийн нүүр хуудсанд багийн 6 гишүүний нэр бүрэн
- [ ] GitHub repo нийтлэгдсэн, README-д жинхэнэ холбоос
- [ ] v1.0 tag / Release үүссэн
- [ ] Гишүүн бүр дор хаяж нэг commit хийсэн
- [ ] БД1 пост нийтлэгдсэн, хоёр PDF хавсаргасан
- [ ] `./build.sh` ба `./test.sh` цэвэр машин дээр ажилласан
- [ ] 10-р долоо хоногт IV-2-10-д Review PDF илгээсэн
