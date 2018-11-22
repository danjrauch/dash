#!/bin/zsh
set -e
set -u
set -o pipefail

dishelp(){
  echo "Servio DX for CPS 0.0.1 (8.6.2018)"
  echo
  echo "usage: zsh build.sh [-m [name]] [-s] [-S] [-p [name]] [-B [name]] [--help] " >&2
  echo
  echo "Arguments:"
  echo "   -b                    Build the project on Docker Container"
  echo "   --help                Ask for help"
}

if [ "$#" = "0" ]; then
  dishelp
  exit 0
fi

if [ "$1" = "--help" ] ; then
  dishelp
  exit 0
fi

buildProject(){
  echo "Starting to Build dashDB"
  # if [ ! "$(docker ps -q -f name=dashdb)" ]; then
  #   if [ "$(docker ps -aq -f status=exited -f name=dashdb)" ]; then
  #     docker rm dashdb
  #   fi
  # fi
  docker build -t dashdb .
  docker run -it --rm --name running-app dashdb
  echo "Done Building dashDB"
}

while getopts 'b' OPTION; do
  case "$OPTION" in
    b)
      buildProject
      ;;
    ?)
      echo "That is an invalid command"
      dishelp
      exit 1
      ;;
  esac
done
shift "$(($OPTIND -1))"
