plugins {
    id("framefork.java")
}

dependencies {
    api(libs.junit.jupiter)

    implementation(libs.hibernate.orm.v63)
    implementation(libs.hypersistence.utils.hibernate63)

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
