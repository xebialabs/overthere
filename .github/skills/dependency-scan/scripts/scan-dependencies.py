#!/usr/bin/env python3
"""
scan-dependencies.py — Dependency scan for overthere
Phases:
  xl-platform-scan  : Scan xl-platform BOM for upstream updates
  scan              : Scan overthere's own build.gradle for hardcoded dep updates
  alignment         : Check version consistency in build.gradle
  all               : Run xl-platform-scan + scan + alignment

Usage:
  python3 scan-dependencies.py --phase scan
  python3 scan-dependencies.py --phase all --xl-platform-dir ../xl-platform
"""

import argparse
import base64
import json
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional

# Windows: ensure stdout/stderr use UTF-8 so emoji and non-ASCII chars print correctly
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

MAVEN_CENTRAL_METADATA = "https://repo1.maven.org/maven2/{group}/{artifact}/maven-metadata.xml"
MAVEN_CENTRAL_SEARCH = "https://search.maven.org/solrsearch/select"
NEXUS_BASE_URL = "https://nexus.xebialabs.com/nexus/content"
NEXUS_REPOS = ["releases", "central", "public"]

REQUEST_TIMEOUT = 15
CACHE: dict[str, Optional[str]] = {}
FAILED: set[str] = set()

# ---------------------------------------------------------------------------
# XL_PLATFORM_COORDINATES — 114 entries
# Maps xl-platform variable name → "groupId:artifactId"
# ---------------------------------------------------------------------------
XL_PLATFORM_COORDINATES: dict[str, str] = {
    # Spring ecosystem
    "springBootVersion": "org.springframework.boot:spring-boot",
    "springVersion": "org.springframework:spring-core",
    "springSecurityVersion": "org.springframework.security:spring-security-core",
    "springDataVersion": "org.springframework.data:spring-data-commons",
    "springIntegrationVersion": "org.springframework.integration:spring-integration-core",
    "springCloudVersion": "org.springframework.cloud:spring-cloud-core",
    "springBatchVersion": "org.springframework.batch:spring-batch-core",
    "springRetryVersion": "org.springframework.retry:spring-retry",
    "springWebservicesVersion": "org.springframework.ws:spring-ws-core",
    "springSessionVersion": "org.springframework.session:spring-session-core",
    "springShellVersion": "org.springframework.shell:spring-shell-core",
    "springKafkaVersion": "org.springframework.kafka:spring-kafka",
    "springRabbitVersion": "org.springframework.amqp:spring-rabbit",
    # Hibernate / JPA
    "hibernateVersion": "org.hibernate.orm:hibernate-core",
    "hibernateValidatorVersion": "org.hibernate.validator:hibernate-validator",
    "hibernateSearchVersion": "org.hibernate.search:hibernate-search-engine",
    "javaxPersistenceVersion": "jakarta.persistence:jakarta.persistence-api",
    # Jackson
    "jacksonVersion": "com.fasterxml.jackson.core:jackson-databind",
    "jacksonModuleScalaVersion": "com.fasterxml.jackson.module:jackson-module-scala_2.13",
    "jacksonDataformatYamlVersion": "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml",
    "jacksonDataformatXmlVersion": "com.fasterxml.jackson.dataformat:jackson-dataformat-xml",
    "jacksonDatatypeJsr310Version": "com.fasterxml.jackson.datatype:jackson-datatype-jsr310",
    # Logging
    "slf4jVersion": "org.slf4j:slf4j-api",
    "logbackVersion": "ch.qos.logback:logback-classic",
    "log4j2Version": "org.apache.logging.log4j:log4j-core",
    "logstashLogbackEncoderVersion": "net.logstash.logback:logstash-logback-encoder",
    # Database
    "flywayVersion": "org.flywaydb:flyway-core",
    "liquibaseVersion": "org.liquibase:liquibase-core",
    "h2Version": "com.h2database:h2",
    "postgresVersion": "org.postgresql:postgresql",
    "mysqlVersion": "com.mysql:mysql-connector-j",
    "mssqlVersion": "com.microsoft.sqlserver:mssql-jdbc",
    "hikariVersion": "com.zaxxer:HikariCP",
    "c3p0Version": "com.mchange:c3p0",
    # Apache Commons
    "commonsLangVersion": "org.apache.commons:commons-lang3",
    "commonsCollectionsVersion": "org.apache.commons:commons-collections4",
    "commonsIoVersion": "commons-io:commons-io",
    "commonsCodecVersion": "commons-codec:commons-codec",
    "commonsTextVersion": "org.apache.commons:commons-text",
    "commonsMathVersion": "org.apache.commons:commons-math3",
    "commonsNetVersion": "commons-net:commons-net",
    "commonsCompressVersion": "org.apache.commons:commons-compress",
    "commonsCsvVersion": "org.apache.commons:commons-csv",
    # HTTP / Networking
    "httpClientVersion": "org.apache.httpcomponents:httpclient",
    "httpCoreVersion": "org.apache.httpcomponents:httpcore",
    "httpClient5Version": "org.apache.httpcomponents.client5:httpclient5",
    "okHttpVersion": "com.squareup.okhttp3:okhttp",
    "retrofitVersion": "com.squareup.retrofit2:retrofit",
    "nettyVersion": "io.netty:netty-all",
    "grpcVersion": "io.grpc:grpc-core",
    "grpcNettyVersion": "io.grpc:grpc-netty-shaded",
    # Serialization
    "protobufVersion": "com.google.protobuf:protobuf-java",
    "dom4jVersion": "org.dom4j:dom4j",
    "jaxenVersion": "jaxen:jaxen",
    "snakeyamlVersion": "org.yaml:snakeyaml",
    "gsonVersion": "com.google.gson:gson",
    "kryoVersion": "com.esotericsoftware:kryo",
    # Testing
    "junitVersion": "junit:junit",
    "junit5Version": "org.junit.jupiter:junit-jupiter",
    "testngVersion": "org.testng:testng",
    "mockitoVersion": "org.mockito:mockito-core",
    "hamcrestVersion": "org.hamcrest:hamcrest",
    "assertjVersion": "org.assertj:assertj-core",
    "spockVersion": "org.spockframework:spock-core",
    "awaitilityVersion": "org.awaitility:awaitility",
    "wireMockVersion": "com.github.tomakehurst:wiremock",
    "restAssuredVersion": "io.rest-assured:rest-assured",
    "testContainersVersion": "org.testcontainers:testcontainers",
    # Security
    "bouncycastleVersion": "org.bouncycastle:bcprov-jdk18on",
    "nimbusJoseVersion": "com.nimbusds:nimbus-jose-jwt",
    "keycloakVersion": "org.keycloak:keycloak-core",
    "oauthClientVersion": "com.google.auth:google-auth-library-oauth2-http",
    # Build tools
    "gradleVersion": "dev.gradleplugins:gradle-api",
    "lombokVersion": "org.projectlombok:lombok",
    "mapstructVersion": "org.mapstruct:mapstruct",
    "immutablesVersion": "org.immutables:value",
    # Guava / Google
    "guavaVersion": "com.google.guava:guava",
    "googleApiClientVersion": "com.google.api-client:google-api-client",
    "googleCloudVersion": "com.google.cloud:google-cloud-core",
    "googleHttpClientVersion": "com.google.http-client:google-http-client",
    "googleAuthVersion": "com.google.auth:google-auth-library-credentials",
    "observabilityVersion": "io.micrometer:micrometer-core",
    "micrometerVersion": "io.micrometer:micrometer-core",
    "opentelemetryVersion": "io.opentelemetry:opentelemetry-api",
    "prometheusVersion": "io.prometheus:simpleclient",
    # Akka/Pekko
    "akkaVersion": "com.typesafe.akka:akka-actor_2.13",
    "pekkoVersion": "org.apache.pekko:pekko-actor_2.13",
    "akkaHttpVersion": "com.typesafe.akka:akka-http_2.13",
    "pekkoHttpVersion": "org.apache.pekko:pekko-http_2.13",
    # Scala
    "scalatestVersion": "org.scalatest:scalatest_2.13",
    # Cache
    "caffeineVersion": "com.github.ben-manes.caffeine:caffeine",
    "ehcacheVersion": "org.ehcache:ehcache",
    "hazelcastVersion": "com.hazelcast:hazelcast",
    # Message queues
    "kafkaVersion": "org.apache.kafka:kafka-clients",
    "rabbitMQVersion": "com.rabbitmq:amqp-client",
    # Build plugins (xl-platform gradle.properties)
    "gradleNexusStagingVersion": "io.codearte.gradle.nexus:gradle-nexus-staging-plugin",
    "nebulaReleaseVersion": "com.netflix.nebula:nebula-release-plugin",
    "gradlePomPluginVersion": "ru.vyarus:gradle-pom-plugin",
    "grgitGradleVersion": "org.ajoberstar.grgit:grgit-gradle",
    "licensePluginVersion": "com.github.hierynomus:license-gradle-plugin",
    "sonarQubePluginVersion": "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin",
    # Jakarta EE (xl-jakartaee-bom)
    "jakartaActivationVersion": "jakarta.activation:jakarta.activation-api",
    "jakartaAnnotationVersion": "jakarta.annotation:jakarta.annotation-api",
    "jakartaBatchVersion": "jakarta.batch:jakarta.batch-api",
    "jakartaCdiVersion": "jakarta.enterprise:jakarta.enterprise.cdi-api",
    "jakartaEjbVersion": "jakarta.ejb:jakarta.ejb-api",
    "jakartaElVersion": "jakarta.el:jakarta.el-api",
    "jakartaFacesVersion": "jakarta.faces:jakarta.faces-api",
    "jakartaInjectVersion": "jakarta.inject:jakarta.inject-api",
    "jakartaInterceptorVersion": "jakarta.interceptor:jakarta.interceptor-api",
    "jakartaJsonVersion": "jakarta.json:jakarta.json-api",
    "jakartaJsonBindVersion": "jakarta.json.bind:jakarta.json.bind-api",
    "jakartaMailVersion": "jakarta.mail:jakarta.mail-api",
    "jakartaMessagingVersion": "jakarta.jms:jakarta.jms-api",
    "jakartaPersistenceVersion": "jakarta.persistence:jakarta.persistence-api",
    "jakartaSecurityVersion": "jakarta.security.enterprise:jakarta.security.enterprise-api",
    "jakartaServletVersion": "jakarta.servlet:jakarta.servlet-api",
    "jakartaTransactionVersion": "jakarta.transaction:jakarta.transaction-api",
    "jakartaValidationVersion": "jakarta.validation:jakarta.validation-api",
    "jakartaWebsocketVersion": "jakarta.websocket:jakarta.websocket-api",
    "jakartaWsRsVersion": "jakarta.ws.rs:jakarta.ws.rs-api",
    "jakartaXmlBindVersion": "jakarta.xml.bind:jakarta.xml.bind-api",
    "jakartaXmlSoapVersion": "jakarta.xml.soap:jakarta.xml.soap-api",
    "jakartaXmlWsVersion": "jakarta.xml.ws:jakarta.xml.ws-api",
    "jakartaResourceVersion": "jakarta.resource:jakarta.resource-api",
    "jakartaSecurityJaspicVersion": "jakarta.security.auth.message:jakarta.security.auth.message-api",
    "jakartaDeployVersion": "jakarta.enterprise.deploy:jakarta.enterprise.deploy-api",
    "jakartaManagementVersion": "jakarta.management.j2ee:jakarta.management.j2ee-api",
    "jakartaXmlRegistryVersion": "jakarta.xml.registry:jakarta.xml.registry-api",
    "jakartaConnectorVersion": "jakarta.resource:jakarta.resource-api",
}

