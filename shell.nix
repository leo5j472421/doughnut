{ pkgs ? import <nixpkgs> { } }:
with pkgs;
let
  inherit (pkgs) stdenv;
  apple_sdk = darwin.apple_sdk.frameworks;
in mkShell {
  name = "doughnut";
  MYSQL_HOME = builtins.getEnv "MYSQL_HOME";
  MYSQL_DATADIR = builtins.getEnv "MYSQL_DATADIR";
  buildInputs = [
    python312
    pipenv
    nodejs_20
    zsh
    dum
    yarn
    jdk17
    kotlin
    flutter
    libiconv
    git
    git-secret
    gitAndTools.delta
    binutils-unwrapped
    hostname
    inetutils
    openssh
    pkg-config
    rsync
    autojump
    fasd
    fzf
    gnupg
    jq
    less
    lesspipe
    lsof
    lzma
    btop
    ps
    vgrep
    unixtools.whereis
    libmysqlclient
    libpcap
    patchelf
    cacert
    mysql80
    mysql-client
    mysql_jdbc
    uutils-coreutils
    google-cloud-sdk
    yamllint
    atuin
    chezmoi
    nix-direnv
    qemu
    podman
  ] ++ lib.optionals stdenv.isDarwin [
    darwin.apple_sdk.libs.utmp
    apple_sdk.CoreServices
    apple_sdk.Security
    pinentry_mac
    sequelpro
    cocoapods
    xcbuild
  ] ++ lib.optionals (!stdenv.isDarwin) [
    sequeler
    ungoogled-chromium
    psmisc
    x11vnc
    xclip
    xvfb-run
    pinentry
  ];
  shellHook = ''
            #!/usr/bin/env bash

            export NIXPKGS_ALLOW_UNFREE=1
            export PS1="(nix)$PS1"
            export GPG_TTY=$(tty)
            export JAVA_HOME="$(readlink -e $(type -p javac) | sed  -e 's/\/bin\/javac//g')"
            export KOTLIN_HOME="$(readlink -e $(type -p kotlinc) | sed -e 's/\/bin\/kotlinc//g')"
            export NODE_PATH="$(readlink -e $(type -p node) | sed  -e 's/\/bin\/node//g')"
            export DUM_PATH="$(readlink -e $(type -p dum) | sed  -e 's/\/bin\/dum//g')"
            export FLUTTER_PATH="$(readlink -e $(type -p flutter) | sed  -e 's/\/bin\/flutter//g')"
            export DART_PATH="$(readlink -e $(type -p dart) | sed  -e 's/\/bin\/dart//g')"
            export PUB_CACHE="''${PUB_CACHE:-$PWD/.pub-cache}"

            export MYSQL_BASEDIR=${pkgs.mysql80}
            export MYSQL_HOME="''${MYSQL_HOME:-$PWD/mysql}"
            export MYSQL_DATADIR="''${MYSQL_DATADIR:-$MYSQL_HOME/data}"
            export MYSQL_UNIX_SOCKET=$MYSQL_HOME/mysql.sock
            export MYSQLX_UNIX_SOCKET=$MYSQL_HOME/mysqlx.sock
            export MYSQL_PID_FILE=$MYSQL_HOME/mysql.pid
            export MYSQL_TCP_PORT=3309
            export MYSQLX_TCP_PORT=33090
            export PATH=$JAVA_HOME/bin:$KOTLIN_HOME/bin:$DUM_PATH/bin:$NODE_PATH/bin:$MYSQL_BASEDIR/bin:$FLUTTER_PATH/bin:$DART_PATH/bin:$PATH
            export LANG="en_US.UTF-8"
            #export LC_ALL="en_US.UTF-8"

            if [[ "$USER" = @(codespace|gitpod) ]]; then
              [[ -d $HOME/.cache/Cypress ]] || npx --yes cypress install --force
            fi

            echo "###################################################################################################################"
            echo "                                                                                "
            echo "##   !! DOUGHNUT NIX DEVELOPMENT ENVIRONMENT ;) !!    "
            echo "##   NIX VERSION: `nix --version`                     "
            echo "##   JAVA_HOME: $JAVA_HOME                            "
            echo "##   KOTLIN_HOME: $KOTLIN_HOME                        "
            echo "##   NODE_PATH: $NODE_PATH                            "
            echo "##   FLUTTER_PATH: $FLUTTER_PATH                      "
            echo "##   MYSQL_BASEDIR: $MYSQL_BASEDIR                    "
            echo "##   MYSQL_HOME: $MYSQL_HOME                          "
            echo "##   MYSQL_DATADIR: $MYSQL_DATADIR                    "
            echo "##   JAVA VERSION: `javac --version`                  "
            echo "##   KOTLIN VERSION: `kotlin -version`                "
            echo "##   NODE VERSION: `node --version`                   "
            echo "##   FLUTTER VERSION: `flutter --version | head -n 1` "
            echo "                                                                                "
            echo "###################################################################################################################"
            mkdir -p $MYSQL_HOME
            mkdir -p $MYSQL_DATADIR

            cat <<EOF > $MYSQL_HOME/init_doughnut_db.sql
            CREATE USER IF NOT EXISTS 'doughnut'@'localhost' IDENTIFIED BY 'doughnut';
            CREATE USER IF NOT EXISTS 'doughnut'@'127.0.0.1' IDENTIFIED BY 'doughnut';
            CREATE DATABASE IF NOT EXISTS doughnut_development DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
            CREATE DATABASE IF NOT EXISTS doughnut_test        DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
            CREATE DATABASE IF NOT EXISTS doughnut_e2e_test    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
            GRANT ALL PRIVILEGES ON doughnut_development.* TO 'doughnut'@'localhost';
            GRANT ALL PRIVILEGES ON doughnut_test.*        TO 'doughnut'@'localhost';
            GRANT ALL PRIVILEGES ON doughnut_e2e_test.*    TO 'doughnut'@'localhost';
            GRANT ALL PRIVILEGES ON doughnut_development.* TO 'doughnut'@'127.0.0.1';
            GRANT ALL PRIVILEGES ON doughnut_test.*        TO 'doughnut'@'127.0.0.1';
            GRANT ALL PRIVILEGES ON doughnut_e2e_test.*    TO 'doughnut'@'127.0.0.1';
            FLUSH PRIVILEGES;
            EOF

            export MYSQLD_PID=$(ps -ax | grep -v " grep " | grep mysqld | awk '{ print $1 }')
            if [[ -z "$MYSQLD_PID" ]]; then
              [ ! "$(ls -A mysql/data)" ] && mysqld --initialize-insecure --port=$MYSQL_TCP_PORT --user=`whoami` --datadir=$MYSQL_DATADIR --tls-version=TLSv1.2 --basedir=$MYSQL_BASEDIR --explicit_defaults_for_timestamp
              mysqld --datadir=$MYSQL_DATADIR --pid-file=$MYSQL_PID_FILE --port=$MYSQL_TCP_PORT --socket=$MYSQL_UNIX_SOCKET --mysqlx-socket=$MYSQLX_UNIX_SOCKET --mysqlx_port=$MYSQLX_TCP_PORT --tls-version=TLSv1.2 &
              export MYSQLD_PID=$!

              sleep 6 && mysql -u root -S $MYSQL_UNIX_SOCKET < $MYSQL_HOME/init_doughnut_db.sql
            fi

            export NIX_SSL_CERT_FILE=/etc/ssl/certs/ca-certificates.crt
            if [[ "$OSTYPE" == "darwin"* ]]; then
              export NIX_SSL_CERT_FILE=/etc/ssl/cert.pem
            fi

            cleanup()
            {
              echo -e "\nBYE!!! EXITING doughnut NIX DEVELOPMENT ENVIRONMENT."
              rm -f $MYSQL_HOME/init_doughnut_db.sql
              if [[ ! -z "$MYSQLD_PID" ]]; then
                echo -e "MySQL Server still running on Port: $MYSQL_TCP_PORT, Socket: $MYSQL_UNIX_SOCKET at PID: $MYSQLD_PID"
                echo -e "You may choose to SHUTDOWN MySQL Server by issuing 'kill -SIGTERM $MYSQLD_PID'\n"
              fi
            }
            trap cleanup EXIT
  '';
}
