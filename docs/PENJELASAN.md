# Penjelasan gerak objek

Objek mengalami gravitasi hanya sampai pertama kali menyentuh lantai. Setelah
pantulan pertama, gravitasi sengaja dimatikan dan objek bergerak lurus dengan
pantulan pada keenam dinding. Ini adalah penyederhanaan yang disengaja, bukan
fisika murni: tujuannya agar objek tidak berhenti dan gerak terlihat hidup.
Status FINISHED muncul setelah tidak ada objek aktif dan animasi menghilang
selesai.

Tahap 5 tidak dikerjakan karena limas sudah digabung ke Tahap 4; massa dari
volume (6c) dan sensor kemiringan (6d) dibatalkan.

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

## Jejak bayangan

Motion trail adalah beberapa salinan kecil di posisi-posisi terakhir objek,
agar arah geraknya gampang dilihat. Setiap objek punya ring buffer enam posisi:
posisi baru menimpa yang paling lama, jadi kapasitas dan biaya gambar tetap
terbatas. Posisi dicatat tiap `TRAIL_INTERVAL = 0,03` detik, bukan tiap frame,
supaya panjang jejak tidak bergantung pada cepat-lambatnya layar menggambar.

Salinan digambar dari yang terbaru ke terlama: alpha turun dari `0,40` sampai
`0,05`, dan skala turun dari `0,9` sampai `0,4`. Dengan `TRAIL_ENABLED = true`,
jejak aktif; konstanta ini bisa diubah menjadi `false` untuk membandingkan
performa. Jejak memakai posisi dunia, jadi ikut berputar bersama wadah.
Jejak dihapus saat Mulai, Ulangi, objek mulai VANISHING, atau objek REMOVED.

Objek solid digambar dulu agar depth buffer berisi kedalamannya. Saat menggambar
salinan, depth test tetap aktif tetapi depth mask dimatikan: salinan yang ada di
belakang objek solid tetap tertutup, sementara salinan tidak menulis kedalaman
yang dapat mengganggu salinan lain. Wadah transparan digambar terakhir supaya
sisi wadah tetap terlihat di depan atau belakang jejak sesuai kedalamannya.
Lighting dimatikan untuk salinan agar warna dan transparansinya konsisten.

Batasannya, jejak ini bukan motion blur sungguhan; ia hanya menggambar ulang
mesh yang sama di posisi lama. Maksimal ada 12 objek x 6 salinan (72 salinan)
per frame.
