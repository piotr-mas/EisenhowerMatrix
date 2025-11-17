#!/bin/bash
# Set -e ensures the script exits immediately if any command fails
set -e

# --- Load Environment Variables from Parent Directory ---
ENV_FILE="../.env"
if [ -f "$ENV_FILE" ]; then
    # Loads variables (passwords) from the .env file in the project root
    source "$ENV_FILE"
    echo "Environment variables loaded from $ENV_FILE"
else
    echo "Error: $ENV_FILE not found! Passwords for keystores will be missing."
    exit 1
fi

# --- CONFIGURATION (Uses variables from your .env file) ---
echo "Starting certificate generation..."
SSL_DIR="./"
CA_CN="MatrixCA"

# Hostnames for the services (from application.yml, Matrix.odt, and Docker)
GATEWAY_SERVER_CN="ms-gateway"
GATEWAY_SERVER_SANS="DNS.1=localhost,DNS.2=ms-gateway"

MICROSERVICE_SERVER_CN="microservice-cluster"

# Includes all 5 Microservices (User, Auth, Matrix, Notification, Task)
MICROSERVICE_SERVER_SANS="\
DNS.1=user-service-host,\
DNS.2=auth-service-host,\
DNS.3=matrix-service-host,\
DNS.4=notification-service-host,\
DNS.5=task-service-host,\
DNS.6=ms-user,\
DNS.7=ms-auth,\
DNS.8=ms-matrix,\
DNS.9=ms-notification,\
DNS.10=ms-task"

# --- 1. CLEANUP (Initial Wipe) ---
echo "1. Cleanup old files..."
rm -f ca.* *.key *.csr *.crt *.cnf *.p12 *.jks *.srl

# --- 2. CERTIFICATE AUTHORITY (CA) ---
echo "2. Generating CA key and certificate..."
openssl genrsa -out ca.key 2048
MSYS_NO_PATHCONV=1 openssl req -x509 -new -nodes -key ca.key -sha256 -days 3650 \
  -out ca.crt -subj "/CN=$CA_CN"

# --- 3. TRUSTSTORES (Import CA into all required truststores) ---
echo "3. Generating Truststores..."

# Gateway Truststore (trusts all microservices)
keytool -import -trustcacerts -alias matrixca -file ca.crt \
  -keystore gateway-truststore.jks -storepass $GATEWAY_TRUST_PASSWORD -noprompt

# Microservice Truststore (trusts Gateway)
keytool -import -trustcacerts -alias matrixca -file ca.crt \
  -keystore user-truststore.jks -storepass $TRUSTSTORE_PASSWORD -noprompt


# --- 4. GATEWAY SERVER/CLIENT CERTIFICATE ---
echo "4. Generating Gateway Certificate and Keystore (gateway-keystore.p12)..."
GATEWAY_PREFIX="gateway"

# 4a. Create SAN config for Gateway
cat > $GATEWAY_PREFIX.cnf <<EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
CN = $GATEWAY_SERVER_CN

[v3_req]
subjectAltName = @alt_names

[alt_names]
$GATEWAY_SERVER_SANS
EOF

# 4b. Generate Key, CSR, and Sign Certificate (with SAN fix)
openssl genrsa -out $GATEWAY_PREFIX.key 2048
openssl req -new -key $GATEWAY_PREFIX.key -out $GATEWAY_PREFIX.csr -config $GATEWAY_PREFIX.cnf
openssl x509 -req -in $GATEWAY_PREFIX.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
  -out $GATEWAY_PREFIX.crt -days 365 -sha256 -extfile $GATEWAY_PREFIX.cnf -extensions v3_req -copy_extensions copyall

# 4c. Create Keystore
cat $GATEWAY_PREFIX.crt ca.crt > $GATEWAY_PREFIX-fullchain.crt
openssl pkcs12 -export -in $GATEWAY_PREFIX-fullchain.crt -inkey $GATEWAY_PREFIX.key \
  -out gateway-keystore.p12 -name $GATEWAY_PREFIX -password pass:$GATEWAY_STORE_PASSWORD


# --- 5. MICROSERVICE SERVER CERTIFICATE (For ALL Microservices) ---
echo "5. Generating Microservice Certificate and Keystore (user-keystore.p12)..."
MICRO_PREFIX="microservice"

# 5a. Create SAN config for Microservices
cat > $MICRO_PREFIX.cnf <<EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
CN = $MICROSERVICE_SERVER_CN

[v3_req]
subjectAltName = @alt_names

[alt_names]
$MICROSERVICE_SERVER_SANS
EOF

# 5b. Generate Key, CSR, and Sign Certificate (with SAN fix)
openssl genrsa -out $MICRO_PREFIX.key 2048
openssl req -new -key $MICRO_PREFIX.key -out $MICRO_PREFIX.csr -config $MICRO_PREFIX.cnf
openssl x509 -req -in $MICRO_PREFIX.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
  -out $MICRO_PREFIX.crt -days 365 -sha256 -extfile $MICRO_PREFIX.cnf -extensions v3_req -copy_extensions copyall

# 5c. Create Keystore (Shared by all microservices)
cat $MICRO_PREFIX.crt ca.crt > $MICRO_PREFIX-fullchain.crt
openssl pkcs12 -export -in $MICRO_PREFIX-fullchain.crt -inkey $MICRO_PREFIX.key \
  -out user-keystore.p12 -name $MICRO_PREFIX -password pass:$KEYSTORE_PASSWORD

# --- 6. FINAL CLEANUP (Remove Intermediate Files) ---
echo "6. Final cleanup of intermediate files..."
rm -f *.cnf *.csr *-fullchain.crt $GATEWAY_PREFIX.* $MICRO_PREFIX.*

echo "✅ All certificates and keystores generated successfully."