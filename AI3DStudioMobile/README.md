# AI 3D Studio Mobile

تطبيق أندرويد احترافي لإنشاء الصور والنماذج ثلاثية الأبعاد والأصول الرقمية والألعاب باستخدام الذكاء الاصطناعي، من خلال إدخال عنوان خادم واحد فقط (Base URL). يقوم التطبيق تلقائياً باكتشاف نماذج الخادم وقدراته (محادثة، صور، نماذج 3D، ألعاب) دون أي إعداد تقني إضافي من المستخدم.

## نظرة عامة

- **الإدخال الوحيد المطلوب من المستخدم:** Base URL (مثال: `https://example.com/api`).
- **الاكتشاف التلقائي:** النماذج المتاحة، قدرات الصور/الدردشة/التوليد ثلاثي الأبعاد/الألعاب، حدود الطلبات، معلومات الخادم.
- **بدون:** مفاتيح API إضافية، معرّفات نماذج يدوية، أو إعدادات تقنية معقدة.

## المتطلبات

- **Android Studio** Jellyfish أو أحدث.
- **JDK 17** أو أحدث.
- **Android SDK** مع `compileSdk 35` و `minSdk 29`.
- اتصال بالإنترنت لتنزيل الاعتماديات عبر Gradle (Google/Maven Central).

## طريقة التثبيت والفتح في Android Studio

1. فك ضغط ملف المشروع (ZIP).
2. افتح Android Studio ← `Open` ← اختر مجلد `AI3DStudioMobile`.
3. انتظر حتى تنتهي مزامنة Gradle تلقائياً (يتم تنزيل الاعتماديات من الإنترنت).
4. لا حاجة لأي تعديل يدوي إضافي — المشروع جاهز للتشغيل والبناء مباشرة.

## طريقة البناء عبر سطر الأوامر

```bash
./gradlew clean
./gradlew assembleDebug
```

سينتج ملف APK في:

```
app/build/outputs/apk/debug/app-debug.apk
```

لإصدار Release (يتطلب توقيع، راجع قسم التوقيع أدناه):

```bash
./gradlew assembleRelease
```

سينتج الملف في:

```
app/build/outputs/apk/release/app-release.apk
```

## تشغيل سكربت البناء التلقائي

```bash
chmod +x build_apk.sh
./build_apk.sh
```

يقوم السكربت تلقائياً بـ:

- التحقق من Java 17+، Android SDK، و Gradle Wrapper.
- منح صلاحيات التنفيذ للملفات المطلوبة.
- تنظيف المشروع (`clean`).
- تنزيل الاعتماديات وتنفيذ `assembleDebug` (و `assembleRelease` إن توفرت إعدادات التوقيع).
- طباعة المسار النهائي لملف APK الناتج.

## توقيع نسخة Release (اختياري)

عرّف متغيرات البيئة التالية قبل تشغيل `assembleRelease`:

```bash
export RELEASE_STORE_FILE=/path/to/keystore.jks
export RELEASE_STORE_PASSWORD=********
export RELEASE_KEY_ALIAS=your_alias
export RELEASE_KEY_PASSWORD=********
```

إن لم تُعرَّف، يستخدم Gradle توقيع Debug تلقائياً حتى لا يفشل البناء.

## بنية المشروع

```
AI3DStudioMobile/
├── app/
│   ├── src/main/java/com/ai3dstudio/mobile/
│   │   ├── core/
│   │   │   ├── data/        # Room DAOs/Entities/Repositories (Offline-first)
│   │   │   ├── di/          # وحدات Hilt (Network, Database, Repository)
│   │   │   ├── domain/      # النماذج، الواجهات، Use Cases
│   │   │   ├── network/     # Retrofit + اكتشاف القدرات التلقائي
│   │   │   ├── security/    # Keystore, EncryptedSharedPreferences, Rate Limiting
│   │   │   └── util/        # تخزين الملفات، خدمة التصدير، حزم الألعاب
│   │   ├── feature/
│   │   │   ├── setup/       # شاشة إدخال Base URL والاكتشاف التلقائي
│   │   │   ├── chat/        # AI Chat Studio
│   │   │   ├── image/       # AI Image Generation
│   │   │   ├── model3d/     # AI 3D Generation + عارض Filament
│   │   │   ├── game/        # Game Generator وتصدير game.zip
│   │   │   ├── projects/    # إدارة المشاريع
│   │   │   ├── assets/      # مكتبة الأصول
│   │   │   └── settings/    # الإعدادات، المظهر، الأمان
│   │   ├── navigation/      # Navigation Compose
│   │   └── ui/theme/        # Material 3 (فاتح/داكن)
│   ├── src/test/            # اختبارات وحدة (JUnit)
│   └── src/androidTest/     # اختبارات مُدارة (Compose UI Test)
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── build_apk.sh
└── .github/workflows/build-apk.yml
```

## التقنيات المستخدمة

- **اللغة:** Kotlin.
- **الواجهة:** Jetpack Compose + Material 3 (فاتح/داكن، Responsive، دعم الأجهزة القابلة للطي والأجهزة اللوحية).
- **العرض ثلاثي الأبعاد:** Google Filament (PBR, IBL, Real-Time Shadows, Bloom, Tone Mapping, Anti-Aliasing) + OpenGL ES 3.2 / Vulkan عبر Filament backend.
- **البنية:** Clean Architecture + MVVM + Repository Pattern + Use Cases + Hilt (Dependency Injection).
- **قاعدة البيانات:** Room (Offline-First) + Repository Pattern + طبقة Caching.
- **الشبكة:** Retrofit + OkHttp + Kotlinx Serialization.
- **التخزين:** Proto/Preferences DataStore، EncryptedSharedPreferences، Android Keystore.
- **الأمان:** Certificate Pinning ديناميكي (TOFU)، تحقق من صحة الطلبات، Rate Limiting على مستوى العميل.
- **الأداء:** Kotlin Coroutines + Flow، WorkManager، Memory Mapping للأصول الكبيرة، Foreground Service للمعالجة الطويلة.
- **الوسائط:** Coil (صور)، Media3 (صوت/فيديو).
- **الذكاء الاصطناعي على الجهاز:** ML Kit، TensorFlow Lite Support (تصنيف وتحليل مساعد اختياري).

## كيف يعمل الاكتشاف التلقائي

عند إدخال Base URL، يقوم `CapabilityDiscoveryService` باستدعاء مجموعة من مسارات الاكتشاف المعروفة (`/models`, `/capabilities`, `/health`, `/limits`, إلخ) على الخادم المُدخل، ويدمج أي استجابة يعيدها الخادم في بنية موحدة (`CapabilityManifest`) تحدد النماذج المتاحة وقدرات الدردشة/الصور/النماذج ثلاثية الأبعاد/الألعاب وحدود الطلبات. أي حقل غير متوفر من الخادم يُعامل بأمان دون كسر التطبيق.

## ملاحظة حول الخادم

هذا التطبيق **عميل عام (Generic Client)** يتوافق مع أي خادم يعرض نقاط نهاية بأسلوب متوافق مع OpenAI/عام (`/chat/completions`, `/images/generations`, إلخ) عبر Base URL واحد. لا يتضمن مفاتيح API لأي مزود خدمة ذكاء اصطناعي؛ المستخدم يوفر خادمه الخاص الذي يتولى الاتصال الفعلي بمزود الذكاء الاصطناعي (مثل خدمات Google AI أو أي بنية تحتية أخرى يديرها المستخدم).

## الترخيص

هذا المشروع مُسلَّم كقاعدة إنتاجية جاهزة للتخصيص من قبل المستخدم/الفريق المالك.