# ---------------------------------------------------------------------------
# REPO_COORDINATES — 9 entries specific to overthere
# Maps artifact coordinate (group:artifact) as used in build.gradle to its
# canonical Maven coordinate for latest-version lookup
# ---------------------------------------------------------------------------
REPO_COORDINATES: dict[str, str] = {
    "io.codearte.gradle.nexus:gradle-nexus-staging-plugin": "io.codearte.gradle.nexus:gradle-nexus-staging-plugin",
    "com.netflix.nebula:nebula-release-plugin": "com.netflix.nebula:nebula-release-plugin",
    "ru.vyarus:gradle-pom-plugin": "ru.vyarus:gradle-pom-plugin",
    "org.ajoberstar.grgit:grgit-gradle": "org.ajoberstar.grgit:grgit-gradle",
    "com.github.hierynomus:license-gradle-plugin": "com.github.hierynomus:license-gradle-plugin",
    "com.hierynomus:sshj": "com.hierynomus:sshj",
    "com.hierynomus:smbj": "com.hierynomus:smbj",
    "com.google.auth:google-auth-library-oauth2-http": "com.google.auth:google-auth-library-oauth2-http",
    "com.google.cloud:google-cloud-os-login": "com.google.cloud:google-cloud-os-login",
}

# ---------------------------------------------------------------------------
# CODE_ANALYSIS_COORDINATES — 0 entries for overthere
# overthere has no codeAnalysis.gradle
# ---------------------------------------------------------------------------
CODE_ANALYSIS_COORDINATES: dict[str, str] = {}

