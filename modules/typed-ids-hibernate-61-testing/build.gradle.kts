plugins {
    id("framefork.java")
}

dependencies {
    api(project(":typed-ids-testing"))

    api(libs.hibernate.orm.v61)
    api(libs.hypersistence.utils.hibernate61)
}
