#!/usr/bin/env bash
set -Eeuo pipefail

# CherryVis setup helper for Linux Mint / Ubuntu.
# Installs system dependencies, downloads TorchCraftAI and Emscripten,
# creates a Python virtual environment, builds the OpenBW viewer, and
# optionally starts the local CherryVis server.
#
# Usage:
#   chmod +x install_cherryvis.sh
#   ./install_cherryvis.sh
#   ./install_cherryvis.sh --run
#   ./install_cherryvis.sh --run-only
#
# --run installs everything and then starts the server; --run-only skips the
# installation and only starts the server (fails fast when something that the
# installation would provide is missing).
#
# Default directories:
#   /sc-ai/CherryVis/TorchCraftAI  - CherryVis source repository
#   /sc-ai/CherryVis/emsdk         - Emscripten SDK
#   /sc-ai/CherryVis/venv_django   - Python virtual environment

BASE_DIR="${BASE_DIR:-/sc-ai/CherryVis}"
REPO_DIR="${REPO_DIR:-$BASE_DIR/TorchCraftAI}"
EMSDK_DIR="${EMSDK_DIR:-$BASE_DIR/emsdk}"
CHERRYVIS_DIR="$REPO_DIR/scripts/cherryvis"
VENV_DIR="${VENV_DIR:-$BASE_DIR/venv_django}"
START_SERVER=0
RUN_ONLY=0

if [[ "${1:-}" == "--run" ]]; then
    START_SERVER=1
elif [[ "${1:-}" == "--run-only" ]]; then
    RUN_ONLY=1
elif [[ -n "${1:-}" ]]; then
    echo "Usage: $0 [--run|--run-only]"
    exit 2
fi

log() {
    printf '\n\033[1;34m==> %s\033[0m\n' "$*"
}

die() {
    printf '\nERROR: %s\n' "$*" >&2
    exit 1
}

need_command() {
    command -v "$1" >/dev/null 2>&1 || die "Required command not found: $1"
}

if [[ "$RUN_ONLY" -eq 1 ]]; then
    echo "Starting CherryVis server without installation..."
    [[ -d "$CHERRYVIS_DIR" ]] || die "CherryVis directory not found: $CHERRYVIS_DIR. Run '$0' (install) first."
    [[ -f "$CHERRYVIS_DIR/server.py" ]] || die "server.py not found in $CHERRYVIS_DIR. Run '$0' (install) first."
    [[ -x "$VENV_DIR/bin/python" ]] || die "Python virtual environment not found: $VENV_DIR. Run '$0' (install) first."
    log "Starting CherryVis server (Ctrl+C stops it)"
    cd "$CHERRYVIS_DIR"
    # shellcheck disable=SC1091
    source "$VENV_DIR/bin/activate"
    exec python server.py
fi

log "Installing system dependencies"
sudo apt update
sudo apt install -y \
    git python3 python3-venv python3-pip \
    cmake build-essential nodejs npm curl ca-certificates

need_command git
need_command python3

# Create the requested base directory and make it writable by the current user.
if [[ ! -d "$BASE_DIR" ]]; then
    log "Creating base directory: $BASE_DIR"
    sudo mkdir -p "$BASE_DIR"
    sudo chown "$(id -un):$(id -gn)" "$BASE_DIR"
fi
[[ -w "$BASE_DIR" ]] || die "Base directory is not writable: $BASE_DIR. Fix its ownership/permissions first."

log "Downloading TorchCraftAI"
if [[ -d "$REPO_DIR/.git" ]]; then
    echo "Repository already exists: $REPO_DIR"
else
    [[ ! -e "$REPO_DIR" ]] || die "$REPO_DIR exists but is not a Git repository. Move it or set REPO_DIR."
    git clone https://github.com/TorchCraft/TorchCraftAI.git "$REPO_DIR"
fi

[[ -d "$CHERRYVIS_DIR" ]] || die "CherryVis directory not found: $CHERRYVIS_DIR"
[[ -f "$CHERRYVIS_DIR/build_js.sh" ]] || die "build_js.sh not found in $CHERRYVIS_DIR"
[[ -f "$CHERRYVIS_DIR/server.py" ]] || die "server.py not found in $CHERRYVIS_DIR"

log "Installing Emscripten SDK"
if [[ -d "$EMSDK_DIR/.git" ]]; then
    echo "Emscripten SDK already exists: $EMSDK_DIR"
else
    [[ ! -e "$EMSDK_DIR" ]] || die "$EMSDK_DIR exists but is not an Emscripten Git checkout. Move it or set EMSDK_DIR."
    git clone https://github.com/emscripten-core/emsdk.git "$EMSDK_DIR"
fi

(
    cd "$EMSDK_DIR"
    ./emsdk install latest
    ./emsdk activate latest
)

# Add the SSH-to-HTTPS rewrite to this checkout's local Git config.
# This avoids requiring a GitHub SSH key if build_js.sh clones OpenBW via SSH.
log "Configuring GitHub HTTPS clone fallback"
git -C "$REPO_DIR" config --local \
    url."https://github.com/".insteadOf git@github.com:

log "Preparing Python virtual environment"
if [[ ! -d "$VENV_DIR" ]]; then
    python3 -m venv "$VENV_DIR"
fi
# shellcheck disable=SC1091
source "$VENV_DIR/bin/activate"
python -m pip install --upgrade pip
# django + zstandard come from the upstream README; the zstd package is needed
# on top because replays/views.py does `import zstd` (measured 2026-10-12 -
# without it every page returns HTTP 500 with ModuleNotFoundError).
python -m pip install django zstandard zstd

log "Checking Emscripten"
# shellcheck disable=SC1091
source "$EMSDK_DIR/emsdk_env.sh"
emcc --version
emcmake --version

log "Building the OpenBW viewer"
cd "$CHERRYVIS_DIR"
chmod +x ./build_js.sh
./build_js.sh

log "Checking build output"
if [[ -d "$CHERRYVIS_DIR/replays/static/replays/viewer" ]]; then
    find "$CHERRYVIS_DIR/replays/static/replays/viewer" -maxdepth 1 \
        -type f \( -name 'test.html' -o -name 'test.js' -o -name 'test.wasm' \) \
        -printf '%f  (%k KB)\n' || true
else
    echo "Viewer output directory not found; inspect the build log above."
fi

log "Setup finished"
echo "Base directory: $BASE_DIR"
echo "CherryVis directory: $CHERRYVIS_DIR"
echo "Emscripten SDK: $EMSDK_DIR"
echo "Python environment: $VENV_DIR"
echo "To start CherryVis later, run:"
printf '  cd "%s"\n' "$CHERRYVIS_DIR"
printf '  source "%s/bin/activate"\n' "$VENV_DIR"
echo "  python server.py"
echo "Then open: http://127.0.0.1:8770/"

if [[ "$START_SERVER" -eq 1 ]]; then
    log "Starting CherryVis server (Ctrl+C stops it)"
    cd "$CHERRYVIS_DIR"
    # shellcheck disable=SC1091
    source "$VENV_DIR/bin/activate"
    exec python server.py
fi