# ---------------------------------------------------------------------------
# SKIP_VARIABLES — 24 entries: variables that should not be looked up
# ---------------------------------------------------------------------------
SKIP_VARIABLES: set[str] = {
    # Meta / non-version
    "scalaVersion",
    "scalaFullVersion",
    "pekkoMajorVersion",
    # Python plugin deps (13 total)
    "pyTestVersion",
    "pyMockVersion",
    "pyConanVersion",
    "pyJinja2Version",
    "pyParamikoVersion",
    "pyRequestsVersion",
    "pySixVersion",
    "pyYamlVersion",
    "pyLxmlVersion",
    "pySetuptoolsVersion",
    "pyWheelVersion",
    "pyTwineVersion",
    "pyVirtualenvVersion",
    # Internal artifacts
    "crashVersion",
    "scannitVersion",
    "docBaseStyleVersion",
    "overcastVersion",
    "jythonStandaloneVersion",
    # Derived versions (managed transitively)
    "jacksonAnnotationsVersion",
    # Gradle settings
    "languageLevel",
    "release.stage",
    # Internal group artifacts
    "overcastLibVersion",
    "xlPlatformVersion",
}

# ---------------------------------------------------------------------------
# DYNAMIC_PATTERNS — 46 regex patterns for unmapped variable inference
# Strip "Version" suffix, then try these transformations
# ---------------------------------------------------------------------------
DYNAMIC_PATTERNS: list[tuple[str, str]] = [
    (r"^spring(.+)$", "org.springframework:spring-{lower}"),
    (r"^springBoot(.+)$", "org.springframework.boot:spring-boot-{lower}"),
    (r"^springCloud(.+)$", "org.springframework.cloud:spring-cloud-{lower}"),
    (r"^springSecurity(.+)$", "org.springframework.security:spring-security-{lower}"),
    (r"^springData(.+)$", "org.springframework.data:spring-data-{lower}"),
    (r"^hibernate(.+)$", "org.hibernate.orm:hibernate-{lower}"),
    (r"^jackson(.+)$", "com.fasterxml.jackson.core:jackson-{lower}"),
    (r"^jacksonModule(.+)$", "com.fasterxml.jackson.module:jackson-module-{lower}"),
    (r"^jacksonDataformat(.+)$", "com.fasterxml.jackson.dataformat:jackson-dataformat-{lower}"),
    (r"^jacksonDatatype(.+)$", "com.fasterxml.jackson.datatype:jackson-datatype-{lower}"),
    (r"^slf4j(.*)$", "org.slf4j:slf4j-{lower}"),
    (r"^logback(.+)$", "ch.qos.logback:logback-{lower}"),
    (r"^log4j(.*)$", "org.apache.logging.log4j:log4j-{lower}"),
    (r"^commons(.+)$", "org.apache.commons:commons-{lower}"),
    (r"^apacheCommons(.+)$", "org.apache.commons:commons-{lower}"),
    (r"^netty(.*)$", "io.netty:netty-{lower}"),
    (r"^grpc(.*)$", "io.grpc:grpc-{lower}"),
    (r"^protobuf(.*)$", "com.google.protobuf:protobuf-{lower}"),
    (r"^guava$", "com.google.guava:guava"),
    (r"^gson$", "com.google.gson:gson"),
    (r"^okhttp(.*)$", "com.squareup.okhttp3:okhttp"),
    (r"^retrofit(.*)$", "com.squareup.retrofit2:retrofit"),
    (r"^kafka(.*)$", "org.apache.kafka:kafka-{lower}"),
    (r"^akka(.*)$", "com.typesafe.akka:akka-{lower}_2.13"),
    (r"^pekko(.*)$", "org.apache.pekko:pekko-{lower}_2.13"),
    (r"^junit(.*)$", "org.junit.jupiter:junit-jupiter"),
    (r"^testng$", "org.testng:testng"),
    (r"^mockito(.*)$", "org.mockito:mockito-{lower}"),
    (r"^assertj(.*)$", "org.assertj:assertj-{lower}"),
    (r"^hamcrest(.*)$", "org.hamcrest:hamcrest-{lower}"),
    (r"^spock(.*)$", "org.spockframework:spock-{lower}"),
    (r"^testContainers(.*)$", "org.testcontainers:testcontainers"),
    (r"^flyway(.*)$", "org.flywaydb:flyway-{lower}"),
    (r"^liquibase(.*)$", "org.liquibase:liquibase-{lower}"),
    (r"^h2(.*)$", "com.h2database:h2"),
    (r"^postgres(.*)$", "org.postgresql:postgresql"),
    (r"^hikari(.*)$", "com.zaxxer:HikariCP"),
    (r"^bouncycastle(.*)$", "org.bouncycastle:bcprov-jdk18on"),
    (r"^micrometer(.*)$", "io.micrometer:micrometer-{lower}"),
    (r"^opentelemetry(.*)$", "io.opentelemetry:opentelemetry-{lower}"),
    (r"^lombok$", "org.projectlombok:lombok"),
    (r"^mapstruct(.*)$", "org.mapstruct:mapstruct"),
    (r"^caffeine$", "com.github.ben-manes.caffeine:caffeine"),
    (r"^ehcache(.*)$", "org.ehcache:ehcache"),
    (r"^hazelcast(.*)$", "com.hazelcast:hazelcast"),
    (r"^dom4j(.*)$", "org.dom4j:dom4j"),
]

