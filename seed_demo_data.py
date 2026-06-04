import sqlite3
import random

DB_PATH = r"C:\Users\vu\Desktop\ClassManagerDemoV2\ClassManagerDemoV2\class_manager.db"

# Vietnamese name pools
HO = ["Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Phan", "Vũ", "Đặng", "Bùi", "Đỗ",
      "Hồ", "Ngô", "Dương", "Lý", "Đinh", "Tô", "Mai", "Cao", "Lưu", "Trịnh"]

DEM_NAM = ["Văn", "Quốc", "Đức", "Minh", "Anh", "Tuấn", "Trung", "Hữu", "Công", "Gia"]
DEM_NU  = ["Thị", "Ngọc", "Kim", "Thu", "Lan", "Thanh", "Thùy", "Hồng", "Phương", "Mỹ"]

TEN_NAM = ["An", "Bảo", "Dũng", "Hải", "Khoa", "Long", "Minh", "Nam", "Phong",
           "Quân", "Sơn", "Tiến", "Toàn", "Tùng", "Việt", "Mạnh", "Hùng", "Khải", "Lâm", "Thắng"]
TEN_NU  = ["Chi", "Giang", "Lan", "Linh", "Nga", "Thảo", "Trang", "Uyên", "Vân",
           "Yến", "Hà", "Hương", "Mai", "Nhi", "Quỳnh", "Tú", "Xuân", "Diệu", "Lam", "Nhung"]

SUBJECTS = ["Toán", "Lý", "Hóa", "Anh", "CNTT"]
SEMESTER = "HK1_2024"

CLASSES = [
    ("22DHT01", "2022-2026"),
    ("22DHT02", "2022-2026"),
    ("22DHT03", "2022-2026"),
    ("22DHT04", "2022-2026"),
    ("22DHT05", "2022-2026"),
]

def rand_score():
    # random in [4.0, 9.5] step 0.5
    return round(random.choice([x * 0.5 for x in range(8, 20)]), 1)

def gen_name(idx):
    is_female = (idx % 3 == 0)
    ho = HO[idx % len(HO)]
    if is_female:
        dem = DEM_NU[idx % len(DEM_NU)]
        ten = TEN_NU[idx % len(TEN_NU)]
    else:
        dem = DEM_NAM[idx % len(DEM_NAM)]
        ten = TEN_NAM[idx % len(TEN_NAM)]
    return f"{ho} {dem} {ten}"

def gen_phone(seed):
    prefixes = ["0903", "0912", "0988", "0977", "0908", "0936", "0961", "0972", "0984", "0945"]
    prefix = prefixes[seed % len(prefixes)]
    suffix = str(100000 + (seed * 137 + 31) % 900000)
    return prefix + suffix

conn = sqlite3.connect(DB_PATH)
cur = conn.cursor()

# 1. Xóa data cũ (giữ lại users)
cur.execute("DELETE FROM scores")
cur.execute("DELETE FROM students")
cur.execute("DELETE FROM classes")
cur.execute("DELETE FROM sqlite_sequence WHERE name IN ('scores','students','classes')")

print("Đã xóa data cũ.")

# 2. Thêm 5 lớp
class_ids = []
for cls_name, cls_year in CLASSES:
    cur.execute("INSERT INTO classes (name, school_year) VALUES (?, ?)", (cls_name, cls_year))
    class_ids.append(cur.lastrowid)
    print(f"  Thêm lớp: {cls_name} (id={cur.lastrowid})")

# 3. Thêm 20 sinh viên mỗi lớp + điểm 5 môn
global_idx = 0
for cls_idx, (cls_name, _) in enumerate(CLASSES):
    class_id = class_ids[cls_idx]
    for sv_idx in range(1, 21):
        name = gen_name(global_idx)
        mssv = f"{cls_name}{sv_idx:03d}"
        email = f"sv{mssv.lower()}@vaa.edu.vn"
        phone = gen_phone(global_idx)

        cur.execute(
            "INSERT INTO students (name, className, email, phone, student_code, class_id) VALUES (?,?,?,?,?,?)",
            (name, cls_name, email, phone, mssv, class_id)
        )
        student_id = cur.lastrowid

        for subject in SUBJECTS:
            score = rand_score()
            cur.execute(
                "INSERT INTO scores (student_id, subject, score, semester) VALUES (?,?,?,?)",
                (student_id, subject, score, SEMESTER)
            )

        global_idx += 1

conn.commit()
conn.close()

print(f"\nHoàn thành! Đã tạo:")
print(f"  - 5 lớp: 22DHT01 → 22DHT05")
print(f"  - 100 sinh viên (20 / lớp)")
print(f"  - 500 bản ghi điểm (5 môn / sinh viên)")
