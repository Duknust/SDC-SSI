#!/bin/bash

openssl pkcs8 -topk8 -inform PEM -outform DER -in $1 -out $2 -nocrypt
