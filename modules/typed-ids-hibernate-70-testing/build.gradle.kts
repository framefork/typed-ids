plugins {
    id("framefork.java")
}

dependencies {
    api(project(":typed-ids-testing"))

    api(libs.hibernate.orm.v70)
    api(libs.hypersistence.utils.hibernate70)
}
