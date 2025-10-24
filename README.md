# 🕒 Sistem Absensi Karyawan - Java NetBeans

Program ini adalah aplikasi **Absensi Karyawan** berbasis **Java Desktop (NetBeans)** yang digunakan untuk mencatat kehadiran, izin, dan keterlambatan karyawan secara sederhana dan efisien.

---

## 🚀 Fitur Utama

✅ **Login Admin & User**
- Admin dapat mengelola data karyawan dan melihat laporan absensi.  
- User (karyawan) dapat melakukan absen masuk dan keluar.

✅ **Manajemen Data Karyawan**
- Tambah, ubah, hapus, dan cari data karyawan.

✅ **Pencatatan Absensi Otomatis**
- Menyimpan waktu absen masuk dan keluar secara otomatis menggunakan `java.time.LocalDateTime`.

✅ **Laporan Absensi**
- Menampilkan data absensi harian atau bulanan.  

✅ **Validasi Input**
- Cegah input kosong dan validasi data yang tidak sesuai.

---

## 🧩 Teknologi yang Digunakan

| Komponen | Deskripsi |
|-----------|------------|
| **Bahasa Pemrograman** | Java |
| **IDE** | NetBeans (8.2 atau lebih baru) |
| **Database** | MySQL (dapat disesuaikan) |
| **Library** | JDBC, Swing, Java AWT |
| **Tools Tambahan (opsional)** | JasperReports (untuk cetak laporan) |

---

## 🛠️ Cara Menjalankan Aplikasi

1. **Clone atau Download Repository**
   ```bash
   git clone https://github.com/username/AbsensiKaryawan.git
2. **Buka Project di NetBeans**
   - Buka NetBeans.
   - Pilih File > Open Project.
   - Arahkan ke folder hasil clone atau folder project lokal.
3. **Import Database**
   - Buka MySQL Workbench atau phpMyAdmin.
   - Buat database baru, misalnya db_absensi_karyawan.
   - Import file SQL yang disediakan di folder database.
4. **Konfigurasi Koneksi Database**
   - Cari file koneksi (misalnya Koneksi.java atau sejenisnya).
   - Sesuaikan pengaturan berikut dengan lingkungan lokal Anda:
     ```Contoh:
       String url  = "jdbc:mysql://localhost:3306/db_absensi_karyawan";
       String user = "root";
       String pass = "";
5. **Jalankan Aplikasi**
   - Klik kanan project → Run di NetBeans.
