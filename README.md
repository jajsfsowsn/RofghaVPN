# 🛡️ Rofgha VPN - اپلیکیشن VPN اندروید

## ✨ ویژگی‌ها

- 🔐 **VLESS + Reality** - پشتیبانی از پروتکل‌های امن
- 🌐 **تونل کل گوشی** - تمام ترافیک از VPN رد میشه
- 🎨 **رابط کاربری ساده** - تم تاریک زیبا
- 🚀 **سرعت بالا** - بدون Padding برای حداکثر سرعت
- 🔒 **امنیت قوی** - رمزنگاری Reality

---

## 📋 پیش‌نیازها

### برای کامپایل:
- **Android Studio** (آخرین نسخه)
- **JDK 17**
- **Android SDK 34**
- **NDK** (برای کامپایل Xray Core)

### برای کامپایل Xray Core:
- **Go** (version 1.21+)
- **Git**

---

## 🚀 مراحل نصب

### مرحله ۱: کامپایل Xray Core

```bash
# رفتن به پوشه پروژه
cd VpnApp

# اجرای اسکریپت کامپایل
chmod +x compile-xray.sh
./compile-xray.sh
```

### مرحله ۲: باز کردن در Android Studio

1. Android Studio رو باز کن
2. File → Open → پوشه VpnApp
3. صبر کن تا Sync تموم بشه

### مرحله ۳: بیلد APK

1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. صبر کن تا بیلد تموم بشه
3. APK در پوشه `app/build/outputs/apk/debug/` ذخیره میشه

### مرحله ۴: نصب روی گوشی

1. APK رو به گوشی منتقل کن
2. نصب کن
3. مجوز VPN رو بده

---

## 📁 ساختار پروژه

```
VpnApp/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/
│       │   └── xray              ← هسته Xray (بعد از کامپایل)
│       ├── java/com/rofgha/vpn/
│       │   ├── MainActivity.kt      ← صفحه اصلی
│       │   ├── VpnService.kt        ← سرویس VPN
│       │   ├── VlessParser.kt       ← پارسر کانفیگ VLESS
│       │   ├── XrayCore.kt          ← مدیریت هسته Xray
│       │   └── XrayConfigGenerator.kt ← ساخت تنظیمات Xray
│       └── res/
│           ├── layout/
│           │   └── activity_main.xml
│           ├── values/
│           │   ├── colors.xml
│           │   ├── strings.xml
│           │   └── themes.xml
│           └── drawable/
├── compile-xray.sh              ← اسکریپت کامپایل Xray
├── build.gradle
├── settings.gradle
└── README.md
```

---

## 🔧 استفاده

### ۱. وارد کردن کانفیگ

کانفیگ VLESS رو در فرمت زیر وارد کن:

```
vless://UUID@SERVER:PORT?security=reality&sni=www.samsung.com&fp=chrome&pbk=PUBLIC_KEY&sid=SHORT_ID&spx=SPIDER_X&type=xhttp&path=PATH&host=HOST#NAME
```

### ۲. اتصال

روی دکمه **"اتصال"** کلیک کن

### ۳. قطع اتصال

روی دکمه **"قطع اتصال"** کلیک کن

---

## 📝 نکات مهم

### ۱. هسته Xray
- باید جداگانه کامپایل بشه
- فایل `xray` در پوشه `assets/` قرار بگیره
- حجم تقریبی: ~۱۰-۱۵ MB

### ۲. مجوز VPN
- اولین بار که اپ رو اجرا می‌کنی، ازت مجوز VPN میخواد
- باید تأیید کنی

### ۳. سرویس Foreground
- اپ یه نوتیفیکیشن نشون میده که VPN فعاله
- این باعث میشه اتصال قطع نشه

---

## 🐛 عیب‌یابی

### مشکل: اپ کرش می‌کنه
- **راه‌حل:** مطمئن شو Xray Core کامپایل شده

### مشکل: اتصال برقرار نمیشه
- **راه‌حل:** کانفیگ رو چک کن
- **راه‌حل:** پورت 10808 آزاد باشه

### مشکل: سرعت کمه
- **راه‌حل:** Padding رو `0-0` بذار

---

## 📄 لایسنس

MIT License

## 🤘 سازنده

**Rofgha** - @Rofgha12
