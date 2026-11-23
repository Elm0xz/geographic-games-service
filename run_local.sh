#!/usr/bin/env bash
set -euo pipefail

trap 'docker compose down -v' EXIT

docker compose up -d --wait
./gradlew bootRun --args='--spring.profiles.active=local'