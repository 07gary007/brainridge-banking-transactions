#!/bin/sh

set -eu

wrapper_version="3.9.11"
maven_user_home="${MAVEN_USER_HOME:-${HOME}/.m2}"
wrapper_dist_dir="${maven_user_home}/wrapper/dists"
maven_home="${wrapper_dist_dir}/apache-maven-${wrapper_version}"
maven_binary="${maven_home}/bin/mvn"

if [ ! -x "${maven_binary}" ]; then
    temporary_dir="$(mktemp -d 2>/dev/null || mktemp -d -t banking-transactions-maven)"
    archive_path="${temporary_dir}/apache-maven-${wrapper_version}-bin.tar.gz"
    extracted_home="${temporary_dir}/apache-maven-${wrapper_version}"

    trap 'rm -rf "${temporary_dir}"' EXIT INT TERM

    echo "Maven ${wrapper_version} is not cached; downloading it once..."
    curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${wrapper_version}/apache-maven-${wrapper_version}-bin.tar.gz" \
        -o "${archive_path}"
    tar -xzf "${archive_path}" -C "${temporary_dir}"
    mkdir -p "${wrapper_dist_dir}"
    mv "${extracted_home}" "${maven_home}"
fi

exec "${maven_binary}" "$@"
