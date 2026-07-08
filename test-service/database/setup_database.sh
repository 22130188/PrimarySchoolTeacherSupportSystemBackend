#!/bin/bash

# =====================================================
# Setup Test Database - Linux/Mac Bash Script
# =====================================================

echo ""
echo "========================================"
echo "Setting up Test Database..."
echo "========================================"
echo ""

# Get MySQL credentials
read -p "Enter MySQL username (default: root): " mysql_user
mysql_user=${mysql_user:-root}

read -sp "Enter MySQL password (press Enter if none): " mysql_password
echo ""

# Check if schema file exists
if [ ! -f "schema.sql" ]; then
    echo "ERROR: schema.sql not found in current directory!"
    echo "Please run this script from the database directory."
    exit 1
fi

# Connect to MySQL and run schema
echo ""
echo "Executing schema.sql..."
echo ""

if [ -z "$mysql_password" ]; then
    mysql -u "$mysql_user" < schema.sql
else
    mysql -u "$mysql_user" -p"$mysql_password" < schema.sql
fi

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Database setup completed successfully!"
    echo "========================================"
    echo ""
    echo "Verify by running:"
    echo "  mysql -u $mysql_user -e \"USE test_db; SHOW TABLES;\""
    echo ""
else
    echo ""
    echo "ERROR: Database setup failed!"
    echo "Please check MySQL connection and try again."
    echo ""
fi
