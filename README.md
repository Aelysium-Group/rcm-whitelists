# RustyConnector Whitelists [Native Module]
Welcome to RCM-Whitelists!
A native whitelists module built for RustyConnector.

You can download the jar from the Releases tab.

For API access use
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