# ---------------------------------------------------------------------------
# Risk classification
# ---------------------------------------------------------------------------
CATEGORY_MAP: dict[str, str] = {
    "org.springframework": "Core Framework",
    "org.springframework.boot": "Core Framework",
    "org.springframework.security": "Security",
    "org.hibernate": "Core Framework",
    "com.fasterxml.jackson": "Serialization",
    "org.slf4j": "Logging",
    "ch.qos.logback": "Logging",
    "org.apache.logging.log4j": "Logging",
    "org.bouncycastle": "Security",
    "com.nimbusds": "Security",
    "org.apache.httpcomponents": "HTTP",
    "io.netty": "HTTP",
    "io.grpc": "HTTP",
    "com.squareup.okhttp3": "HTTP",
    "org.dom4j": "Serialization",
    "jaxen": "Serialization",
    "org.yaml": "Serialization",
    "com.google.protobuf": "Serialization",
    "commons-codec": "Serialization",
    "org.testng": "Testing",
    "org.mockito": "Testing",
    "org.hamcrest": "Testing",
    "org.assertj": "Testing",
    "com.google.guava": "Testing",
    "nl.javadude.assumeng": "Testing",
    "org.spockframework": "Testing",
    "org.testcontainers": "Testing",
    "com.google.auth": "Security",
    "com.google.cloud": "GCP",
    "com.google.apis": "GCP",
    "io.grpc:grpc-netty": "GCP",
    "com.hierynomus:sshj": "SSH",
    "com.hierynomus:smbj": "SMB",
    "jcifs": "SMB",
    "commons-net": "Network",
    "com.jcraft": "SSH",
    "net.engio": "Core Framework",
    "nl.javadude.scannit": "Core Framework",
    "org.flywaydb": "Data",
    "com.h2database": "Data",
    "org.postgresql": "Data",
    "com.zaxxer": "Data",
    "io.micrometer": "Observability",
    "io.opentelemetry": "Observability",
    "io.codearte.gradle.nexus": "Build",
    "com.netflix.nebula": "Build",
    "ru.vyarus": "Build",
    "org.ajoberstar.grgit": "Build",
    "com.github.hierynomus": "Build",
    "org.sonarsource": "Build",
    "jakarta": "Jakarta EE",
}

RISK_EMOJI: dict[str, str] = {
    "Security":       "🔴",
    "Core Framework": "🔴",
    "Logging":        "🔴",
    "SSH":            "🟠",
    "SMB":            "🟠",
    "Data":           "🟠",
    "Serialization":  "🟠",
    "HTTP":           "🟠",
    "GCP":            "🟠",
    "Network":        "🟠",
    "Testing":        "🟡",
    "Observability":  "🟡",
    "Jakarta EE":     "🟡",
    "Build":          "🟢",
    "Unknown":        "⚪",
}

# ---------------------------------------------------------------------------
# Spring compatibility matrix
# ---------------------------------------------------------------------------
SPRING_COMPAT: dict[str, dict] = {
    "hibernate": {
        "constraint": "Must be compatible with Spring Boot version",
        "spring_boot_6x": ">=6.0.0",
    },
    "spring-security": {
        "constraint": "Major version must match Spring Boot major version",
    },
    "jackson-databind": {
        "constraint": "Managed by Spring Boot BOM — do not upgrade independently",
    },
}

# ---------------------------------------------------------------------------
# Internal group prefixes — skip Maven Central lookup
# ---------------------------------------------------------------------------
INTERNAL_GROUP_PREFIXES: tuple[str, ...] = (
    "com.xebialabs.",
    "ai.digital.",
)

# ---------------------------------------------------------------------------
# Pre-release version filter patterns
# ---------------------------------------------------------------------------
PRE_RELEASE_PATTERNS: list[re.Pattern] = [
    re.compile(r"(?i)snapshot"),
    re.compile(r"(?i)(^|[.\-])alpha"),
    re.compile(r"(?i)(^|[.\-])beta"),
    re.compile(r"(?i)(^|[.\-])(rc|cr)\d*($|[.\-])"),
    re.compile(r"(?i)(^|[.\-])m\d+($|[.\-])"),
    re.compile(r"(?i)milestone"),
    re.compile(r"(?i)dev"),
    re.compile(r"(?i)incubating"),
    re.compile(r"(?i)-pr[\-\.]"),
    re.compile(r"(?i)-preview"),
]

