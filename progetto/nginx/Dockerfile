FROM node:22-alpine AS frontend-build
WORKDIR /app
RUN corepack enable && corepack prepare pnpm@latest --activate
COPY web/package.json web/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY web/ .
RUN pnpm build

FROM nginx:1.27-alpine
COPY --from=frontend-build /app/build /usr/share/nginx/html
EXPOSE 80 443
