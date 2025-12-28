Payment API Projesi – Teknik Tasarım ve Geliştirme Dokümanı


1. Projenin Amacı ve Kapsamı


Bu projenin amacı, gerçek bir banka ödeme sürecini temel alan bir Payment API tasarlamak ve bu API’yi uçtan uca test edilebilir hale getirmektir.



Proje geliştirilirken aşağıdaki hedefler gözetilmiştir:

Gerçekçi bir banka ödeme akışını modellemek

API prensiplerine uygun endpoint’ler oluşturmak

PostgreSQL üzerinde kalıcı veri yönetimi sağlamak

Karmaşık SQL sorgularını (JOIN, GROUP BY, HAVING) kullanabilmek

Postman üzerinden otomatik ve manuel test edilebilir bir yapı kurmak




Bu proje bir production sistemi değildir; teknik yetkinliği ve yaklaşımı göstermek amacıyla hazırlanmıştır.

2. Genel Mimari Yaklaşım


Proje, klasik katmanlı mimari yaklaşımı ile geliştirilmiştir:

Controller Layer

HTTP isteklerini karşılayan endpoint’ler

Service Layer

İş kurallarının ve ödeme akışının yönetildiği katman

Repository Layer

PostgreSQL ile veri erişiminin sağlandığı katman

Database Layer

Ödeme ve müşteri verilerinin tutulduğu PostgreSQL veritabanı




3. Teknoloji Seçimleri ve Gerekçeleri


3.1 Java ve Spring Boot


Spring Boot, hızlı API geliştirme, dependency yönetimi ve konfigürasyon kolaylığı sağladığı için tercih edilmiştir.



3.2 PostgreSQL


PostgreSQL şu nedenlerle seçilmiştir:

Kalıcı veri saklama ihtiyacı

Banka senaryolarına uygunluk

Karmaşık SQL sorgularını güçlü şekilde desteklemesi

JOIN, GROUP BY, HAVING gibi yapıların gerçekçi kullanımını gösterebilmek

4. Veritabanı Tasarımı


Projede temel olarak aşağıdaki tablolar oluşturulmuştur:

customers
payments
ledger
orders


Bu tablolar arasında ilişkiler tanımlanmıştır.

Örnek kullanım senaryoları:

Müşteriye ait tüm ödemelerin listelenmesi

Günlük / müşteri bazlı ödeme toplamlarının hesaplanması

Belirli tutarın üzerindeki müşterilerin raporlanması



Bu sorgular sayesinde sadece CRUD değil, analitik SQL kullanımı da gösterilmiştir.

5. Ödeme Akışının Tasarlanması


Ödeme süreci, gerçek banka sistemlerine benzer şekilde aşamalara bölünmüştür.



5.1 Initiate Payment
Ödeme talebi oluşturulur

Ödeme başlangıç durumunda kaydedilir

paymentId üretilir



5.2 Update Initiated Payment
Başlatılmış ödeme üzerinde güncelleme yapılabilir

Tutar veya açıklama gibi alanlar düzenlenebilir



5.3 Provision Payment
Ödeme için provizyon alınır

Ödeme henüz tamamlanmaz



5.4 Complete Payment
Provizyonu alınmış ödeme tamamlanır

Ödeme durumu final hale getirilir



5.5 Get Payment
Ödeme detayları sorgulanır

Mevcut durum kontrol edilir



Bu yapı, banka ödeme yaşam döngüsünü birebir yansıtacak şekilde tasarlanmıştır.

6. HTTP Method Kullanımı


Projede farklı HTTP metodları, bilinçli ve gerçek kullanım senaryolarına uygun şekilde kullanılmıştır:

POST

Yeni ödeme oluşturma ve ödeme işlemleri

GET

Ödeme ve müşteri bilgilerini sorgulama

PUT

Ödeme bilgisini tamamen güncelleme

PATCH

Kısmi alan güncellemeleri

DELETE

Ödeme iptali

HEAD

Kaynağın varlığını ve header bilgilerini kontrol etme

OPTIONS

Endpoint’in desteklediği HTTP metodlarını görüntüleme



Bu sayede API’nin sadece çalışması değil, HTTP standartlarına uygunluğu da gösterilmiştir.

7. Postman Yapısının Oluşturulması


Backend geliştirme tamamlandıktan sonra API testleri için Postman kullanılmıştır.



7.1 Collection Yapısı


Postman içinde:

Payment API Collection

Akışa göre gruplanmış request’ler

End-to-end çalıştırılabilir senaryo



oluşturulmuştur.



7.2 Environment Kullanımı


Dinamik değerlerin manuel girilmemesi için Environment kullanılmıştır.



Örnek:

paymentId → bir request’ten alınıp diğerlerinde otomatik kullanılır



Bu sayede testler tekrar edilebilir ve hatasız hale getirilmiştir.

8. Collection Runner ile End-to-End Test


Tüm ödeme akışı, Postman Collection Runner kullanılarak:

Tek tuşla

Belirli bir sırayla

Otomatik olarak



çalıştırılabilmektedir.



Bu yapı, CI/CD süreçlerine uygun bir test yaklaşımı sunduğunu da göstermektedir.

9. Projenin Açık Kaynak Hale Getirilmesi


Proje GitHub’a eklenirken aşağıdaki prensipler uygulanmıştır:

Postman Collection ve Environment dosyaları repoya eklenmiştir

README dosyasında çalıştırma adımları detaylı yazılmıştır

Projeyi indiren birinin ek konfigürasyona ihtiyaç duymaması hedeflenmiştir

10. Sonuç ve Kazanımlar


Bu proje sayesinde:

Gerçek banka ödeme akışı modellenmiştir

API tasarımı ve test yaklaşımı uçtan uca gösterilmiştir

PostgreSQL ile kalıcı ve gerçekçi veri yönetimi sağlanmıştır

Postman ile profesyonel test yapısı kurulmuştur

Açık kaynak ve paylaşılabilir bir proje ortaya çıkarılmıştır
