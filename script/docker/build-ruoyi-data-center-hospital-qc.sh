#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./script/docker/build-ruoyi-data-center-hospital-qc.sh [image-tag]

IMAGE_TAG="${1:-2.5.3}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

cd "${ROOT_DIR}"

echo "[1/2] Build ruoyi-data-center jar"
mvn -pl ruoyi-modules/ruoyi-data-center -am -DskipTests clean package

echo "[2/2] Build docker image ruoyi/ruoyi-data-center:${IMAGE_TAG}"
docker build \
  -t "ruoyi/ruoyi-data-center:${IMAGE_TAG}" \
  -f ruoyi-modules/ruoyi-data-center/Dockerfile \
  ruoyi-modules/ruoyi-data-center

echo "Done: ruoyi/ruoyi-data-center:${IMAGE_TAG}"
