# Mobile_Totbiz
Tugas Mobile 2020 - Kelas Ko Andre Rusli
LAPORAN PROJECT 
IF-633 MOBILE APPLICATION

APLIKASI TENTANG BOOKING MEETING UNTUK INTERNAL TEAM TOTALLY BIZARRE
 
Disusun Oleh Kelompok
1.     Celvyn Yulian - 00000019475
2.     Jonathan - 00000019524
3.     Mikhail Aresa - 00000019452
4.     Stefanus Dwitra Lauwrendo – 00000019999

PROGRAM STUDI INFORMATIKA
FAKULTAS TEKNIK DAN INFORMATIKA
UNIVERSITAS MULTIMEDIA NUSANTARA
TANGERANG
2020
DESKRIPSI APLIKASI


	Aplikasi yang kami buat merupakan aplikasi yang dikhususkan untuk Start Up yang sedangkan kami bangun untuk kepentingan melakukan penjadwalan meeting internal team kami. Tujuan dari aplikasi ini mempermudah melakukan penjadwalan meeting agar lebih teratur dan sistematis. Di dalam meeting tersebut berisi topik utama tentang meeting, lalu deskripsi secara singkat tentang meeting, kapan dilakukannya meeting, dan jika ada dokumen yang ingin dibaca terlebih dahulu oleh peserta meeting dapat dimasukan kedalam pembuatan jadwal meeting. 

Untuk alur aplikasinya, pertama user yang belum mempunyai akun pada aplikasi kami dapat menekan tombol register pada halaman awal. Lalu setelah register kita akan langsung login ke dalam aplikasi, apalagi kita logout maka kita akan dapat login kembali dengan masukan email dan password yang telah kita daftar sebelumnya. Lalu di halaman utama user dapat melihat profile dengan mengklik tombol pada header bar, disana akan muncul foto profil hingga biodata yang di input pada register. Lalu di sini  kita dapat menggunakan kamera ataupun gambar yang berada pada local storage kita untuk mempercantik user profil kita. Setelah itu kembali ke halaman utama user dapat memasukkan meeting dengan menekan tombol insert. 

Kemudian, user akan dibawa ke dalam form  yang dimana user dapat melakukan input pada judul meeting, deskripsi meeting, tanggal meeting, status meeting dan jika ada dokumen yang ingin user baca sebelum meeting maka dapat mengupload dokumen tersebut. Kemudian, di halaman utama maka hasil meeting yang di input akan muncul listnya yang akan berubah sesuai dengan warna status. Jika user ingin mengubah status mengedit judul ataupun mendownload dokumen yang diinput dapat menekan salah satu list magang yang diinginkan, untuk hasil download tersebut akan berada pada folder download yang mana penamaan filenya akan sesuai dengan judul meeting.















List Fitur-Fitur

Register user baru 
Login berdasarkan Registered User
Menampilkan List Data dari Firestore database
Menampilkan item berdasarkan posisi tertentu dalam List
Mengedit data berdasarkan posisi dalam List
Mendownload dokumen dari item list yang dipilih dan dimasukan ke external storage
Upload data dan dokumen baru ke Database
Menampilkan menu My Profile dari user yang sedang login
Mengubah data pada menu My Profile
Delete data pada main menu dengan fitur Swipe (Akan muncul prompt konfirmasi Yes/No)
Mengakses kamera atau membuka storage/gallery saat ingin mengubah Profile Picture
Error Handling



Spesifikasi Aplikasi

Bahasa Pemrograman		: Java Android
IDE				: Android Studio
Minimum Android Version	: 7.0 (Nougat/API 24)
Target Android Version		: 10 (Android10/API 29)
Storage				: 15 MB available space
Permission:
Internet Connection
Read/Write External Storage
Read Phone State
Access Camera
Library:

Constraint Layout 
Recycler View
Firebase Functions
Firebase Firestore
Firebase UI Firestore
Firebase UI Auth
Firebase UI Storage
Firebase Client Android
Firebase Database
Firebase Auth
Firebase Storage
Android Multidex
Android Material
Firebase Core
Firebase Analytics
Room Runtime
Room Compiler
Appcompat
Bumptech/Glide
Android/FileProvider


Asset:
@drawable/user_icon.png


@drawable/home.png

@drawable/kue.png

@drawable/nama.png

@drawable/surat.png

@drawable/logo_totallybizarre_color.png









