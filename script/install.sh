#!/bin/sh

set -e

OS_NAME=$(uname -s)
JB_PRODUCTS="idea clion phpstorm goland pycharm webstorm webide rider datagrip rubymine appcode dataspell gateway jetbrains_client jetbrainsclient studio devecostudio"

BASE_PATH=$(dirname $(
  cd $(dirname "$0")
  pwd
))

JAR_FILE_PATH="${BASE_PATH}/jetbra-agent.jar"

if [ ! -f "${JAR_FILE_PATH}" ]; then
  echo 'jetbra-agent.jar not found'
  exit -1
fi

KDE_ENV_DIR="${HOME}/.config/plasma-workspace/env"
LAUNCH_AGENTS_DIR="${HOME}/Library/LaunchAgents"

PROFILE_PATH="${HOME}/.profile"
ZSH_PROFILE_PATH="${HOME}/.zshrc"
PLIST_PATH="${LAUNCH_AGENTS_DIR}/jetbrains.vmoptions.plist"

if [ $OS_NAME = "Darwin" ]; then
  BASH_PROFILE_PATH="${HOME}/.bash_profile"

  mkdir -p "${LAUNCH_AGENTS_DIR}"
  echo '<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd"><plist version="1.0"><dict><key>Label</key><string>jetbrains.vmoptions</string><key>ProgramArguments</key><array><string>sh</string><string>-c</string><string>' >"${PLIST_PATH}"
else
  BASH_PROFILE_PATH="${HOME}/.bashrc"
  mkdir -p "${KDE_ENV_DIR}"
fi

touch "${PROFILE_PATH}"
touch "${BASH_PROFILE_PATH}"
touch "${ZSH_PROFILE_PATH}"

MY_VMOPTIONS_SHELL_NAME="jetbrains.vmoptions.sh"
MY_VMOPTIONS_SHELL_FILE="${HOME}/.${MY_VMOPTIONS_SHELL_NAME}"
echo '#!/bin/sh' >"${MY_VMOPTIONS_SHELL_FILE}"

EXEC_LINE='___MY_VMOPTIONS_SHELL_FILE="${HOME}/.jetbrains.vmoptions.sh"; if [ -f "${___MY_VMOPTIONS_SHELL_FILE}" ]; then . "${___MY_VMOPTIONS_SHELL_FILE}"; fi'

for PRD in $JB_PRODUCTS; do
  VM_FILE_PATH="${BASE_PATH}/vmoptions/${PRD}.vmoptions"
  if [ ! -f "${VM_FILE_PATH}" ]; then
    continue
  fi

  if [ $OS_NAME = "Darwin" ]; then
    sed -i '' '/^\-javaagent:.*[\/\\]jetbra\-agent\.jar.*/d' "${VM_FILE_PATH}"
  else
    sed -i '/^\-javaagent:.*[\/\\]jetbra\-agent\.jar.*/d' "${VM_FILE_PATH}"
  fi

  echo "-javaagent:${JAR_FILE_PATH}" >>"${VM_FILE_PATH}"

  ENV_NAME=$(echo $PRD | tr '[a-z]' '[A-Z]')"_VM_OPTIONS"
  echo "export ${ENV_NAME}=\"${VM_FILE_PATH}\"" >>"${MY_VMOPTIONS_SHELL_FILE}"

  if [ $OS_NAME = "Darwin" ]; then
    launchctl setenv "${ENV_NAME}" "${VM_FILE_PATH}"
    echo "launchctl setenv \"${ENV_NAME}\" \"${VM_FILE_PATH}\"" >>"${PLIST_PATH}"
  fi
done

if [ $OS_NAME = "Darwin" ]; then
  sed -i '' '/___MY_VMOPTIONS_SHELL_FILE="${HOME}\/\.jetbrains\.vmoptions\.sh"; if /d' "${PROFILE_PATH}" >/dev/null 2>&1
  sed -i '' '/___MY_VMOPTIONS_SHELL_FILE="${HOME}\/\.jetbrains\.vmoptions\.sh"; if /d' "${BASH_PROFILE_PATH}" >/dev/null 2>&1
  sed -i '' '/___MY_VMOPTIONS_SHELL_FILE="${HOME}\/\.jetbrains\.vmoptions\.sh"; if /d' "${ZSH_PROFILE_PATH}" >/dev/null 2>&1
  
  echo '</string></array><key>RunAtLoad</key><true/></dict></plist>' >>"${PLIST_PATH}"
else
  sed -i '/___MY_VMOPTIONS_SHELL_FILE="${HOME}\/\.jetbrains\.vmoptions\.sh"; if /d' "${PROFILE_PATH}" >/dev/null 2>&1
  sed -i '/___MY_VMOPTIONS_SHELL_FILE="${HOME}\/\.jetbrains\.vmoptions\.sh"; if /d' "${BASH_PROFILE_PATH}" >/dev/null 2>&1
  sed -i '/___MY_VMOPTIONS_SHELL_FILE="${HOME}\/\.jetbrains\.vmoptions\.sh"; if /d' "${ZSH_PROFILE_PATH}" >/dev/null 2>&1
fi

echo "${EXEC_LINE}" >>"${PROFILE_PATH}"
echo "${EXEC_LINE}" >>"${BASH_PROFILE_PATH}"
echo "${EXEC_LINE}" >>"${ZSH_PROFILE_PATH}"

if [ $OS_NAME = "Darwin" ]; then
  echo 'done. the "kill Dock" command can fix the crash issue.'
else
  ln -sf "${MY_VMOPTIONS_SHELL_FILE}" "${KDE_ENV_DIR}/${MY_VMOPTIONS_SHELL_NAME}"
  echo "done. you'd better log off first!"
fi
