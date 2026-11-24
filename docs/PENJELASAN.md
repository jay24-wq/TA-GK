# Penjelasan gerak objek

Objek mengalami gravitasi hanya sampai pertama kali menyentuh lantai. Setelah
pantulan pertama, gravitasi sengaja dimatikan dan objek bergerak lurus dengan
pantulan pada keenam dinding. Ini adalah penyederhanaan yang disengaja, bukan
fisika murni: tujuannya agar objek tidak berhenti, permainan pasti selesai,
dan gerak terlihat hidup.

Fitur sensor kemiringan nantinya dapat menghidupkan kembali gravitasi dengan
mengambil arah gravitasi dari accelerometer.

## Pencahayaan

Bola dan limas memakai satu sumber cahaya OpenGL supaya bentuk 3D-nya lebih
mudah terlihat. Titik bola memakai arah dari pusat sebagai normal. Normal ini
memang sudah panjang satu, tetapi GL_NORMALIZE tetap diaktifkan karena objek
diskalakan saat digambar. Limas memakai normal datar per sisi; titik sudutnya
disalin untuk tiap segitiga supaya cahaya tidak menghaluskan batas antar-sisi.
Normal hasil cross product dibalik jika arahnya masuk ke dalam limas.

Cahaya ambient bernilai `(0,24; 0,24; 0,28)` agar sisi gelap tidak hilang,
sedangkan diffuse `(0,9; 0,9; 0,85)` memberi arah cahaya utama. Posisi `(4; 6;
5; 1)` ditetapkan sebelum kamera dan rotasi tampilan, jadi cahaya tetap di
posisi layar yang sama ketika wadah diputar. Wadah digambar paling akhir tanpa
lighting agar garis dan transparansinya tetap seperti semula.
