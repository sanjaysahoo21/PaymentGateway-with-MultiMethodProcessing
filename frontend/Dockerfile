# Multi-stage build Dockerfile for React Merchant Dashboard
# Stage 1: Build React app using Node 18 and Vite bundler
# Stage 2: Serve static files using lightweight Nginx

# ===== Build Stage =====
# Use Node 18 Alpine for minimal image size
FROM node:18-alpine AS build
WORKDIR /app
# Copy package files and install dependencies
COPY package*.json ./
RUN npm install
# Copy source code
COPY . .
# Build React app with Vite (produces optimized dist/ folder)
RUN npm run build

# ===== Runtime Stage =====
# Use lightweight Nginx Alpine for serving static files
FROM nginx:1.25-alpine
# Copy built React app to Nginx web root
COPY --from=build /app/dist /usr/share/nginx/html
# Copy Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Expose HTTP port
EXPOSE 80
# Run Nginx in foreground (required for Docker containers)
CMD ["nginx", "-g", "daemon off;"]
