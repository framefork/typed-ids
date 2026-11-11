#!/usr/bin/env bash
# Checks the typed-ids index processor in real Maven builds of this project.
#
# Usage: run-maven-index-check.sh <typed-ids version>
# Publish that version to the local Maven repository first, from the repository root:
#   ./gradlew :typed-ids:publishToMavenLocal :typed-ids-index-java-classes-processor:publishToMavenLocal -Pversion=<typed-ids version>
#
# 1. A clean build with each processor wiring from the README, whose tests assert both index files and TypedIdsRegistry.
#    The surefire reports are copied to build/test-results/test/, where the CI picks up the test results of the Gradle modules too.
# 2. Partial compiles of a copy of the project, which assert the index after each change.
#    A source deleted with useIncrementalCompilation=false is not checked: maven-compiler-plugin then compiles nothing and leaves the class file
#    in target/classes, so the class stays loadable and the processor does not run.
#    The deletion is checked with incremental compilation, which deletes the class files that the previous incremental compile created,
#    so it runs before the non-incremental compiles: after one of those, maven-compiler-plugin also keeps the class files of deleted sources.
set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <typed-ids version>" >&2
    exit 2
fi

version=$1
project_dir=$(cd "$(dirname "$0")" && pwd)
results_dir=$project_dir/build/test-results/test

uuid_index=target/classes/META-INF/services/org.framefork.typedIds.uuid.ObjectUuid
bigint_index=target/classes/META-INF/services/org.framefork.typedIds.bigint.ObjectBigIntId

full_uuid_index='com.example.Catalog$Section$Item$Id
com.example.Person$Id
com.example.TenantUserId
com.example.UserId
com.example.Warehouse$Id'

full_bigint_index='com.example.Catalog$Section$Item$Sku
com.example.OrderId
com.example.Person$LegacyId
com.example.TenantOrderId
com.example.Warehouse$ShelfId'

mvnw() {
    ./mvnw --batch-mode --no-transfer-progress -Dtyped-ids.version="$version" "$@"
}

# Runs a compile, prints its log, and fails unless javac compiled exactly the expected number of main sources.
compile() {
    local expected_sources=$1
    shift

    local log
    log=$(mvnw -Pprocessor-path "$@" compile 2>&1) || { echo "$log"; return 1; }
    echo "$log"

    local main_sources="Compiling $expected_sources source file"
    if ! grep -qE "$main_sources(s)? with javac .* to target/classes" <<<"$log"; then
        echo "FAILED: expected maven-compiler-plugin to compile $expected_sources main sources" >&2
        return 1
    fi
}

# Compares an index file with the expected entries, which get a trailing newline each, as the processor writes them.
assert_index() {
    local file=$1 expected=$2
    if ! diff -u --label "expected $file" <(printf '%s\n' "$expected") --label "actual $file" "$file"; then
        echo "FAILED: unexpected content of $file" >&2
        return 1
    fi
    echo "OK: $file"
}

without() {
    grep -vxF "$2" <<<"$1"
}

with() {
    printf '%s\n%s\n' "$1" "$2" | sort
}

echo "## Clean builds with both processor wirings"
cd "$project_dir"
rm -rf "$results_dir"
mkdir -p "$results_dir"
for wiring in processor-path classpath-discovery; do
    echo "### $wiring"
    mvnw -P"$wiring" -Dsurefire.reportNameSuffix="$wiring" clean test
    cp target/surefire-reports/TEST-*.xml "$results_dir/"
    assert_index "$uuid_index" "$full_uuid_index"
    assert_index "$bigint_index" "$full_bigint_index"
done

echo "## Partial compiles"
work_dir=$project_dir/target/partial-compile
rm -rf "$work_dir"
mkdir -p "$work_dir"
cp -r "$project_dir/pom.xml" "$project_dir/mvnw" "$project_dir/.mvn" "$project_dir/src" "$work_dir/"
cd "$work_dir"
sources=src/main/java/com/example
non_incremental=-Dmaven.compiler.useIncrementalCompilation=false

echo "### Full compile"
compile 10
uuid=$full_uuid_index
bigint=$full_bigint_index
assert_index "$uuid_index" "$uuid"
assert_index "$bigint_index" "$bigint"

echo "### A deleted source with incremental compilation, which recompiles the module and deletes the class files that the previous incremental compile created"
rm "$sources/Warehouse.java"
compile 9
if compgen -G 'target/classes/com/example/Warehouse*.class' >/dev/null; then
    echo "FAILED: maven-compiler-plugin kept the class files of the deleted Warehouse.java, so this scenario checks nothing" >&2
    exit 1
fi
uuid=$(without "$uuid" 'com.example.Warehouse$Id')
bigint=$(without "$bigint" 'com.example.Warehouse$ShelfId')
assert_index "$uuid_index" "$uuid"
assert_index "$bigint_index" "$bigint"

# From here on without incremental compilation, so maven-compiler-plugin compiles only the changed sources and the processor merges with the existing index.
echo "### A new id class is merged into the existing index"
cat >"$sources/AddedId.java" <<'EOF'
package com.example;

public final class AddedId extends org.framefork.typedIds.uuid.ObjectUuid<AddedId>
{
    private AddedId(final java.util.UUID inner) { super(inner); }
}
EOF
compile 1 "$non_incremental"
uuid=$(with "$uuid" 'com.example.AddedId')
assert_index "$uuid_index" "$uuid"
assert_index "$bigint_index" "$bigint"

echo "### An id class that became abstract is dropped"
cat >"$sources/AddedId.java" <<'EOF'
package com.example;

public abstract class AddedId extends org.framefork.typedIds.uuid.ObjectUuid<AddedId>
{
    protected AddedId(final java.util.UUID inner) { super(inner); }
}
EOF
compile 1 "$non_incremental"
uuid=$(without "$uuid" 'com.example.AddedId')
assert_index "$uuid_index" "$uuid"
assert_index "$bigint_index" "$bigint"

echo "### An id class that moved to the other base leaves the old index, though the compile has no class of the old base"
cat >"$sources/UserId.java" <<'EOF'
package com.example;

public final class UserId extends org.framefork.typedIds.bigint.ObjectBigIntId<UserId>
{
    private UserId(final long inner) { super(inner); }
}
EOF
compile 1 "$non_incremental"
uuid=$(without "$uuid" 'com.example.UserId')
bigint=$(with "$bigint" 'com.example.UserId')
assert_index "$uuid_index" "$uuid"
assert_index "$bigint_index" "$bigint"

echo "### A class that is no longer an id is dropped when the compile has an id class"
cat >"$sources/UserId.java" <<'EOF'
package com.example;

public final class UserId
{
}
EOF
touch "$sources/Person.java"
compile 2 "$non_incremental"
bigint=$(without "$bigint" 'com.example.UserId')
assert_index "$uuid_index" "$uuid"
assert_index "$bigint_index" "$bigint"

echo "All Maven index checks passed"
