# RustyConnector Whitelists [Native Module]
Welcome to RCM-Whitelists!
A native whitelists module built for RustyConnector.

You can download the jar from the Releases tab.

## Module Installation
1. Download the jar from the Releases tab.
2. Make sure you download whatever jar supports your version of RustyConnector.
3. Copy the jar into the `rc-modules` directory on your Proxy.
4. Reload the RustyConnector Kernel.

## API Usage
```xml
<repository>
    <id>mrnavastar-releases</id>
    <name>MrNavaStar's Repository</name>
    <url>https://maven.mrnavastar.me/releases</url>
</repository>

<dependency>
    <groupId>group.aelysium.rustyconnector</groupId>
    <artifactId>rcm-whitelists</artifactId>
    <version>0.9.1-1</version>
</dependency>
```
```gradle
maven {
    name "mrnavastarReleases"
    url "https://maven.mrnavastar.me/releases"
}

implementation "group.aelysium.rustyconnector:rcm-whitelists:0.9.1-1"
```

You can access the Whitelist registry from the RustyConnector Proxy Kernel:
```java
WhitelistRegistry registry = RC.Module("Whitelists");
```