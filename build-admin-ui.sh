#!/usr/bin/env bash
# Build admin UI and copy to BFF server resources

set -e

echo "Building IAM Admin UI..."

# Navigate to admin-ui directory
cd "$(dirname "$0")/iam-admin-ui"

# Install dependencies
echo "Installing dependencies..."
npm install

# Build for production
echo "Building admin UI..."
npm run build

# Copy dist folder to BFF server resources
echo "Copying build output to BFF server..."
rm -rf ../iam-bff-server/src/main/resources/static/admin-ui
mkdir -p ../iam-bff-server/src/main/resources/static
cp -r dist ../iam-bff-server/src/main/resources/static/admin-ui

echo "Admin UI build complete!"
echo "Files copied to: iam-bff-server/src/main/resources/static/admin-ui"
