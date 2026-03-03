#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   HOSPITAL_QC_AES_SECRET=your-secret \
#   ./script/docker/deploy-ruoyi-data-center-hospital-qc.sh

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

export HOSPITAL_QC_AES_SECRET="${HOSPITAL_QC_AES_SECRET:-change-me-hospital-qc-secret}"
export HOSPITAL_QC_SQL_TIMEOUT_SECONDS="${HOSPITAL_QC_SQL_TIMEOUT_SECONDS:-10}"
export HOSPITAL_QC_SQL_TEST_LIMIT="${HOSPITAL_QC_SQL_TEST_LIMIT:-50}"
export HOSPITAL_QC_DASHBOARD_CACHE_SECONDS="${HOSPITAL_QC_DASHBOARD_CACHE_SECONDS:-300}"
export HOSPITAL_QC_SQLSERVER_TLS_MODE="${HOSPITAL_QC_SQLSERVER_TLS_MODE:-TLS12}"

docker compose -f script/docker/docker-compose.yml up -d ruoyi-data-center

echo "ruoyi-data-center started with hospital-qc env variables."
