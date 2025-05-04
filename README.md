# Sports Data Parser

A lightweight console application for fetching and processing sports data from the LeonBets API.

## Prerequisites

- Java 17 or later
- Maven
- Bash shell (for running the script)

## Building the Project

The project will be built automatically when needed. The build process is smart and will only rebuild if:
- The JAR file doesn't exist
- Source files have been modified
- POM file has been modified
- Force rebuild is requested

To build manually:
```bash
mvn clean package
```

## Running the Application

The application can be run using the provided `run.sh` script. Make sure to make it executable first:

```bash
chmod +x run.sh
```

### Basic Usage

```bash
./run.sh
```

This will run the application with default settings (console output only, UTC timezone).

### Command Line Options

- `-p, --print-to-file`: Enable writing output to a file
- `-n, --no-print`: Disable writing output to a file (default)
- `-d, --dir <path>`: Set custom directory for report files (default: "reports")
- `-t, --timezone <zone>`: Set timezone (default: UTC)
- `-b, --benchmark`: Enable performance benchmarking
- `-f, --force-rebuild`: Force rebuild the project even if no changes detected
- `-h, --help`: Show help message

### Examples

1. Print output to file in the default reports directory:
```bash
./run.sh -p
```

2. Use a custom reports directory:
```bash
./run.sh -p -d /path/to/reports
```

3. Set a specific timezone:
```bash
./run.sh -p -t "Europe/London"
```

4. Force rebuild and run with custom options:
```bash
./run.sh -f -p -d ./my-reports -t "America/New_York"
```

## Output Files

When file output is enabled, the application creates files in the following format:
- File name: `yyyy-MM-dd HH:mm:ss z.txt`
- Location: In the specified reports directory (default: `./reports/`)

## Build Behavior

The script intelligently manages rebuilds:
1. Checks if the JAR file exists
2. Compares modification times of source files with the JAR
3. Checks if POM file has been modified
4. Only rebuilds if changes are detected or forced

You can force a rebuild using the `-f` or `--force-rebuild` option.

## Timezone Support

The application supports all standard Java timezone IDs. Common examples:
- `UTC`
- `Europe/London`
- `Europe/Paris`
- `America/New_York`
- `Asia/Tokyo`

For a complete list of supported timezones, you can check the [IANA Time Zone Database](https://www.iana.org/time-zones). 