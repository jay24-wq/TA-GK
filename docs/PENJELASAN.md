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

## Animasi menghilang

Saat dua objek sejenis bertabrakan, skor langsung bertambah dan keduanya masuk
status VANISHING. Selama sekitar `0,3` detik, skala tampilan turun lurus dari
`1` ke `0`; setelah itu objek berstatus REMOVED. Skala hanya dipakai renderer,
jadi radius dan tumbukan fisika tidak berubah. Objek VANISHING langsung
dinonaktifkan dari tumbukan dan keepMoving, lalu diam di posisi terakhir selama
animasi. Status permainan baru menjadi FINISHED setelah semua objek aktif dan
semua animasi VANISHING selesai. Ulangi membuat objek baru dengan status normal
dan skala `1`.

Durasi `VANISH_DURATION = 0,3` detik dipilih supaya efek terlihat tetapi tidak
memperlambat putaran permainan. Batasannya, efek ini hanya mengecilkan objek;
tidak ada blur atau efek partikel.
