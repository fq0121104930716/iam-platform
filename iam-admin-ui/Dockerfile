# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app
COPY iam-admin-ui/package.json iam-admin-ui/package-lock.json* ./
RUN npm config set registry https://registry.npmmirror.com && npm install
COPY iam-admin-ui/ .
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY iam-admin-ui/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
