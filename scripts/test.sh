#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> Backend tests"
cd "$ROOT_DIR/backend"
./mvnw test

echo "==> Frontend tests"
cd "$ROOT_DIR/frontend"
npm run test -- --watch=false --browsers=ChromeHeadless
