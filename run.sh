#!/bin/bash

# Default values
PRINT_TO_FILE=""
REPORTS_DIR=""
TIMEZONE=""
BENCHMARK=""
JAR_FILE="target/parser-1.0-SNAPSHOT.jar"
SRC_DIR="src/main/java"
POM_FILE="pom.xml"

# Function to show script usage
show_usage() {
    echo "Usage: $0 [options]"
    echo "Options:"
    echo "  -p, --print-to-file    Enable writing output to a file"
    echo "  -n, --no-print         Disable writing output to a file (default)"
    echo "  -d, --dir <path>       Set custom directory for report files"
    echo "  -t, --timezone <zone>  Set timezone (e.g., UTC, Europe/London)"
    echo "  -f, --force-rebuild    Force rebuild the project"
    echo "  -b, --benchmark        Enable performance benchmarking"
    echo "  -h, --help             Show this help message"
}

# Function to check if rebuild is needed
needs_rebuild() {
    # If JAR doesn't exist, rebuild is needed
    if [ ! -f "$JAR_FILE" ]; then
        echo "JAR file doesn't exist. Build needed."
        return 0
    fi

    # Get JAR modification time
    jar_time=$(stat -c %Y "$JAR_FILE")

    # Check pom.xml modification time
    if [ -f "$POM_FILE" ]; then
        pom_time=$(stat -c %Y "$POM_FILE")
        if [ "$pom_time" -gt "$jar_time" ]; then
            echo "POM file has been modified. Build needed."
            return 0
        fi
    fi

    # Check if any source file is newer than JAR
    while IFS= read -r -d '' file; do
        file_time=$(stat -c %Y "$file")
        if [ "$file_time" -gt "$jar_time" ]; then
            echo "Source files have been modified. Build needed."
            return 0
        fi
    done < <(find "$SRC_DIR" -type f -name "*.java" -print0)

    echo "No changes detected. Using existing build."
    return 1
}

# Parse command line arguments
FORCE_REBUILD=false
while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--print-to-file)
            PRINT_TO_FILE="--print-to-file"
            shift
            ;;
        -n|--no-print)
            PRINT_TO_FILE="--no-print-to-file"
            shift
            ;;
        -d|--dir)
            REPORTS_DIR="--reports-dir $2"
            shift 2
            ;;
        -t|--timezone)
            TIMEZONE="--timezone $2"
            shift 2
            ;;
        -f|--force-rebuild)
            FORCE_REBUILD=true
            shift
            ;;
        -b|--benchmark)
            BENCHMARK="--benchmark"
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Build the project if needed
if [ "$FORCE_REBUILD" = true ] || needs_rebuild; then
    echo "Building the project..."
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "Build failed"
        exit 1
    fi
fi

# Verify the JAR exists (final check)
if [ ! -f "$JAR_FILE" ]; then
    echo "Failed to locate JAR file"
    exit 1
fi

# Run the application with provided arguments
java -jar "$JAR_FILE" $PRINT_TO_FILE $REPORTS_DIR $TIMEZONE $BENCHMARK 