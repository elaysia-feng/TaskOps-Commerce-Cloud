# TLS 证书说明

将证书放到本目录:

- `server.crt`
- `server.key`

本地开发可用 OpenSSL 自签:

```bash
openssl req -x509 -nodes -days 3650 -newkey rsa:2048 \
  -keyout server.key \
  -out server.crt \
  -subj "/C=CN/ST=Local/L=Local/O=TaskOps/OU=Dev/CN=localhost"
```

