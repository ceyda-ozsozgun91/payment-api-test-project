Payment API – End-to-End Payment Flow
# Payment API Test Projesi

Bu proje, gerçek banka ödeme sistemlerinden ilham alınarak hazırlanmış uçtan uca bir ödeme akışını modellemektedir.  
Projenin temel amacı; ödeme yaşam döngüsünü yönetmek, REST API uç noktalarını Postman ile test etmek ve veritabanı tutarlılığını SQL sorguları ile doğrulamaktır.

## Proje Kapsamı

Bu proje aşağıdaki konulara odaklanmaktadır:
- Ödeme yaşam döngüsü yönetimi
- Farklı HTTP metodlarının (POST, GET, PUT, PATCH, DELETE, HEAD, OPTIONS) kullanımı
- Postman ile API testleri
- JOIN, GROUP BY, HAVING içeren karmaşık SQL sorguları
- Uçtan uca ödeme senaryosu testleri

Finansal rollback ve transaction yönetimi bilinçli olarak kapsam dışında bırakılmıştır.

## Ödeme Yaşam Döngüsü

Projede uygulanan ödeme akışı aşağıdaki gibidir:

1. INITIATED – Ödeme isteği oluşturulur
2. PROVISIONED – Ödeme tutarı rezerve edilir
3. SUCCESS – Ödeme tamamlanır
4. FAILED – Ödeme başarısız olarak işaretlenir

## API Uç Noktaları

### End-to-End Payment Flow
- POST /payments/initiate
- PUT /payments/update
- POST /payments/provision
- POST /payments/complete
- GET /payments/{id}
- HEAD /payments/{id}
- OPTIONS /payments
- PATCH /payments/description
- DELETE /payments/cancel

### Raporlama ve Sorgulama
- GET /customers
- GET /customers/high-value
- GET /customers/top-success
- GET /customers/without-success

## Kullanılan Teknolojiler

- Java
- Spring Boot
- JDBC
- SQL
- Postman
- PostgreSQL

## Postman Kullanımı

Tüm API çağrıları tek bir Postman Collection altında toplanmıştır.  
Environment değişkenleri kullanılarak dinamik veri yönetimi sağlanmıştır.  
Collection Runner ile uçtan uca senaryolar tek tuşla çalıştırılabilmektedir.

## Projenin Amacı

Bu proje;
- API test yetkinliğini
- Bankacılık ödeme domain bilgisi
- JOIN, GROUP BY, HAVING içeren SQL sorgularını kullanmak
- Postman Collection ve Environment ile uçtan uca test edilebilir bir yapı kurmak
- Veritabanı doğrulama becerisini

göstermek amacıyla hazırlanmıştır.

## Veritabanı Yapısı

Bu projede **PostgreSQL** kullanılmaktadır.

- Ödeme ve müşteri tabloları arasında ilişkiler kurulmuştur
- JOIN, GROUP BY ve HAVING içeren sorgular kullanılmaktadır
- Müşteri bazlı ödeme toplamları ve raporlama senaryoları desteklenmektedir

Bu yapı, banka sistemlerinde sıkça karşılaşılan raporlama ve analiz ihtiyaçlarını yansıtacak şekilde tasarlanmıştır.

## PostgreSQL Kurulumu

### Gereksinimler

- PostgreSQL 14 veya üzeri

### Veritabanı Oluşturma

```sql
CREATE DATABASE payment_db;
application.properties Örneği
spring.datasource.url=jdbc:postgresql://localhost:5432/payment_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
Not: Kullanıcı adı ve şifre kendi lokal ortamınıza göre güncellenmelidir.

Projeyi Çalıştırma


Proje kök dizininde aşağıdaki komut çalıştırılır:

mvn spring-boot:run
Uygulama varsayılan olarak şu adreste çalışır:

http://localhost:8080

Postman Collection

Bu proje, Payment API uç noktalarını test etmek için hazırlanmış bir Postman Collection ve Environment içerir.

### Import Adımları

1. Postman uygulamasını açın
2. Import → File seçeneğini kullanın
3. `postman/Payment_API.postman_collection.json` dosyasını import edin
4. `postman/Payment_API.postman_environment.json` dosyasını import edin
5. Environment olarak `local` seçin
6. Collection Runner ile End-to-End akışı çalıştırın
