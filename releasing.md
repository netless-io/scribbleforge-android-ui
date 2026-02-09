Releasing
=========
Before Release
-----------------

1. unit test and instrumented test passed.
```shell

```

Cutting a Release
-----------------

1. Update `CHANGELOG.md`.

2. Set versions:

    ```shell
    export RELEASE_VERSION=$(awk -F '[][]' '/\[/{print $2; exit}' CHANGELOG.md) \
      && echo "RELEASE_VERSION=$RELEASE_VERSION"
    ```
3. Update versions:
   ```shell
    sed -i '' \
   "s/^VERSION_NAME=.*/VERSION_NAME=${RELEASE_VERSION}/" "gradle.properties"
    sed -i '' \
   "s/^agoraBoardForge = \".*\"/agoraBoardForge = \"${RELEASE_VERSION}\"/" "gradle/libs.versions.toml"
   find . -name "Room.kt" -exec sed -i '' 's/\(const val VERSION = "\)[^"]*\(".*\)/\1'"${RELEASE_VERSION}"'\2/' {} \;
    ```

4. Tag the release and push to GitHub.
   ```shell
   git commit -am "Prepare for release $RELEASE_VERSION"
   git tag -a $RELEASE_VERSION -m "Version $RELEASE_VERSION"
   git push -v origin refs/heads/main:refs/heads/main
   git push origin $RELEASE_VERSION
   ```

5. Publish to maven
    ```shell
    ./publishMaven.sh all remote
    ```

6. Check Maven

- [ ] Check the release on [maven central](https://central.sonatype.com/artifact/io.github.duty-os.forge/forge-all)

Local Testing
----------------

1. publish to local maven
    ```shell
    export RELEASE_VERSION=1.0.1-beta01
    
    sed -i '' \
    "s/^VERSION_NAME=.*/VERSION_NAME=${RELEASE_VERSION}/" "gradle.properties"
    
    ./publishMaven.sh all local
    ```

2. configure the local maven in your project
    ```groovy
    repositories {
        // build.gradle local path
        maven { url = uri('/Users/flb/MavenRepo') }
    }
    ```
