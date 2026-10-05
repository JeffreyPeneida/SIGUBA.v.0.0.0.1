# ---- build ---------------------------------------------------------------
FROM node:22-alpine AS build
WORKDIR /app

# .npmrc trae legacy-peer-deps: sin el, `npm ci` falla por el peer de ngx-toastr.
COPY package.json package-lock.json .npmrc ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

# ---- runtime -------------------------------------------------------------
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend-app/browser /usr/share/nginx/html
EXPOSE 80
