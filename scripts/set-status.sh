#!/bin/bash
#
# Script to set errand status via oep-integrator API.
#
# Usage: ./set-status.sh [-x] [-t <ms>] <csv-file>
#
# CSV format (with header): id,"created","touched","channel","value"
#   - channel: ESERVICE_INTERNAL -> instanceType INTERNAL, ESERVICE -> instanceType EXTERNAL
#   - value: The flowInstanceId of the errand
#
# By default the script runs in dry-run mode (no actual calls are made).
# Use -x to execute for real.
#
# Options:
#   -x          Execute for real (default is dry-run)
#   -t <ms>     Throttle: sleep <ms> milliseconds between PUT calls
#
# Example:
#   ./set-status.sh errands.csv             # dry-run
#   ./set-status.sh -x errands.csv          # real run
#   ./set-status.sh -x -t 500 errands.csv   # real run, 500ms between calls
#
# Before running, edit the constants below:
#   TOKEN_URL, CLIENT_ID, CLIENT_SECRET, BASE_URL, MUNICIPALITY_ID, STATUS
#

set -euo pipefail

# --- Edit these constants before running ---
TOKEN_URL="token-url"
CLIENT_ID="client-id"
CLIENT_SECRET="client-secret"
BASE_URL="base-url"
MUNICIPALITY_ID="2281"
STATUS="Klart"
# -------------------------------------------

fatal() {
  echo "FATAL: $1" >&2
  [[ -n "${LOG_FILE:-}" ]] && echo "[$(date +"%Y-%m-%d %H:%M:%S")] FATAL: $1" >> "$LOG_FILE"
  exit 1
}

log() {
  local msg="[$(date +"%Y-%m-%d %H:%M:%S")] $1"
  echo "$msg"
  echo "$msg" >> "$LOG_FILE"
}

json_string() { sed -n 's/.*"'"$2"'":"\([^"]*\)".*/\1/p' <<< "$1"; }
json_number() { sed -n 's/.*"'"$2"'":\([0-9]*\).*/\1/p' <<< "$1"; }

parse_args() {
  DRY_RUN=true
  THROTTLE_MS=""
  while [ $# -gt 0 ]; do
    case "$1" in
      -x) DRY_RUN=false; shift ;;
      -t) THROTTLE_MS="$2"; shift 2 ;;
      -*) echo "Unknown option: $1"; exit 1 ;;
      *)  break ;;
    esac
  done

  if [ $# -lt 1 ]; then
    echo "Usage: $0 [-x] [-t <ms>] <csv-file>"
    echo ""
    echo "CSV format (with header): id,\"created\",\"touched\",\"channel\",\"value\""
    echo "  channel: ESERVICE_INTERNAL or ESERVICE"
    echo "  value: flowInstanceId"
    echo ""
    echo "Options:"
    echo "  -x          Execute for real (default is dry-run)"
    echo "  -t <ms>     Throttle: sleep <ms> milliseconds between PUT calls"
    exit 1
  fi

  CSV_FILE="$1"
  BASE_URL="${BASE_URL%/}"

  if [ ! -f "$CSV_FILE" ]; then
    echo "ERROR: File '$CSV_FILE' not found."
    exit 1
  fi
}

ACCESS_TOKEN=""
TOKEN_EXPIRES_AT=0

ensure_token() {
  if [[ -n "${ACCESS_TOKEN}" ]] && (( $(date +%s) < TOKEN_EXPIRES_AT )); then
    return
  fi
  fetch_token
}

fetch_token() {
  local resp http_code expires_in
  resp="$(curl -sS -w '\n%{http_code}' \
    -u "${CLIENT_ID}:${CLIENT_SECRET}" \
    --data-urlencode "grant_type=client_credentials" \
    "${TOKEN_URL}" || true)"
  http_code="${resp##*$'\n'}"
  resp="${resp%$'\n'*}"
  [[ "${http_code}" =~ ^2 ]] \
    || fatal "Token request failed: HTTP ${http_code} ${resp:-<no response>}"

  ACCESS_TOKEN="$(json_string "${resp}" access_token)"
  expires_in="$(json_number "${resp}" expires_in)"
  [[ -n "${expires_in}" ]] || expires_in=3600
  [[ -n "${ACCESS_TOKEN}" ]] || fatal "No access_token in token response: ${resp}"
  TOKEN_EXPIRES_AT=$(( $(date +%s) + 10#${expires_in} - 30 ))
  log "Obtained access token (expires in ${expires_in}s)"
}

init_logging() {
  LOG_DIR="logs"
  mkdir -p "$LOG_DIR"

  local timestamp
  timestamp=$(date +"%Y%m%d_%H%M%S")

  if [ "$DRY_RUN" = true ]; then
    LOG_FILE="${LOG_DIR}/set-status_dry-run_${timestamp}.log"
  else
    LOG_FILE="${LOG_DIR}/set-status_${timestamp}.log"
  fi

  log "Starting $([ "$DRY_RUN" = true ] && echo "DRY-RUN" || echo "LIVE RUN")"
  log "CSV file: $CSV_FILE"
  log "Base URL: $BASE_URL"
  log "Municipality ID: $MUNICIPALITY_ID"
  [[ -n "$THROTTLE_MS" ]] && log "Throttle: ${THROTTLE_MS}ms between calls"
}

resolve_instance_type() {
  local channel="$1"
  case "$channel" in
    ESERVICE_INTERNAL) echo "INTERNAL" ;;
    ESERVICE)          echo "EXTERNAL" ;;
    *)                 echo "" ;;
  esac
}

set_status() {
  local url="$1"

  if [ "$DRY_RUN" = true ]; then
    log "[DRY-RUN] PUT $url -d '{\"name\": \"${STATUS}\"}'"
    return 0
  fi

  ensure_token

  local http_code
  http_code=$(curl -s -o /dev/null -w "%{http_code}" \
    -X PUT "$url" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    -d "{\"name\": \"${STATUS}\"}")

  if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
    log "OK ($http_code) PUT $url"
    return 0
  else
    log "FAILED ($http_code) PUT $url"
    return 1
  fi
}

process_csv() {
  local success_count=0
  local fail_count=0
  local skip_count=0
  local line_num=0

  while IFS=',' read -r _id _created _touched channel flowInstanceId; do
    line_num=$((line_num + 1))

    if [ "$line_num" -eq 1 ]; then
      continue
    fi

    flowInstanceId=$(echo "$flowInstanceId" | tr -d '[:space:]"')
    channel=$(echo "$channel" | tr -d '[:space:]"')

    local instance_type
    instance_type=$(resolve_instance_type "$channel")

    if [ -z "$instance_type" ]; then
      log "SKIP line $line_num: Unknown channel '$channel' for flowInstanceId '$flowInstanceId'"
      skip_count=$((skip_count + 1))
      continue
    fi

    local url="${BASE_URL}/${MUNICIPALITY_ID}/${instance_type}/cases/${flowInstanceId}/status"

    if set_status "$url"; then
      success_count=$((success_count + 1))
    else
      fail_count=$((fail_count + 1))
    fi

    if [[ -n "$THROTTLE_MS" ]]; then
      sleep "$(awk "BEGIN {printf \"%.3f\", $THROTTLE_MS / 1000}")"
    fi

  done < "$CSV_FILE"

  log "Done. Success: $success_count, Failed: $fail_count, Skipped: $skip_count"
  echo "Log file: $LOG_FILE"
}

main() {
  parse_args "$@"
  init_logging
  process_csv
}

main "$@"