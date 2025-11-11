#!/bin/bash
# Init script for Solr core creation with custom config
# This script waits for Solr to be ready, then creates the core with our custom configset

set -e

CORE_NAME=${SOLR_CORE_NAME:-books}
SOLR_HOST=${SOLR_HOST:-localhost}
SOLR_PORT=${SOLR_PORT:-8983}
CONFIG_DIR="/opt/solr/server/solr/configsets/books-config"

# Wait for Solr to be ready
echo "Waiting for Solr to be ready..."
until curl -f http://${SOLR_HOST}:${SOLR_PORT}/solr/admin/ping > /dev/null 2>&1; do
  echo "Solr is not ready yet. Waiting..."
  sleep 2
done

echo "Solr is ready!"

# Check if core already exists
if curl -f http://${SOLR_HOST}:${SOLR_PORT}/solr/admin/cores?action=STATUS&core=${CORE_NAME} > /dev/null 2>&1; then
  echo "Core '${CORE_NAME}' already exists. Skipping creation."
  exit 0
fi

# Create core with custom config
echo "Creating core '${CORE_NAME}'..."
curl "http://${SOLR_HOST}:${SOLR_PORT}/solr/admin/cores?action=CREATE&name=${CORE_NAME}&configSet=_default" || {
  echo "Failed to create core with default config. Trying alternative method..."
  # Alternative: create core directory and copy config
  mkdir -p /var/solr/data/${CORE_NAME}/conf
  # Config should be mounted from host
  if [ -d "/opt/solr-config" ]; then
    cp -r /opt/solr-config/* /var/solr/data/${CORE_NAME}/conf/
    curl "http://${SOLR_HOST}:${SOLR_PORT}/solr/admin/cores?action=CREATE&name=${CORE_NAME}&instanceDir=/var/solr/data/${CORE_NAME}"
  else
    echo "Config directory not found. Using default config."
    curl "http://${SOLR_HOST}:${SOLR_PORT}/solr/admin/cores?action=CREATE&name=${CORE_NAME}"
  fi
}

echo "Core '${CORE_NAME}' created successfully!"

