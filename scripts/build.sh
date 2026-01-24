#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> Backend build"
cd "$ROOT_DIR/backend"
./mvnw -DskipTests package

echo "==> Frontend build"
cd "$ROOT_DIR/frontend"
npm run build