# Google API services use non-semver versions like "v1-rev20251110-2.0.0"
NON_SEMVER_PATTERNS: list[re.Pattern] = [
    re.compile(r"^v\d+-rev\d+-"),  # e.g. v1-rev20251110-2.0.0
]


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _nexus_credentials() -> Optional[tuple[str, str]]:
    """Return (user, password) from env or ~/.gradle/gradle.properties, or None."""
    user = os.environ.get("NEXUS_USER")
    pwd = os.environ.get("NEXUS_PASSWORD")
    if user and pwd:
        return (user, pwd)
    gradle_props = Path.home() / ".gradle" / "gradle.properties"
    if gradle_props.exists():
        text = gradle_props.read_text(encoding="utf-8", errors="replace")
        u_match = re.search(r"^nexusUserName\s*=\s*(.+)$", text, re.MULTILINE)
        p_match = re.search(r"^nexusPassword\s*=\s*(.+)$", text, re.MULTILINE)
        if u_match and p_match:
            return (u_match.group(1).strip(), p_match.group(1).strip())
    return None


_NEXUS_CREDS: Optional[tuple[str, str]] = _nexus_credentials()


def fetch_url(url: str, auth: Optional[tuple[str, str]] = None) -> Optional[str]:
    """Fetch URL content as text, returns None on error."""
    req = urllib.request.Request(url)
    req.add_header("User-Agent", "overthere-dep-scan/1.0")
    if auth:
        token = base64.b64encode(f"{auth[0]}:{auth[1]}".encode()).decode()
        req.add_header("Authorization", f"Basic {token}")
    try:
        with urllib.request.urlopen(req, timeout=REQUEST_TIMEOUT) as resp:
            return resp.read().decode("utf-8", errors="replace")
    except Exception:
        return None


def is_pre_release(version: str) -> bool:
    for pat in PRE_RELEASE_PATTERNS:
        if pat.search(version):
            return True
    return False


def is_non_semver(version: str) -> bool:
    for pat in NON_SEMVER_PATTERNS:
        if pat.search(version):
            return True
    return False


def parse_semver(version: str) -> Optional[tuple[int, int, int]]:
    """Parse a version string into (major, minor, patch) tuple, or None."""
    # Strip leading 'v'
    v = version.lstrip("v")
    m = re.match(r"^(\d+)\.(\d+)(?:\.(\d+))?", v)
    if m:
        return (int(m.group(1)), int(m.group(2)), int(m.group(3) or 0))
    return None


def classify_bump(current: str, latest: str) -> str:
    """Return 'major', 'minor', 'patch', or 'unknown'."""
    c = parse_semver(current)
    n = parse_semver(latest)
    if not c or not n:
        return "unknown"
    if n[0] > c[0]:
        return "major"
    if n[1] > c[1]:
        return "minor"
    if n[2] > c[2]:
        return "patch"
    return "up-to-date"


def latest_from_maven_central(group: str, artifact: str) -> Optional[str]:
    """Fetch the latest stable version from Maven Central maven-metadata.xml."""
    cache_key = f"{group}:{artifact}"
    if cache_key in CACHE:
        return CACHE[cache_key]
    if cache_key in FAILED:
        return None

    url = MAVEN_CENTRAL_METADATA.format(
        group=group.replace(".", "/"), artifact=artifact
    )
    body = fetch_url(url)
    if not body:
        # Try Maven Central search API as fallback
        body = _maven_central_search(group, artifact)

    result = _extract_latest_stable(body) if body else None
    if result:
        CACHE[cache_key] = result
    else:
        FAILED.add(cache_key)
    return result


def _maven_central_search(group: str, artifact: str) -> Optional[str]:
    """Query Maven Central search API, return pseudo-metadata XML or None."""
    url = f"{MAVEN_CENTRAL_SEARCH}?q={urllib.parse.quote(f'g:{group} AND a:{artifact}')}&rows=5&wt=json&core=gav"
    body = fetch_url(url)
    if not body:
        return None
    try:
        data = json.loads(body)
        versions = [doc["v"] for doc in data.get("response", {}).get("docs", [])]
        return "<versions>" + "".join(f"<version>{v}</version>" for v in versions) + "</versions>"
    except Exception:
        return None


def latest_from_nexus(group: str, artifact: str) -> Optional[str]:
    """Try each Nexus repo for the artifact's latest stable version."""
    if not _NEXUS_CREDS:
        return None
    for repo in NEXUS_REPOS:
        url = (
            f"{NEXUS_BASE_URL}/repositories/{repo}/"
            f"{group.replace('.', '/')}/{artifact}/maven-metadata.xml"
        )
        body = fetch_url(url, auth=_NEXUS_CREDS)
        if body:
            result = _extract_latest_stable(body)
            if result:
                return result
    return None


def _extract_latest_stable(xml_body: str) -> Optional[str]:
    """Extract the highest non-pre-release version from maven-metadata.xml content."""
    versions = re.findall(r"<version>([^<]+)</version>", xml_body)
    stable = [v for v in versions if not is_pre_release(v)]
    if not stable:
        return None
    # Sort by semver, keep string for non-semver
    def sort_key(v: str):
        parsed = parse_semver(v)
        return parsed if parsed else (0, 0, 0)
    stable.sort(key=sort_key)
    return stable[-1]


def get_latest_version(group: str, artifact: str) -> Optional[str]:
    """Full version lookup: Maven Central → Nexus fallback."""
    # Skip internal artifacts
    coord = f"{group}:{artifact}"
    for prefix in INTERNAL_GROUP_PREFIXES:
        if group.startswith(prefix.rstrip(".")):
            return None
    result = latest_from_maven_central(group, artifact)
    if not result:
        result = latest_from_nexus(group, artifact)
    return result


def get_category(group: str, artifact: str) -> str:
    coord = f"{group}:{artifact}"
    for prefix, cat in CATEGORY_MAP.items():
        if coord.startswith(prefix) or group.startswith(prefix):
            return cat
    return "Unknown"


