#!/bin/bash

# ============================================
# Xray Core Compiler for Android
# ============================================

set -e

echo "🚀 Starting Xray Core compilation for Android..."

# Check prerequisites
if ! command -v go &> /dev/null; then
    echo "❌ Go is not installed. Please install Go first."
    echo "   brew install go (macOS)"
    echo "   sudo apt install golang (Ubuntu)"
    exit 1
fi

# Clone Xray-core
echo "📥 Cloning Xray-core..."
if [ -d "Xray-core" ]; then
    echo "Directory already exists, pulling latest..."
    cd Xray-core && git pull && cd ..
else
    git clone https://github.com/XTLS/Xray-core.git
fi

cd Xray-core

# Build for Android ARM64
echo "🔨 Building for Android ARM64..."
mkdir -p build

# Build using Go
GOOS=android GOARCH=arm64 CGO_ENABLED=1 \
    go build -o ../app/src/main/assets/xray \
    -trimpath \
    -ldflags="-s -w -buildid=" \
    ./main

# Build for Android ARM (32-bit)
echo "🔨 Building for Android ARM..."
GOOS=android GOARCH=arm GOARM=7 CGO_ENABLED=1 \
    go build -o ../app/src/main/assets/xray-arm32 \
    -trimpath \
    -ldflags="-s -w -buildid=" \
    ./main

cd ..

echo "✅ Xray Core compiled successfully!"
echo "📁 Output files:"
ls -lh app/src/main/assets/xray*
echo ""
echo "📋 Next steps:"
echo "1. Copy the binary to your Android project"
echo "2. Build the APK"
echo "3. Test on device"
