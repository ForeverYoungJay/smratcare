FROM node:20-alpine AS builder
WORKDIR /app/admin-web
ARG NODE_OPTIONS=--max-old-space-size=4096
ENV NODE_OPTIONS=$NODE_OPTIONS

COPY admin-web/package*.json ./
RUN npm ci

COPY admin-web .
COPY src/main/resources/security /app/src/main/resources/security
RUN npm run build

FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

COPY --from=builder /app/admin-web/dist ./
COPY admin-web/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
