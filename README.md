# Bad Habit Tracker 🚫

**Bad Habit Tracker**, kötü alışkanlıkları bırakmayı oyunlaştıran (Gamification), modern ve kullanıcı dostu bir Android uygulamasıdır. Kullanıcılar, alışkanlıklarını takip edebilir, ilerlemelerini kaydedebilir ve arkadaşlarıyla rekabet ederek motivasyonlarını artırabilirler.

## 🌟 Özellikler

*   **Oyunlaştırma (Gamification):**
    *   **Can Sistemi:** Her alışkanlık için 3 can. Hata yapıldığında can azalır, belirli süre temiz kalındığında can yenilenir.
    *   **XP ve Rütbeler:** Temiz kalınan her saat için XP kazanılır. Acemi'den Efsane'ye uzanan rütbe sistemi.
    *   **Görsel İlerleme:** Dairesel ve bölmeli ilerleme çubukları ile anlık durum takibi.
*   **Modern Arayüz (UI/UX):**
    *   **Jetpack Compose:** Tamamen deklaratif UI yapısı.
    *   **Dark/Light Mode:** Dinamik tema desteği.
    *   **Lottie Animasyonları:** Başarı, başarısızlık ve boş liste durumları için etkileyici animasyonlar.
*   **Sosyal Etkileşim:**
    *   Arkadaş ekleme ve takibi.
    *   Arkadaşların ilerlemesini görüntüleme.
*   **Bildirimler:**
    *   **WorkManager:** Günlük hatırlatıcılar (Uygulama kapalıyken bile çalışır).
    *   **Kişiselleştirme:** Kullanıcı bildirim saatini ve motivasyon mesajını kendisi seçebilir.
*   **Profil Yönetimi:**
    *   Avatar seçimi (Karikatür ikonları).
    *   Kullanıcı adı ve e-posta yönetimi.
*   **Güvenli Giriş:**
    *   Firebase Authentication (E-posta/Şifre ve Google ile Giriş).

## 🛠️ Kullanılan Teknolojiler

*   **Dil:** Kotlin
*   **UI Framework:** Jetpack Compose (Material3)
*   **Mimari:** MVVM (Model-View-ViewModel)
*   **Asenkron İşlemler:** Coroutines & Flow
*   **Backend:** Firebase (Firestore Database, Authentication)
*   **Arka Plan İşlemleri:** WorkManager
*   **Navigasyon:** Navigation Compose
*   **Animasyon:** LottieFiles
*   **Diğer:** Hilt (Dependency Injection - Opsiyonel), Coil (Resim Yükleme - Opsiyonel)

## 📸 Ekran Görüntüleri

| Ana Sayfa | Detay & Takvim | Profil & Ayarlar | Arkadaşlar |
|:---:|:---:|:---:|:---:|
| ![Home](screenshots/home.png) | ![Detail](screenshots/detail.png) | ![Profile](screenshots/profile.png) | ![Friends](screenshots/friends.png) |

*(Not: Ekran görüntülerini `screenshots` klasörüne eklemeyi unutmayın)*

## 🚀 Kurulum

Bu projeyi yerel makinenizde çalıştırmak için:

1.  **Repository'yi Klonlayın:**
    ```bash
    git clone https://github.com/KULLANICI_ADINIZ/BadHabitTracker.git
    ```
2.  **Firebase Kurulumu:**
    *   [Firebase Konsolu](https://console.firebase.google.com/)'nda yeni bir proje oluşturun.
    *   Android uygulamasını ekleyin (Paket adı: `com.hasan.badhabit`).
    *   `google-services.json` dosyasını indirin ve projenin `app/` klasörüne yapıştırın.
    *   Firebase Authentication'da "Email/Password" ve "Google" sağlayıcılarını etkinleştirin.
    *   Firestore Database'i oluşturun.
3.  **Projeyi Çalıştırın:**
    *   Android Studio'da projeyi açın.
    *   Gradle senkronizasyonunu yapın.
    *   Uygulamayı emülatörde veya fiziksel cihazda çalıştırın.

## 📄 Lisans

Bu proje [MIT Lisansı](LICENSE) altında lisanslanmıştır.

---
**Geliştirici:** Hasan Ören
**İletişim:** [hasanorentr@gmail.com]