def infer_coordinate(var_name: str) -> Optional[str]:
    """
    Try to infer a Maven coordinate from a variable name using DYNAMIC_PATTERNS.
    Returns "group:artifact" or None.
    """
    # Strip "Version" suffix
    base = re.sub(r"Version$", "", var_name, flags=re.IGNORECASE)
    if not base:
        return None
    for pattern_str, template in DYNAMIC_PATTERNS:
        m = re.match(pattern_str, base, re.IGNORECASE)
        if m:
            suffix = m.group(1).lower() if m.lastindex and m.lastindex >= 1 else ""
            coord = template.replace("{lower}", suffix)
            # Clean up double hyphens or trailing hyphens
            coord = re.sub(r"-+", "-", coord)
            coord = re.sub(r"-$", "", coord)
            return coord
    return None


# ---------------------------------------------------------------------------
# Conf file parser (HOCON-style: key: "value" or key = "value")
# ---------------------------------------------------------------------------
def parse_conf_file(path: Path) -> dict[str, str]:
    """Parse a .conf file and return {variableName: version} for version-like values."""
    result = {}
    if not path.exists():
        return result
    text = path.read_text(encoding="utf-8", errors="replace")
    # Match: key: "1.2.3" or key = "1.2.3" or key: 1.2.3
    pattern = re.compile(
        r'^[ \t]*([a-zA-Z][a-zA-Z0-9_.-]*)[ \t]*[:=][ \t]*"?([0-9][^"\s,}]+)"?',
        re.MULTILINE,
    )
    for m in pattern.finditer(text):
        key, val = m.group(1).strip(), m.group(2).strip()
        val = val.strip('"\'')
        if re.match(r"^\d", val):
            result[key] = val
    return result


# ---------------------------------------------------------------------------
# build.gradle parser
# ---------------------------------------------------------------------------
def parse_build_gradle(path: Path) -> list[dict]:
    """
    Parse a build.gradle file and return a list of dependency dicts:
    {group, artifact, version, source_file, line_num, scope}
    """
    if not path.exists():
        print(f"  [WARN] File not found: {path}")
        return []

    text = path.read_text(encoding="utf-8", errors="replace")
    deps = []

    # Match strings like 'group:artifact:version' or "group:artifact:version"
    dep_pattern = re.compile(
        r"""['"]([\w.\-]+):([\w.\-]+):([\w.\-+]+)['"]""",
        re.MULTILINE,
    )
    # Also match: id "plugin.id" version "x.y.z"
    plugin_pattern = re.compile(
        r"""id\s+['"]([^'"]+)['"]\s+version\s+['"]([^'"]+)['"]""",
        re.MULTILINE,
    )

    lines = text.splitlines()

    for i, line in enumerate(lines, 1):
        m = dep_pattern.search(line)
        if m:
            group, artifact, version = m.group(1), m.group(2), m.group(3)
            # Determine scope
            scope = "implementation"
            if "testImplementation" in line or "testCompile" in line:
                scope = "test"
            elif "classpath" in line:
                scope = "buildscript"
            elif "api " in line or "api(" in line:
                scope = "api"
            deps.append({
                "group": group,
                "artifact": artifact,
                "version": version,
                "source_file": str(path.name),
                "line_num": i,
                "scope": scope,
            })
            continue
        m2 = plugin_pattern.search(line)
        if m2:
            plugin_id, version = m2.group(1), m2.group(2)
            deps.append({
                "group": plugin_id,
                "artifact": plugin_id.split(".")[-1],
                "version": version,
                "source_file": str(path.name),
                "line_num": i,
                "scope": "plugin",
            })

    return deps


# ---------------------------------------------------------------------------
# Phase 1 — xl-platform scan
# ---------------------------------------------------------------------------
def scan_xl_platform(xl_platform_dir: Path) -> list[dict]:
    """
    Scan the xl-platform BOM for upstream dependency updates.
    Returns list of update dicts.
    """
    print("\n=== Phase 1: xl-platform scan ===")
    updates = []

    # Source files in xl-platform
    sources = {
        "xl-reference.conf": xl_platform_dir / "xl-reference" / "xl-reference.conf",
        "xl-jakartaee-bom.conf": xl_platform_dir / "xl-jakartaee-bom" / "xl-jakartaee-bom.conf",
        "gradle.properties": xl_platform_dir / "gradle.properties",
    }

    all_vars: dict[str, tuple[str, str]] = {}  # varName -> (version, source_file)
    for label, fpath in sources.items():
        if not fpath.exists():
            print(f"  [SKIP] Not found: {fpath}")
            continue
        if fpath.suffix == ".properties":
            # Parse gradle.properties: key=value
            text = fpath.read_text(encoding="utf-8", errors="replace")
            for m in re.finditer(r"^([a-zA-Z][a-zA-Z0-9._-]*)\s*=\s*(\S+)$", text, re.MULTILINE):
                key, val = m.group(1), m.group(2)
                if re.match(r"^\d", val):
                    all_vars[key] = (val, label)
        else:
            for key, val in parse_conf_file(fpath).items():
                all_vars[key] = (val, label)

    print(f"  Found {len(all_vars)} version variables across xl-platform sources")

    for var_name, (current_ver, source) in sorted(all_vars.items()):
        if var_name in SKIP_VARIABLES:
            continue

        # Try explicit mapping first
        coord = XL_PLATFORM_COORDINATES.get(var_name)
        method = "mapped"

        # Fall back to dynamic inference
        if not coord:
            coord = infer_coordinate(var_name)
            method = "inferred"

        if not coord:
            print(f"  [SKIP] No coordinate for {var_name}")
            continue

        group, _, artifact = coord.partition(":")
        # Skip internal artifacts
        skip = False
        for prefix in INTERNAL_GROUP_PREFIXES:
            if group.startswith(prefix.rstrip(".")):
                skip = True
                break
        if skip:
            continue

        if is_non_semver(current_ver):
            print(f"  [SKIP] Non-semver version for {var_name}={current_ver}")
            continue

        print(f"  Checking {var_name} ({group}:{artifact}) current={current_ver}...", end="", flush=True)
        latest = get_latest_version(group, artifact)
        if not latest:
            print(" no data")
            continue

        bump = classify_bump(current_ver, latest)
        print(f" latest={latest} [{bump}]")

        if bump in ("major", "minor", "patch"):
            category = get_category(group, artifact)
            updates.append({
                "type": "xl-platform",
                "variable": var_name,
                "source_file": source,
                "group": group,
                "artifact": artifact,
                "current": current_ver,
                "latest": latest,
                "bump": bump,
                "category": category,
                "risk": RISK_EMOJI.get(category, "⚪"),
                "method": method,
            })

    print(f"  => {len(updates)} updates available in xl-platform")
    return updates


