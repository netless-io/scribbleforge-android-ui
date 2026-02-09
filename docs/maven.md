MAVEN CENTRAL
---
记录发布到 Maven Central 的步骤。

### 1. Create a Sonatype account

### 2. Namespace

### 3. GPG
```shell
gpg --gen-key
gpg --list-keys --keyid-format short
gpg -ab <FILE>
gpg --keyserver keys.openpgp.org --send-keys <KEY>
gpg --keyserver keys.openpgp.org --recv-keys <KEY>

```
### 4. Gradle

### 5. Deploy (gradle-maven-publish-plugin)

URL
---

[deployments](https://central.sonatype.com/publishing/deployments)
[api](https://central.sonatype.com/api-doc)
[GPG](https://central.sonatype.org/publish/requirements/gpg)
[gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)
[RELEASE FILE](https://repo1.maven.org/maven2/io/agora/aderan/board2/)
