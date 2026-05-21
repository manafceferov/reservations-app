Stavia — Hotel & Restaurant Platform

Həm REST API endpoint-ləri, həm də MVC (Thymeleaf) frontend hissəsi olan otel otağı və restoran masası rezervasiyası layihəsi.

Funksionallıq
* Qeydiyyat və giriş — Spring Security və JWT token ilə
* Otaq rezervasiyası — otaqların statusuna və tarixlərə görə listələnməsi və rezerv edilməsi
* Masa rezervasiyası — tarixlərə, saatlara və masanın yerinə (Terras, VIP, İçəri sal) görə dinamik yoxlanılması və rezerv edilməsi
* Ön sifariş (Pre-order) — masa rezervasiyası zamanı menyudan yeməklərin seçilərək sifarişə əlavə edilməsi
* Rezervasiya detalları — Bootstrap Modal pəncərəsi ilə səhifə yenilənmədən vaxt, qonaq sayısı, qeydlər və sifariş edilən yeməklərin göstərilməsi

Texnologiyalar
* Java 21 — Controller, Service və Repository təbəqələri üçün
* Kotlin — Entity və DTO-ların yazılması üçün
* Spring Boot 3.x — layihənin əsas framework-ü
* Spring MVC + Thymeleaf — tünd (dark) və işıqlı rejim dəstəkli frontend interfeysi
* Spring Security + JWT — istifadəçi seanslarının təhlükəsizliyi və avtorizasiya
* PostgreSQL — əsas əlaqəli məlumat bazası
* Liquibase — verilənlər bazası miqrasiyalarının və cədvəllərin idarə edilməsi
* MapStruct — Entity-lər və DTO-lar arası mapping prosesi üçün
* Docker — PostgreSQL bazasını konteyner daxilində ayağa qaldırmaq üçün
* Swagger UI — REST API endpoint-lərinin sənədləşdirilməsi və test edilməsi

Paket Strukturu
* `config/` — Təhlükəsizlik və Swagger konfiqurasiyaları
* `controller/` — REST API (`@RestController`) və MVC (`@Controller`) sinifləri
* `dto/` — Məlumat daşıyan Kotlin DTO sinifləri
* `entity/` — Verilənlər bazası cədvəllərini təmsil edən Kotlin entity sinifləri
* `enums/` — Rezervasiya statusları və istifadəçi rolları
* `mapper/` — MapStruct interfeysləri
* `repository/` — Spring Data JPA verilənlər bazası layı
* `service/` — Əsas biznes məntiqinin icra olunduğu lay