# ---------------------------------------------------------------------------
# Phase 2 — repo scan (overthere)
# ---------------------------------------------------------------------------
def scan_repo(repo_dir: Path) -> list[dict]:
    """
    Scan overthere's build.gradle for dependency updates.
    Returns list of update dicts.
    """
    print("\n=== Phase 2: overthere repo scan ===")
    updates = []

    build_gradle = repo_dir / "build.gradle"
    raw_deps = parse_build_gradle(build_gradle)
    print(f"  Found {len(raw_deps)} dependency entries in build.gradle")

    seen: set[str] = set()

    for dep in raw_deps:
        group = dep["group"]
        artifact = dep["artifact"]
        current_ver = dep["version"]
        coord = f"{group}:{artifact}"

        # Skip internal artifacts
        skip_internal = any(group.startswith(p.rstrip(".")) for p in INTERNAL_GROUP_PREFIXES)
        if skip_internal:
            continue

        # Skip non-semver versions (like google-api-services-compute v1-rev...)
        if is_non_semver(current_ver):
            print(f"  [SKIP] Non-semver: {coord}:{current_ver}")
            continue

        # Skip duplicates
        key = coord
        if key in seen:
            continue
        seen.add(key)

        # Skip variables in SKIP_VARIABLES (by group:artifact pattern)
        if coord.startswith("com.xebialabs") or coord.startswith("ai.digital"):
            continue

        print(f"  Checking {coord} current={current_ver}...", end="", flush=True)
        latest = get_latest_version(group, artifact)
        if not latest:
            print(" no data")
            continue

        bump = classify_bump(current_ver, latest)
        print(f" latest={latest} [{bump}]")

        if bump in ("major", "minor", "patch"):
            category = get_category(group, artifact)
            updates.append({
                "type": "repo",
                "source_file": dep["source_file"],
                "line_num": dep["line_num"],
                "group": group,
                "artifact": artifact,
                "current": current_ver,
                "latest": latest,
                "bump": bump,
                "scope": dep["scope"],
                "category": category,
                "risk": RISK_EMOJI.get(category, "⚪"),
            })

    print(f"  => {len(updates)} updates available in overthere")
    return updates


# ---------------------------------------------------------------------------
# Phase 3 — alignment check
# ---------------------------------------------------------------------------
def check_alignment(repo_dir: Path, all_deps: list[dict]) -> list[dict]:
    """
    Verify version consistency rules within build.gradle.
    Returns list of alignment issue dicts.
    """
    print("\n=== Phase 3: alignment check ===")
    issues = []

    # Build a map of group:artifact -> version from parsed deps
    version_map: dict[str, str] = {}
    build_gradle = repo_dir / "build.gradle"
    raw_deps = parse_build_gradle(build_gradle)
    for d in raw_deps:
        version_map[f"{d['group']}:{d['artifact']}"] = d["version"]

    # Rule 1: Bouncy Castle bcprov and bcpkix should match
    bcprov = version_map.get("org.bouncycastle:bcprov-jdk18on")
    bcpkix = version_map.get("org.bouncycastle:bcpkix-jdk18on")
    if bcprov and bcpkix and bcprov != bcpkix:
        issues.append({
            "type": "alignment",
            "rule": "Bouncy Castle version mismatch",
            "details": f"bcprov-jdk18on={bcprov} vs bcpkix-jdk18on={bcpkix}",
            "fix": f"Set both to the same version (suggest: {bcprov})",
        })

    # Rule 2: SLF4J slf4j-api and jcl-over-slf4j should match
    slf4j = version_map.get("org.slf4j:slf4j-api")
    jcl = version_map.get("org.slf4j:jcl-over-slf4j")
    if slf4j and jcl and slf4j != jcl:
        issues.append({
            "type": "alignment",
            "rule": "SLF4J version mismatch",
            "details": f"slf4j-api={slf4j} vs jcl-over-slf4j={jcl}",
            "fix": f"Set both to the same version (suggest: {slf4j})",
        })

    # Rule 3: Apache HttpComponents httpclient and httpcore minor alignment (4.x)
    httpclient = version_map.get("org.apache.httpcomponents:httpclient")
    httpcore = version_map.get("org.apache.httpcomponents:httpcore")
    if httpclient and httpcore:
        hc = parse_semver(httpclient)
        hcore = parse_semver(httpcore)
        if hc and hcore and hc[0] != hcore[0]:
            issues.append({
                "type": "alignment",
                "rule": "HttpComponents major version mismatch",
                "details": f"httpclient={httpclient} vs httpcore={httpcore}",
                "fix": "Ensure httpclient and httpcore are from the same major release line",
            })

    if issues:
        print(f"  => {len(issues)} alignment issue(s) found")
        for iss in issues:
            print(f"  [WARN] {iss['rule']}: {iss['details']}")
    else:
        print("  => No alignment issues found")

    return issues


