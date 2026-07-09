#!/usr/bin/env bash
###############################################################################
# AI 3D Studio Mobile - Automated Build Script
# يعمل على Linux, macOS, و WSL
###############################################################################
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${BLUE}[معلومة]${NC} $1"; }
log_ok()    { echo -e "${GREEN}[نجاح]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[تحذير]${NC} $1"; }
log_error() { echo -e "${RED}[خطأ]${NC} $1"; }

fail() {
    log_error "$1"
    log_error "تم إيقاف عملية البناء."
    exit 1
}

echo "==============================================================="
echo "   AI 3D Studio Mobile — Automated APK Build"
echo "==============================================================="

# 1. التحقق من Java
log_info "التحقق من وجود Java 17 أو أحدث..."
if ! command -v java >/dev/null 2>&1; then
    fail "لم يتم العثور على Java. الرجاء تثبيت JDK 17 أو أحدث (على سبيل المثال: https://adoptium.net)."
fi
JAVA_VERSION=$(java -version 2>&1 | head -1 | grep -oE '"[0-9]+' | tr -d '"')
if [ -z "$JAVA_VERSION" ] || [ "$JAVA_VERSION" -lt 17 ]; then
    fail "إصدار Java المكتشف ($JAVA_VERSION) أقل من 17. الرجاء تثبيت JDK 17 أو أحدث."
fi
log_ok "Java $JAVA_VERSION متوفر."

# 2. التحقق من Android SDK
log_info "التحقق من وجود Android SDK..."
if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
    log_warn "المتغيران ANDROID_SDK_ROOT و ANDROID_HOME غير معرّفين."
    log_warn "سيحاول Gradle استخدام ملف local.properties إن وُجد، أو تنزيل مكونات SDK تلقائياً حسب إعداد بيئتك."
else
    SDK_PATH="${ANDROID_SDK_ROOT:-$ANDROID_HOME}"
    if [ -d "$SDK_PATH" ]; then
        log_ok "تم العثور على Android SDK في: $SDK_PATH"
    else
        log_warn "المسار المُعرَّف لـ Android SDK غير موجود فعلياً: $SDK_PATH"
    fi
fi

# 3. التحقق من Gradle Wrapper
log_info "التحقق من وجود Gradle Wrapper..."
[ -f "$SCRIPT_DIR/gradlew" ] || fail "لم يتم العثور على ملف gradlew في جذر المشروع."
[ -f "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar" ] || fail "لم يتم العثور على gradle-wrapper.jar."
[ -f "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.properties" ] || fail "لم يتم العثور على gradle-wrapper.properties."
log_ok "ملفات Gradle Wrapper موجودة وسليمة."

# 4. التحقق من سلامة ملفات المشروع الأساسية
log_info "التحقق من سلامة ملفات المشروع..."
REQUIRED_FILES=(
    "settings.gradle.kts"
    "build.gradle.kts"
    "app/build.gradle.kts"
    "app/src/main/AndroidManifest.xml"
    "gradle/libs.versions.toml"
)
for f in "${REQUIRED_FILES[@]}"; do
    [ -f "$SCRIPT_DIR/$f" ] || fail "ملف مطلوب مفقود: $f"
done
log_ok "جميع ملفات المشروع الأساسية موجودة."

# 5. منح صلاحيات التنفيذ
log_info "منح صلاحيات التنفيذ لملفات gradlew..."
chmod +x "$SCRIPT_DIR/gradlew" || fail "تعذر منح صلاحيات التنفيذ لـ gradlew."
log_ok "تم منح الصلاحيات."

# 6. تنظيف المشروع
log_info "تنظيف المشروع (clean)..."
"$SCRIPT_DIR/gradlew" clean --console=plain || fail "فشلت خطوة gradlew clean."
log_ok "تم تنظيف المشروع بنجاح."

# 7. بناء نسخة Debug (يقوم Gradle تلقائياً بتنزيل الاعتماديات المطلوبة)
log_info "تنفيذ عملية البناء الكاملة (assembleDebug)..."
"$SCRIPT_DIR/gradlew" assembleDebug --console=plain || fail "فشل بناء نسخة Debug من APK."
log_ok "تم بناء نسخة Debug بنجاح."

DEBUG_APK="$SCRIPT_DIR/app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$DEBUG_APK" ]; then
    log_ok "ملف APK (Debug) جاهز في: $DEBUG_APK"
else
    fail "لم يتم العثور على ملف APK الناتج بعد اكتمال البناء."
fi

# 8. بناء نسخة Release إن كانت إعدادات التوقيع متوفرة
if [ -n "$RELEASE_STORE_FILE" ] && [ -n "$RELEASE_STORE_PASSWORD" ] && [ -n "$RELEASE_KEY_ALIAS" ] && [ -n "$RELEASE_KEY_PASSWORD" ]; then
    log_info "تم اكتشاف إعدادات توقيع Release، جارِ بناء نسخة Release..."
    "$SCRIPT_DIR/gradlew" assembleRelease --console=plain || fail "فشل بناء نسخة Release من APK."
    RELEASE_APK="$SCRIPT_DIR/app/build/outputs/apk/release/app-release.apk"
    if [ -f "$RELEASE_APK" ]; then
        log_ok "ملف APK (Release) جاهز في: $RELEASE_APK"
    else
        log_warn "لم يتم العثور على ملف APK الخاص بالإصدار Release رغم نجاح البناء."
    fi
else
    log_warn "لم يتم تعريف متغيرات توقيع Release (RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD)."
    log_warn "تم تخطي بناء نسخة Release. لبنائها لاحقاً، عرّف هذه المتغيرات ثم نفّذ: ./gradlew assembleRelease"
fi

echo "==============================================================="
log_ok "اكتملت عملية البناء بنجاح."
echo "==============================================================="
