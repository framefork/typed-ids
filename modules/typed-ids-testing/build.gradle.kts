plugins {
    id("framefork.java")
}

dependencies {
    api(libs.junit.jupiter)
    api(libs.assertj)

    api(libs.uuidGenerator)
    api(libs.hypersistence.tsid)

    api(platform(libs.testcontainers.bom))
    api(libs.testcontainers.postgresql)
    api(libs.testcontainers.mysql)
    api(libs.testcontainers.base)
    api(libs.datasource.proxy)
    api(libs.datasource.hikaricp)
    api(libs.logback.classic)

    api(libs.postgresql)
    api(libs.mysql)
}