# ---------------------------------------------------------------------------
# Report generation
# ---------------------------------------------------------------------------
def generate_reports(
    repo_dir: Path,
    xl_updates: list[dict],
    repo_updates: list[dict],
    alignment_issues: list[dict],
):
    """Write dependency-upgrade-report.md and dependency-upgrade-report.json."""
    timestamp = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")

    # --- Markdown report ---
    md_lines = [
        f"# Dependency Upgrade Report",
        f"",
        f"Generated: {timestamp}",
        f"",
    ]

    def add_section(title: str, updates: list[dict], key_col: str = "artifact"):
        if not updates:
            return
        md_lines.append(f"## {title}")
        md_lines.append("")
        # Group by bump type
        for bump_type in ("major", "minor", "patch"):
            group_items = [u for u in updates if u.get("bump") == bump_type]
            if not group_items:
                continue
            md_lines.append(f"### {bump_type.capitalize()} updates")
            md_lines.append("")
            md_lines.append("| Risk | Dependency | Current | Latest | Bump | Category |")
            md_lines.append("|------|------------|---------|--------|------|----------|")
            for u in sorted(group_items, key=lambda x: x.get("category", "")):
                risk = u.get("risk", "⚪")
                coord = f"{u['group']}:{u['artifact']}"
                current = u["current"]
                latest = u["latest"]
                bump = u["bump"]
                cat = u.get("category", "Unknown")
                md_lines.append(f"| {risk} | `{coord}` | `{current}` | `{latest}` | {bump} | {cat} |")
            md_lines.append("")

    if xl_updates:
        add_section("xl-platform BOM Updates (Phase 1)", xl_updates)
    if repo_updates:
        add_section("overthere Direct Dependency Updates (Phase 2)", repo_updates)
    if alignment_issues:
        md_lines.append("## Alignment Issues (Phase 3)")
        md_lines.append("")
        for iss in alignment_issues:
            md_lines.append(f"- **{iss['rule']}**: {iss['details']}")
            md_lines.append(f"  - Fix: {iss['fix']}")
        md_lines.append("")

    if not xl_updates and not repo_updates and not alignment_issues:
        md_lines.append("All dependencies are up to date. ✅")
        md_lines.append("")

    total = len(xl_updates) + len(repo_updates)
    md_lines.append(f"---")
    md_lines.append(
        f"**Summary**: {total} update(s) available "
        f"({len([u for u in xl_updates + repo_updates if u['bump']=='major'])} major, "
        f"{len([u for u in xl_updates + repo_updates if u['bump']=='minor'])} minor, "
        f"{len([u for u in xl_updates + repo_updates if u['bump']=='patch'])} patch)"
    )

    md_path = repo_dir / "dependency-upgrade-report.md"
    with open(md_path, "w", encoding="utf-8") as f:
        f.write("\n".join(md_lines))
    print(f"\n  Report written: {md_path}")

    # --- JSON report ---
    json_data = {
        "generated": timestamp,
        "summary": {
            "total_updates": total,
            "major": len([u for u in xl_updates + repo_updates if u["bump"] == "major"]),
            "minor": len([u for u in xl_updates + repo_updates if u["bump"] == "minor"]),
            "patch": len([u for u in xl_updates + repo_updates if u["bump"] == "patch"]),
            "alignment_issues": len(alignment_issues),
        },
        "xl_platform_updates": xl_updates,
        "repo_updates": repo_updates,
        "alignment_issues": alignment_issues,
    }

    json_path = repo_dir / "dependency-upgrade-report.json"
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(json_data, f, indent=2, ensure_ascii=False)
    print(f"  Report written: {json_path}")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
def main():
    parser = argparse.ArgumentParser(description="Dependency scanner for overthere")
    parser.add_argument(
        "--phase",
        choices=["all", "xl-platform-scan", "scan", "alignment"],
        default="scan",
        help="Which phase(s) to run",
    )
    parser.add_argument(
        "--xl-platform-dir",
        default="../xl-platform",
        help="Path to the xl-platform repository root",
    )
    args = parser.parse_args()

    # Locate repo root: assume this script lives at .github/skills/dependency-scan/scripts/
    script_dir = Path(__file__).resolve().parent
    repo_dir = script_dir.parent.parent.parent.parent  # up 4 levels
    # Sanity check
    if not (repo_dir / "build.gradle").exists():
        # Try current working directory
        repo_dir = Path.cwd()
        if not (repo_dir / "build.gradle").exists():
            print("[ERROR] Cannot locate repo root (build.gradle not found).")
            print(f"  Tried: {repo_dir}")
            sys.exit(1)

    xl_platform_dir = Path(args.xl_platform_dir).resolve()
    phase = args.phase

    print(f"Overthere dependency scan")
    print(f"  repo_dir      : {repo_dir}")
    print(f"  xl_platform_dir: {xl_platform_dir}")
    print(f"  phase         : {phase}")
    print(f"  nexus creds   : {'found' if _NEXUS_CREDS else 'not found (Maven Central only)'}")

    xl_updates: list[dict] = []
    repo_updates: list[dict] = []
    alignment_issues: list[dict] = []

    if phase in ("all", "xl-platform-scan"):
        if not xl_platform_dir.exists():
            print(f"\n[WARN] xl-platform directory not found: {xl_platform_dir}")
            print("  Skipping Phase 1. Use --xl-platform-dir to specify the correct path.")
        else:
            xl_updates = scan_xl_platform(xl_platform_dir)

    if phase in ("all", "scan"):
        repo_updates = scan_repo(repo_dir)

    if phase in ("all", "alignment", "scan"):
        alignment_issues = check_alignment(repo_dir, repo_updates)

    generate_reports(repo_dir, xl_updates, repo_updates, alignment_issues)
    print("\nDone.")


if __name__ == "__main__":
    main()
